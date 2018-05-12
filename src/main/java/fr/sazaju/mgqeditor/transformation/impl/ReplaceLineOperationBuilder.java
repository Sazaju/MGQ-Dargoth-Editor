package fr.sazaju.mgqeditor.transformation.impl;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.transformation.OperationBuilder;

public class ReplaceLineOperationBuilder implements OperationBuilder {

	@Override
	public String getOperationName() {
		return "Replace";
	}

	@Override
	public TargetSelector createOperationToTransform(Version currentVersion, int start)
			throws InvalidSourceException {
		return new TargetSelector() {

			@Override
			public OperationDescriptor into(Version expectedVersion) throws InvalidTargetException {
				if (start >= expectedVersion.size()) {
					throw new InvalidTargetException(
							"the expected result has less lines: " + expectedVersion.size());
				} else if (currentVersion.get(start).equals(expectedVersion.get(start))) {
					throw new InvalidTargetException(
							"the expected line is identical: " + expectedVersion.get(start).trim());
				} else {
					return new OperationDescriptor() {

						@Override
						public int getValidUntil() {
							return start;
						}

						@Override
						public Operation getOperation() {
							return new ReplaceLineOperation(start, expectedVersion.get(start));
						}
					};
				}
			}
		};
	}
}
