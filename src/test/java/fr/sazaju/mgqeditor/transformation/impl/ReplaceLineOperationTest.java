package fr.sazaju.mgqeditor.transformation.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;

public class ReplaceLineOperationTest {

	@Test
	public void testAppliedReplacementProvideCorrectContent() {
		Version original = Version.from(Arrays.asList(
				"   ShowMessageFace(\"\",0,0,2,20)", "   ShowMessage(\"テスト\")",
				"   ShowMessageFace(\"\",0,0,2,21)"));

		ReplaceLineOperation replacement = new ReplaceLineOperation(1,
				"   ShowMessageFace(\"\",0,0,2,10)");
		Version translated = replacement.transform(original);

		assertEquals("   ShowMessageFace(\"\",0,0,2,20)", translated.get(0));
		assertEquals("   ShowMessageFace(\"\",0,0,2,10)", translated.get(1));
		assertEquals("   ShowMessageFace(\"\",0,0,2,21)", translated.get(2));
	}

	@Test
	public void testAppliedReplacementDoesNotAffectOriginal() {
		Version original = Version.from(Arrays.asList(
				"   ShowMessageFace(\"\",0,0,2,20)", "   ShowMessage(\"テスト\")",
				"   ShowMessageFace(\"\",0,0,2,21)"));

		ReplaceLineOperation replacement = new ReplaceLineOperation(1,
				"   ShowMessageFace(\"\",0,0,2,10)");
		replacement.transform(original);

		assertEquals("   ShowMessageFace(\"\",0,0,2,20)", original.get(0));
		assertEquals("   ShowMessage(\"テスト\")", original.get(1));
		assertEquals("   ShowMessageFace(\"\",0,0,2,21)", original.get(2));
	}

	@Test
	public void testReplacementOnTooShortContentThrowsException() {
		Version original = Version.from(Arrays.asList(
				"   ShowMessageFace(\"\",0,0,2,20)", "   ShowMessage(\"テスト\")",
				"   ShowMessageFace(\"\",0,0,2,21)"));

		ReplaceLineOperation replacement = new ReplaceLineOperation(50,
				"   ShowMessageFace(\"\",0,0,2,10)");
		try {
			replacement.transform(original);
			fail("No exception thrown");
		} catch (InvalidOperationException e) {
			// OK
		}
	}

	@Test
	public void testNullReplacementThrowsException() {
		try {
			new ReplaceLineOperation(1, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			// OK
		}
	}

	@Test
	public void testReplacementOnNegativeLineThrowsException() {
		try {
			new ReplaceLineOperation(-1, "   ShowMessageFace(\"\",0,0,2,10)");
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}
}
