package fr.sazaju.mgqeditor.transformation.impl;

import java.util.List;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.parser.regex.Events.ChoiceLine;
import fr.sazaju.mgqeditor.transformation.OperationBuilder;
import fr.vergne.parsing.layer.exception.ParsingException;

public class TranslateChoicesOperationBuilder implements OperationBuilder {

	@Override
	public String getOperationName() {
		return "Translate choices";
	}

	@Override
	public TargetSelector createOperationToTransform(Version currentVersion, int start) throws InvalidSourceException {
		return new TargetSelector() {

			@Override
			public OperationDescriptor into(Version expectedVersion) throws InvalidTargetException {
				if (start >= expectedVersion.size()) {
					throw new InvalidTargetException("the expected result has less lines: " + expectedVersion.size());
				} else {
					List<String> translatedChoices;
					try {
						translatedChoices = new ChoiceLine(expectedVersion.get(start)).getChoices();
					} catch (ParsingException e) {
						throw new InvalidTargetException(
								"the expected line is not a choice: " + expectedVersion.get(start).trim());
					}
					return new OperationDescriptor() {

						@Override
						public int getValidUntil() {
							return start;
						}

						@Override
						public Operation getOperation() {
							return new TranslateChoicesOperation(start, translatedChoices);
						}
					};
				}
			}
		};
	}
}
