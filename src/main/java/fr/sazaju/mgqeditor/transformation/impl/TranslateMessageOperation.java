package fr.sazaju.mgqeditor.transformation.impl;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.parser.regex.Events.MessageLine;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.Operation;
import fr.vergne.parsing.layer.exception.ParsingException;

public class TranslateMessageOperation implements Operation {

	private final int index;
	private final String translation;

	public TranslateMessageOperation(int index, String translation) {
		if (index < 0) {
			throw new IndexOutOfBoundsException(
					"The translation must be applied on a positive line, not "
							+ index);
		} else if (translation == null) {
			throw new NullPointerException("No translation provided");
		} else {
			this.index = index;
			this.translation = translation;
		}
	}

	@Override
	public Version transform(Version source) throws InvalidOperationException {
		if (source.size() <= index) {
			throw new InvalidOperationException(
					"The translation cannot be applied on line " + (index + 1)
							+ ": the content only has " + source.size()
							+ " lines");
		} else if (!isMessage(source.get(index))) {
			throw new InvalidOperationException(
					"This line cannot be translated as a message: "
							+ source.get(index));
		} else {
			return new Version(source, (src, i) -> {
				if (i == index) {
					MessageLine line = new MessageLine(src.get(i));
					line.setMessage(translation);
					return line.getContent();
				} else {
					return src.get(i);
				}
			});
		}
	}

	public static boolean isMessage(String content) {
		try {
			new MessageLine(content);
			return true;
		} catch (ParsingException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Translate line " + index + " with message \"" + translation
				+ "\"";
	}
}
