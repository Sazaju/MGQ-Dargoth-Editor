package fr.sazaju.mgqeditor.transformation.impl;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.parser.regex.Events.MessageLine;
import fr.sazaju.mgqeditor.transformation.OperationBuilder;

public class MergeMessageLinesOperationBuilder implements OperationBuilder {

	@Override
	public String getOperationName() {
		return "Merge";
	}

	@Override
	public TargetSelector createOperationToTransform(Version currentVersion, int start) throws InvalidSourceException {
		if (start < 0) {
			throw new IndexOutOfBoundsException("The translation must be applied on a positive line, not " + start);
		} else {
			if (currentVersion.size() <= start + 1) {
				throw new InvalidSourceException("The merging cannot be applied on line " + (start + 1)
						+ ": the content only has " + currentVersion.size() + " lines");
			} else if (!TranslateMessageOperation.isMessage(currentVersion.get(start))) {
				throw new InvalidSourceException(
						"The current line is not a message: " + currentVersion.get(start).trim());
			} else if (!TranslateMessageOperation.isMessage(currentVersion.get(start + 1))) {
				throw new InvalidSourceException(
						"The next line is not a message: " + currentVersion.get(start + 1).trim());
			} else {
				return new TargetSelector() {

					@Override
					public OperationDescriptor into(Version expectedVersion) throws InvalidTargetException {
						if (expectedVersion.size() <= start) {
							throw new InvalidTargetException("The merging cannot reach the target, which only has "
									+ expectedVersion.size() + " lines");
						} else if (!TranslateMessageOperation.isMessage(expectedVersion.get(start))) {
							throw new InvalidTargetException(
									"The target line is not a message: " + expectedVersion.get(start).trim());
						} else {
							return new OperationDescriptor() {

								@Override
								public int getValidUntil() {
									return start - 1;
								}

								@Override
								public Operation getOperation() {
									return new Operation() {

										@Override
										public Version transform(Version source) throws InvalidOperationException {
											try {
												createOperationToTransform(source, start);
											} catch (InvalidSourceException cause) {
												throw new InvalidOperationException(cause);
											}
											return new Version(source, (src, i) -> {
												if (i == start) {
													MessageLine line = new MessageLine(src.get(i));
													MessageLine nextLine = new MessageLine(src.get(i + 1));
													String content = line.getMessage() + nextLine.getMessage();
													line.setMessage(content);
													return line.getContent();
												} else if (i > start) {
													return src.get(i + 1);
												} else {
													return src.get(i);
												}
											});
										}

										@Override
										public String toString() {
											return "Merge lines " + start + "-" + (start + 1);
										}
									};
								}
							};
						}
					}
				};
			}
		}
	}
}
