package fr.sazaju.mgqeditor.transformation.impl;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.Operation;

public class ReplaceLineOperation implements Operation {

	private final int index;
	private final String replacement;

	public ReplaceLineOperation(int index, String replacement) {
		if (index < 0) {
			throw new IndexOutOfBoundsException(
					"The replacement must be applied on a positive line, not "
							+ index);
		} else if (replacement == null) {
			throw new NullPointerException("No replacement provided");
		} else {
			this.index = index;
			this.replacement = replacement;
		}
	}

	@Override
	public Version transform(Version source) throws InvalidOperationException {
		if (source.size() <= index) {
			throw new InvalidOperationException(
					"The replacement cannot be applied on line " + (index + 1)
							+ ": the content only has " + source.size()
							+ " lines");
		} else {
			Version transformed = new Version(source, (src, i) -> {
				if (i == index) {
					return replacement;
				} else {
					return src.get(i);
				}
			});
			return transformed;
		}
	}

	@Override
	public String toString() {
		return "Replace line " + index + " by \"" + replacement + "\"";
	}
}
