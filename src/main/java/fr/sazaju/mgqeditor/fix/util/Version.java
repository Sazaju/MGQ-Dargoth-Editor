package fr.sazaju.mgqeditor.fix.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A {@link Version} is a textual container which provides a sequence of ordered
 * {@link String} instances. Each {@link String} usually corresponds to a line
 * of the whole text, such that the full text corresponds to each line, in
 * order, separated by a new line character or alike. In practice, though,
 * nothing forbids to have a new line character within a {@link String}, yet the
 * full text still corresponds to each of them separated by a new line
 * character.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 *
 */
public class Version implements Iterable<String> {

	public static interface Revision {
		public String revise(Version source, int index);
	}

	public static interface LineReader {
		public String read(int index);
	}

	private final List<String> content;

	public Version(LineReader reader, int size) {
		this.content = new LinkedList<String>();
		for (int i = 0; i < size; i++) {
			content.add(reader.read(i));
		}
	}

	public Version(Version parent, Revision revision) {
		this.content = new LinkedList<String>();
		try {
			for (int i = 0;; i++) {
				content.add(revision.revise(parent, i));
			}
		} catch (IndexOutOfBoundsException e) {
			// Everything found
		}
	}

	public String get(int index) {
		return content.get(index);
	}

	public int size() {
		return content.size();
	}

	@Override
	public Iterator<String> iterator() {
		return content.iterator();
	}

	public static Version from(List<String> strings) {
		return new Version((i) -> strings.get(i), strings.size());
	}

	public static Version fromStringRepresentationsOf(List<?> list) {
		return new Version((i) -> {
			Object item = list.get(i);
			return item == null ? null : item.toString();
		}, list.size());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Version) {
			Version v = (Version) obj;
			return v.content.equals(content);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		forEach((s) -> builder.append("\n" + s));
		return builder.toString().substring(1);
	}

	public List<String> toList() {
		return StreamSupport.stream(this.spliterator(), false).collect(Collectors.toList());
	}
}
