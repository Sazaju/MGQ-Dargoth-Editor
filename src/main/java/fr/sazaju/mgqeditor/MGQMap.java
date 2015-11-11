package fr.sazaju.mgqeditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.sazaju.mgqeditor.regex.Scripts;
import fr.sazaju.mgqeditor.regex.Scripts.Attack;
import fr.sazaju.mgqeditor.regex.Scripts.Monster;
import fr.sazaju.mgqeditor.regex.Scripts.Sentence;
import fr.sazaju.mgqeditor.util.Saver;
import fr.sazaju.mgqeditor.util.Storage;
import fr.vergne.ioutils.FileUtils;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.util.Switcher;

public class MGQMap implements TranslationMap<MGQEntry> {

	private static final Logger logger = Logger.getLogger(MGQMap.class
			.getName());
	private final File file;
	private final Scripts parsed;
	private final List<MGQEntry> entries;
	private final Saver saver;
	private final Switcher<String, String> formatter = new Switcher<String, String>() {

		@Override
		public String switchForth(String raw) {
			return raw.replace("\\n", "\n");
		}

		@Override
		public String switchBack(String formatted) {
			return formatted.replace("\n", "\\n");
		}
	};

	public MGQMap(MapID id) throws IOException {
		file = id.getFile();
		
		logger.info("Parsing "+file+"...");
		parsed = new Scripts();
		parsed.setContent(FileUtils.readFileToString(file));
		logger.info("File parsed...");

		saver = new Saver() {

			@Override
			public void save() {
				try {
					FileUtils.write(file, parsed.getContent());
				} catch (FileNotFoundException e) {
					logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		};

		logger.info("Building entries...");
		entries = new LinkedList<>();
		for (Monster monster : parsed) {
			for (Attack attack : monster) {
				for (final Sentence sentence : attack) {
					logger.info("Building entry...");
					String original = "<not provided>";
					String translation = sentence.getMessage();
					Storage translationStorage = new Storage() {

						@Override
						public String read() {
							return formatter.switchForth(sentence.getMessage());
						}

						@Override
						public void write(String content) {
							content = formatter.switchBack(content);
							logger.info("Update sentence " + sentence + ": "
									+ content);
							sentence.setMessage(content);
						}
					};
					MGQEntry entry = new MGQEntry(original, translation,
							translationStorage, saver);
					logger.info("Entry: " + entry);
					entries.add(entry);
				}
			}
		}
		logger.info("Entries built...");
	}

	@Override
	public Iterator<MGQEntry> iterator() {
		return entries.iterator();
	}

	@Override
	public MGQEntry getEntry(int index) {
		return entries.get(index);
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public void saveAll() {
		for (MGQEntry entry : entries) {
			entry.writeAll();
		}
		saver.save();
	}

	@Override
	public void resetAll() {
		for (MGQEntry entry : entries) {
			entry.resetAll();
		}
	}
}
