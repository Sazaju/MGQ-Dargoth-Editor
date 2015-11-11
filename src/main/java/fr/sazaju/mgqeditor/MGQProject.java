package fr.sazaju.mgqeditor;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.Feature;
import fr.vergne.translation.util.MapNamer;

public class MGQProject implements TranslationProject<MGQEntry, MapID, MGQMap> {

	private final Map<MapID, WeakReference<MGQMap>> mapCache = new HashMap<>();
	private static final Logger logger = Logger.getLogger(MGQProject.class
			.getName());
	private final File directory;

	public MGQProject(File directory) {
		this.directory = directory;
	}

	@Override
	public Iterator<MapID> iterator() {
		LinkedList<MapID> files = new LinkedList<>();
		// TODO consider all the files
		File scriptDirectory = new File(directory, "Scripts");
		for (File file : scriptDirectory.listFiles()) {
			if (file.getName().matches("Script_0+(197)_[^.]*.txt")) {
				files.add(new MapID(file));
			} else {
				// not a file to translate
			}
		}
		return files.iterator();
	}

	@Override
	public Collection<MapNamer<MapID>> getMapNamers() {
		MapNamer<MapID> namer = new MapNamer<MapID>() {

			@Override
			public String getNameFor(MapID id) {
				return id.getFile().getName();
			}

			@Override
			public String getName() {
				return "File Name";
			}

			@Override
			public String getDescription() {
				return "Use the name of the file.";
			}
		};
		return Arrays.asList(namer);
	}

	@Override
	public int size() {
		Iterator<MapID> iterator = iterator();
		int size = 0;
		while (iterator.hasNext()) {
			size++;
		}
		return size;
	}

	@Override
	public MGQMap getMap(MapID id) {
		WeakReference<MGQMap> reference = mapCache.get(id);
		if (reference == null || reference.get() == null) {
			try {
				logger.info("Building map " + id + "...");
				MGQMap map = new MGQMap(id);
				mapCache.put(id, new WeakReference<MGQMap>(map));
				logger.info("Map cached.");
				return map;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return reference.get();
		}
	}

	@Override
	public void saveAll() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemented yet.");
	}

	@Override
	public void resetAll() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemented yet.");
	}

	@Override
	public Collection<Feature> getFeatures() {
		return Collections.emptyList();
	}

	@Override
	public Collection<EntryFilter<MGQEntry>> getEntryFilters() {
		return Collections.emptyList();
	}

}
