package fr.sazaju.mgqeditor.transformation.impl;

import java.util.List;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.parser.regex.Events.ChoiceLine;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.Operation;
import fr.vergne.parsing.layer.exception.ParsingException;

public class TranslateChoicesOperation implements Operation {

	private final int index;
	private final List<String> translations;

	public TranslateChoicesOperation(int index, List<String> translations) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("The translations must be applied on a positive line, not " + index);
		} else if (translations == null) {
			throw new NullPointerException(
					"No translation provided. Use an empty list if the choices are empty, or simply preserve the line as is.");
		} else {
			this.index = index;
			this.translations = translations;
		}
	}

	@Override
	public Version transform(Version source) throws InvalidOperationException {
		if (source.size() <= index) {
			throw new InvalidOperationException("The translations cannot be applied on line " + (index + 1)
					+ ": the content only has " + source.size() + " lines");
		} else if (!isChoice(source.get(index))) {
			throw new InvalidOperationException("This line cannot be translated as choices: " + source.get(index));
		} else if (new ChoiceLine(source.get(index)).size() != translations.size()) {
			throw new InvalidOperationException(
					"This line cannot be translated with " + translations.size() + " choices: " + source.get(index));
		} else {
			return new Version(source, (src, i) -> {
				if (i == index) {
					ChoiceLine line = new ChoiceLine(src.get(i));
					line.setChoices(translations);
					return line.getContent();
				} else {
					return src.get(i);
				}
			});
		}
	}

	public static boolean isChoice(String content) {
		try {
			new ChoiceLine(content);
			return true;
		} catch (ParsingException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Translate line " + index + " with choices " + translations;
	}
}
