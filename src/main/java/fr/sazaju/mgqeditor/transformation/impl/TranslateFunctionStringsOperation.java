package fr.sazaju.mgqeditor.transformation.impl;

import java.util.List;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.parser.regex.Events.Function355And655Line;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.Operation;
import fr.vergne.parsing.layer.exception.ParsingException;

public class TranslateFunctionStringsOperation implements Operation {

	private final int index;
	private final List<String> translations;

	public TranslateFunctionStringsOperation(int index, List<String> translations) {
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
		} else {
			String regex = new Function355And655Line().getRegex();
			StringBuilder initialBuilder = new StringBuilder();
			initialBuilder.append(source.get(index));
			if (!initialBuilder.toString().matches(regex)) {
				throw new InvalidOperationException(
						"This line does not correspond to a function with strings: " + source.get(index));
			} else {
				int validUntil = index;
				String validContent = initialBuilder.toString();
				while (validUntil < source.size() - 1) {
					initialBuilder.append("\n");
					initialBuilder.append(source.get(validUntil + 1));
					String expandedContent = initialBuilder.toString();
					if (expandedContent.matches(regex)) {
						validUntil++;
						validContent = expandedContent;
					} else {
						break;
					}
				}

				Function355And655Line currentLine = new Function355And655Line(validContent);
				if (currentLine.size() != translations.size()) {
					throw new InvalidOperationException("This content cannot be translated with " + translations.size()
							+ " function strings:\n" + validContent);
				} else {
					int stop = validUntil + 1;
					return new Version(source, (src, i) -> {
						if (i >= index && i < stop) {
							StringBuilder builder = new StringBuilder(src.get(index));
							for (int j = index + 1; j < stop; j++) {
								builder.append("\n");
								builder.append(src.get(j));
							}
							Function355And655Line line = new Function355And655Line(builder.toString());
							line.setFunctionStrings(translations);
							return line.getLineContent(i - index);
						} else {
							return src.get(i);
						}
					});
				}
			}
		}
	}

	public static boolean isFunction(Version version, int indexFrom) {
		try {
			// TODO Search the lines to cover
			new Function355And655Line(version.get(indexFrom));
			return true;
		} catch (ParsingException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Translate line " + index + " with function strings " + translations;
	}
}
