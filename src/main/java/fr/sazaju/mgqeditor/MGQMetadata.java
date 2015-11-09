package fr.sazaju.mgqeditor;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

import fr.vergne.translation.TranslationMetadata;

public class MGQMetadata implements TranslationMetadata {

	private static final Logger logger = Logger.getLogger(MGQMetadata.class
			.getName());
	
	@Override
	public Iterator<Field<?>> iterator() {
		// TODO Auto-generated method stub
		return Collections.emptyIterator();
	}

	@Override
	public <T> T getStored(Field<T> field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Field<T> field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> boolean isEditable(Field<T> field) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> void set(Field<T> field, T value)
			throws UneditableFieldException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFieldListener(FieldListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeFieldListener(FieldListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void save(Field<T> field) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void reset(Field<T> field) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetAll() {
		// TODO Auto-generated method stub

	}
}
