package fr.sazaju.mgqeditor.transformation.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;

public class TranslateMessageOperationTest {

	@Test
	public void testAppliedTranslationProvideCorrectContent() {
		Version original = Version.from(Arrays.asList(
				"   ShowMessageFace(\"\",0,0,2,20)", "   ShowMessage(\"テスト\")",
				"   ShowMessageFace(\"\",0,0,2,21)"));

		TranslateMessageOperation translation = new TranslateMessageOperation(
				1, "Test");
		Version translated = translation.transform(original);

		assertEquals("   ShowMessageFace(\"\",0,0,2,20)", translated.get(0));
		assertEquals("   ShowMessage(\"Test\")", translated.get(1));
		assertEquals("   ShowMessageFace(\"\",0,0,2,21)", translated.get(2));
	}

	@Test
	public void testAppliedTranslationDoesNotAffectOriginal() {
		Version original = Version.from(Arrays.asList(
				"   ShowMessageFace(\"\",0,0,2,20)", "   ShowMessage(\"テスト\")",
				"   ShowMessageFace(\"\",0,0,2,21)"));

		TranslateMessageOperation translation = new TranslateMessageOperation(
				1, "Test");
		translation.transform(original);

		assertEquals("   ShowMessageFace(\"\",0,0,2,20)", original.get(0));
		assertEquals("   ShowMessage(\"テスト\")", original.get(1));
		assertEquals("   ShowMessageFace(\"\",0,0,2,21)", original.get(2));
	}

	@Test
	public void testTranslationOnInvalidLineThrowsException() {
		Version original = Version.from(Arrays.asList(
				"   ShowMessageFace(\"\",0,0,2,20)", "   ShowMessage(\"テスト\")",
				"   ShowMessageFace(\"\",0,0,2,21)"));

		TranslateMessageOperation translation = new TranslateMessageOperation(
				0, "Test");
		try {
			translation.transform(original);
			fail("No exception thrown");
		} catch (InvalidOperationException e) {
			// OK
		}
	}

	@Test
	public void testTranslationOnTooShortContentThrowsException() {
		Version original = Version.from(Arrays.asList(
				"   ShowMessageFace(\"\",0,0,2,20)", "   ShowMessage(\"テスト\")",
				"   ShowMessageFace(\"\",0,0,2,21)"));

		TranslateMessageOperation translation = new TranslateMessageOperation(
				50, "Test");
		try {
			translation.transform(original);
			fail("No exception thrown");
		} catch (InvalidOperationException e) {
			// OK
		}
	}

	@Test
	public void testNullTranslationThrowsException() {
		try {
			new TranslateMessageOperation(1, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			// OK
		}
	}

	@Test
	public void testTranslationOnNegativeLineThrowsException() {
		try {
			new TranslateMessageOperation(-1, "Test");
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}
}
