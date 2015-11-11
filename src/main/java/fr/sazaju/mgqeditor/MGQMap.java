package fr.sazaju.mgqeditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.sazaju.mgqeditor.regex.Scripts;
import fr.sazaju.mgqeditor.regex.Scripts.Attack;
import fr.sazaju.mgqeditor.regex.Scripts.FullSentenceID;
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
				logger.info("Saving " + file + "...");
				InputStream stream = parsed.getInputStream();
				try {
					FileOutputStream out = new FileOutputStream(file);
					byte[] buffer = new byte[256];
					int count;
					while ((count = stream.read(buffer)) != -1) {
						out.write(buffer, 0, count);
					}
					out.close();
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				logger.info("File saved.");
			}
		};

		logger.info("Building entries...");
		entries = new LinkedList<>();
		Map<FullSentenceID, Storage> originalsToRetrieve = new HashMap<>();
		for (Monster monster : parsed) {
			for (Attack attack : monster) {
				for (final Sentence sentence : attack) {
					logger.info("Building entry...");
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
					Storage originalStorage = new Storage() {

						private String original = "<not provided>";

						@Override
						public String read() {
							return formatter.switchForth(original);
						}

						@Override
						public void write(String content) {
							content = formatter.switchBack(content);
							original = content;
						}
					};
					originalsToRetrieve.put(new FullSentenceID(monster, attack,
							sentence), originalStorage);
					MGQEntry entry = new MGQEntry(originalStorage,
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
