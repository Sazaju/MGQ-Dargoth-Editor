package fr.sazaju.mgqeditor.transformation.impl;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.parser.regex.Events.MessageLine;
import fr.sazaju.mgqeditor.transformation.OperationBuilder;
import fr.vergne.parsing.layer.exception.ParsingException;

public class TranslateMessageOperationBuilder implements OperationBuilder {

	@Override
	public String getOperationName() {
		return "Translate message";
	}

	@Override
	public TargetSelector createOperationToTransform(Version currentVersion, int start) throws InvalidSourceException {
		return new TargetSelector() {

			@Override
			public OperationDescriptor into(Version expectedVersion) throws InvalidTargetException {
				if (start >= expectedVersion.size()) {
					throw new InvalidTargetException("the expected result has less lines: " + expectedVersion.size());
				} else {
					String translatedContent;
					try {
						translatedContent = new MessageLine(expectedVersion.get(start)).getMessage();
					} catch (ParsingException e) {
						throw new InvalidTargetException(
								"the expected line is not a message: " + expectedVersion.get(start).trim());
					}
					return new OperationDescriptor() {

						@Override
						public int getValidUntil() {
							return start;
						}

						@Override
						public Operation getOperation() {
							return new TranslateMessageOperation(start, translatedContent);
						}
					};
				}
			}
		};
	}
}
