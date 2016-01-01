package fr.sazaju.mgqeditor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import fr.sazaju.mgqeditor.parser.Parser;
import fr.sazaju.mgqeditor.parser.Parser.Sentence;
import fr.sazaju.mgqeditor.util.Generator;
import fr.sazaju.mgqeditor.util.Saver;
import fr.sazaju.mgqeditor.util.Storage;
import fr.vergne.ioutils.FileUtils;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.util.Switcher;
import fr.vergne.translation.util.impl.FileBasedProperties;

public class MGQMap implements TranslationMap<MGQEntry> {

	private static final Logger logger = Logger.getLogger(MGQMap.class
			.getName());
	private final File file;
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

	public <SentenceID> MGQMap(MapID mapID, File projectDirectory,
			Generator<? extends Parser<SentenceID>> parserGenerator)
			throws IOException {
		file = mapID.getFile();

		logger.info("Parsing " + file + "...");
		final Parser<SentenceID> parser = parserGenerator.generates();
		parser.setContent(FileUtils.readFileToString(file));
		logger.info("File parsed...");

		saver = new Saver() {

			@Override
			public void save() {
				logger.info("Saving " + file + "...");
				InputStream stream = parser.getInputStream();
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
		Map<SentenceID, Storage> originalsToRetrieve = new HashMap<>();
		for (SentenceID id : parser) {
			final Sentence sentence = parser.getSentence(id);
			Storage translationStorage = new Storage() {

				private final Logger logger = Logger.getLogger(Storage.class
						.getName());

				@Override
				public String read() {
					return formatter.switchForth(sentence.getContent());
				}

				@Override
				public void write(String content) {
					content = formatter.switchBack(content);
					logger.info("Update sentence " + sentence + ": " + content);
					sentence.setContent(content);
				}
			};
			Storage originalStorage = new Storage() {

				private String original = "<not provided>";
				private final Logger logger = Logger.getLogger(Storage.class
						.getName());

				@Override
				public String read() {
					return formatter.switchForth(original);
				}

				@Override
				public void write(String content) {
					content = formatter.switchBack(content);
					logger.info("Retrieve original " + sentence + ": "
							+ content);
					original = content;
				}
			};

			Storage old = originalsToRetrieve.put(id, originalStorage);
			if (old == null) {
				// new ID, expected
			} else {
				throw new RuntimeException(
						"An ID occurs for multiple sentences: " + id);
			}

			MGQEntry entry = new MGQEntry(originalStorage, translationStorage,
					saver);
			entries.add(entry);
			logger.finest("Entry added: " + entry);
		}
		logger.info("Entries built: " + entries.size());

		Path pathAbsolute = Paths.get(mapID.getFile().getPath());
		Path pathBase = Paths.get(projectDirectory.getPath());
		Path pathRelative = pathBase.relativize(pathAbsolute);
		String filePath = pathRelative.toString();

		if (originalsToRetrieve.isEmpty()) {
			// nothing to retrieve
		} else {
			retrieveFromCache(projectDirectory, originalsToRetrieve, filePath);
		}
		if (originalsToRetrieve.isEmpty()) {
			// nothing more to retrieve
		} else {
			retrieveFromGit(projectDirectory, originalsToRetrieve, filePath,
					parserGenerator);
		}

		if (originalsToRetrieve.isEmpty()) {
			// all done
		} else {
			logger.severe("Missing Japanese: " + originalsToRetrieve.size());
		}
	}

	private <SentenceID> void retrieveFromCache(File projectDirectory,
			Map<SentenceID, Storage> originalsToRetrieve, String filePath) {
		File cacheDirectory = getCacheDirectory();
		File cacheFile = new File(cacheDirectory, filePath);
		FileBasedProperties cache = new FileBasedProperties(cacheFile, false);

		logger.info("Retrieving Japanese from cache " + cacheFile + "...");
		Iterator<Entry<SentenceID, Storage>> iterator = originalsToRetrieve
				.entrySet().iterator();
		int total = originalsToRetrieve.size();
		int count = 0;
		while (iterator.hasNext()) {
			Entry<SentenceID, Storage> entry = iterator.next();
			SentenceID sentenceID = entry.getKey();
			String content = cache.getProperty(sentenceID.toString(), null);
			if (content == null) {
				// unknown entry, nothing retrieved
			} else {
				count++;
				Storage storage = entry.getValue();
				storage.write(content);
				iterator.remove();
			}
		}
		logger.info("Japanese retrieved: " + count + "/" + total);
	}

	private <SentenceID> void retrieveFromGit(File projectDirectory,
			Map<SentenceID, Storage> originalsToRetrieve, String filePath,
			Generator<? extends Parser<SentenceID>> parserGenerator)
			throws IOException {
		File cacheDirectory = getCacheDirectory();
		FileBasedProperties cache = new FileBasedProperties(new File(
				cacheDirectory, filePath), false);

		logger.info("Retrieving Japanese from git repository "
				+ projectDirectory + "...");
		Repository repo = Git.open(projectDirectory).getRepository();
		RevWalk revWalk = new RevWalk(repo);
		revWalk.sort(RevSort.TOPO, true);
		revWalk.sort(RevSort.REVERSE, true);
		revWalk.setTreeFilter(PathFilter.create(filePath));
		ObjectId headId = repo.resolve(Constants.HEAD);
		RevCommit headCommit = revWalk.parseCommit(headId);
		revWalk.markStart(headCommit);
		try {
			Iterator<RevCommit> commitIterator = revWalk.iterator();
			while (commitIterator.hasNext() && !originalsToRetrieve.isEmpty()) {
				RevCommit commit = commitIterator.next();
				logger.info("Checking commit " + commit.getId().getName()
						+ "...");
				TreeWalk treeWalk = new TreeWalk(repo);
				try {
					treeWalk.addTree(commit.getTree());
					treeWalk.setRecursive(true);
					treeWalk.setFilter(PathFilter.create(filePath));
					if (!treeWalk.next()) {
						logger.warning("File not found in commit " + commit
								+ ": " + filePath);
					} else {
						logger.info("GC...");
						System.gc();

						logger.info("Retrieving file content...");
						ObjectLoader loader = repo
								.open(treeWalk.getObjectId(0));
						ByteArrayOutputStream out = new ByteArrayOutputStream(
								(int) loader.getSize());
						loader.copyTo(out);

						logger.info("Parsing file content...");
						Parser<SentenceID> oldParsed = parserGenerator
								.generates();
						oldParsed.setContent(out.toString());

						logger.info("Browsing file content...");
						Iterator<Entry<SentenceID, Storage>> entryIterator = originalsToRetrieve
								.entrySet().iterator();
						int total = originalsToRetrieve.size();
						int count = 0;
						while (entryIterator.hasNext()) {
							Entry<SentenceID, Storage> entry = entryIterator
									.next();
							SentenceID sentenceID = entry.getKey();
							logger.finest("Searching Japanese for "
									+ sentenceID + "...");
							Sentence sentence = oldParsed
									.getSentence(sentenceID);
							if (sentence == null) {
								logger.finest("Japanese not found for "
										+ sentenceID);
							} else {
								entry.getValue().write(sentence.getContent());
								cache.setProperty(sentenceID.toString(),
										sentence.getContent());
								entryIterator.remove();
								logger.finest("Japanese retrieved for "
										+ sentenceID);
								count++;
							}
						}
						logger.info("Japanese retrieved: " + count + "/"
								+ total);
						if (count > 0) {
							cache.save();
						} else {
							// nothing to save
						}
					}
				} finally {
					treeWalk.close();
				}
			}
		} finally {
			revWalk.close();
		}
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

	private static File getCacheDirectory() {
		File cacheDirectory = new File("JapCache");
		if (cacheDirectory.isDirectory()) {
			// already created
		} else if (cacheDirectory.mkdir()) {
			// just created
		} else {
			throw new RuntimeException(
					"Impossible to create the cache directory "
							+ cacheDirectory);
		}
		return cacheDirectory;
	}
}
