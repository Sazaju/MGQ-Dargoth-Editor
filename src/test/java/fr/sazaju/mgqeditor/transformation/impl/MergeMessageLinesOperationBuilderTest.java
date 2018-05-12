package fr.sazaju.mgqeditor.transformation.impl;

import java.util.Arrays;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.transformation.OperationBuilder;
import fr.sazaju.mgqeditor.transformation.OperationBuilderTest;
import fr.vergne.heterogeneousmap.HeterogeneousMap;

@RunWith(JUnitPlatform.class)
public class MergeMessageLinesOperationBuilderTest implements OperationBuilderTest {

	private static final Version minimalSource = Version
			.from(Arrays.asList("ShowMessage(\"Test\")", "ShowMessage(\"ing\")"));
	private static final Version enrichedSource = Version.from(Arrays.asList("ShowMessageFace(\"\",0,0,2,20)",
			"ShowMessage(\"Test\")", "ShowMessage(\"ing\")", "ShowMessageFace(\"\",0,0,2,21)"));
	private static final Version longSource = Version
			.from(Arrays.asList("ShowMessageFace(\"\",0,0,2,20)", "ShowMessage(\"T\")", "ShowMessage(\"e\")",
					"ShowMessage(\"s\")", "ShowMessage(\"t\")", "ShowMessageFace(\"\",0,0,2,21)"));

	@Override
	public OperationBuilder createBuilder(HeterogeneousMap context) {
		return new MergeMessageLinesOperationBuilder();
	}

	@Override
	public Iterable<Version> createValidSources(HeterogeneousMap context) {
		return Arrays.asList(minimalSource, enrichedSource, longSource);
	}

	@Override
	public Iterable<Integer> getValidStartsFor(Version source, HeterogeneousMap context) {
		if (source.equals(minimalSource)) {
			return Arrays.asList(0);
		} else if (source.equals(enrichedSource)) {
			return Arrays.asList(1);
		} else if (source.equals(longSource)) {
			return Arrays.asList(1, 2, 3);
		} else {
			throw new RuntimeException("Unmanaged source: " + source);
		}
	}

	@Override
	public Iterable<Version> createValidTargetsFor(Version source, int start, HeterogeneousMap context) {
		if (source.equals(minimalSource)) {
			return Arrays.asList(Version.from(Arrays.asList("ShowMessage(\"Testing\")")));
		} else if (source.equals(enrichedSource)) {
			return Arrays.asList(Version.from(Arrays.asList("ShowMessageFace(\"\",0,0,2,20)",
					"ShowMessage(\"Testing\")", "ShowMessageFace(\"\",0,0,2,21)")));
		} else if (source.equals(longSource) && start == 1) {
			return Arrays.asList(Version.from(Arrays.asList("ShowMessageFace(\"\",0,0,2,20)", "ShowMessage(\"Te\")",
					"ShowMessage(\"s\")", "ShowMessage(\"t\")", "ShowMessageFace(\"\",0,0,2,21)")));
		} else if (source.equals(longSource) && start == 2) {
			return Arrays.asList(Version.from(Arrays.asList("ShowMessageFace(\"\",0,0,2,20)", "ShowMessage(\"T\")",
					"ShowMessage(\"es\")", "ShowMessage(\"t\")", "ShowMessageFace(\"\",0,0,2,21)")));
		} else if (source.equals(longSource) && start == 3) {
			return Arrays.asList(Version.from(Arrays.asList("ShowMessageFace(\"\",0,0,2,20)", "ShowMessage(\"T\")",
					"ShowMessage(\"e\")", "ShowMessage(\"st\")", "ShowMessageFace(\"\",0,0,2,21)")));
		} else {
			throw new RuntimeException("No target if starts at " + start + " for source:\n" + source);
		}
	}
}
