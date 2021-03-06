package fr.sazaju.mgqeditor;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import fr.sazaju.mgqeditor.util.Saver;
import fr.sazaju.mgqeditor.util.Storage;
import fr.vergne.translation.TranslationEntry;

public class MGQEntry implements TranslationEntry<MGQMetadata> {

	private static final Logger logger = Logger.getLogger(MGQEntry.class
			.getName());
	private final Storage originalStorage;
	private String translation;
	private final Collection<TranslationListener> listeners = new HashSet<>();
	private final Storage translationStorage;
	private final Saver saver;
	private final MGQMetadata metadata;

	public MGQEntry(Storage originalStorage, Storage translationStorage,
			Saver saver) {
		this.originalStorage = originalStorage;
		this.translationStorage = translationStorage;
		this.translation = translationStorage.read();
		this.saver = saver;
		this.metadata = new MGQMetadata();
	}

	@Override
	public String getOriginalContent() {
		return originalStorage.read();
	}

	@Override
	public String getStoredTranslation() {
		return translationStorage.read();
	}

	@Override
	public String getCurrentTranslation() {
		return translation;
	}

	@Override
	public void setCurrentTranslation(String translation) {
		this.translation = translation;
		for (TranslationListener listener : listeners) {
			listener.translationUpdated(translation);
		}
	}

	private void writeTranslation() {
		if (translationStorage.read().equals(translation)) {
			// nothing to update
		} else {
			translationStorage.write(translation);
		}
	}

	@Override
	public void saveTranslation() {
		writeTranslation();
		saver.save();
		for (TranslationListener listener : listeners) {
			listener.translationStored();
		}
	}

	@Override
	public void resetTranslation() {
		setCurrentTranslation(getStoredTranslation());
	}

	public void writeAll() {
		writeTranslation();
		// TODO write metadata too
	}

	@Override
	public void saveAll() {
		writeAll();
		saver.save();
		for (TranslationListener listener : listeners) {
			listener.translationStored();
		}
	}

	@Override
	public void resetAll() {
		resetTranslation();
		metadata.resetAll();
	}

	@Override
	public MGQMetadata getMetadata() {
		return metadata;
	}

	@Override
	public void addTranslationListener(TranslationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTranslationListener(TranslationListener listener) {
		listeners.remove(listener);
	}

	@Override
	public String toString() {
		return originalStorage + " [=>] " + translation;
	}
}
