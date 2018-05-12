package fr.sazaju.mgqeditor.transformation.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;

public class TranslateChoicesOperationTest {

	@Test
	public void testAppliedTranslationProvidesCorrectContent() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   ShowChoices(strings(\"ア\",\"イ\",\"ウ\"),0)",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateChoicesOperation translation = new TranslateChoicesOperation(
				1, Arrays.asList("A", "B", "C"));
		Version translated = translation.transform(original);

		assertEquals("   ShowChoiceFace(\"\",0,0,2,20)", translated.get(0));
		assertEquals("   ShowChoices(strings(\"A\",\"B\",\"C\"),0)", translated.get(1));
		assertEquals("   ShowChoiceFace(\"\",0,0,2,21)", translated.get(2));
	}

	@Test
	public void testAppliedTranslationDoesNotAffectOriginal() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   ShowChoices(strings(\"ア\",\"イ\",\"ウ\"),0)",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateChoicesOperation translation = new TranslateChoicesOperation(
				1, Arrays.asList("A", "B", "C"));
		translation.transform(original);

		assertEquals("   ShowChoiceFace(\"\",0,0,2,20)", original.get(0));
		assertEquals("   ShowChoices(strings(\"ア\",\"イ\",\"ウ\"),0)",
				original.get(1));
		assertEquals("   ShowChoiceFace(\"\",0,0,2,21)", original.get(2));
	}

	@Test
	public void testTranslationOnInvalidLineThrowsException() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   ShowChoices(strings(\"ア\",\"イ\",\"ウ\"),0)",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateChoicesOperation translation = new TranslateChoicesOperation(
				0, Arrays.asList("A", "B", "C"));
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
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   ShowChoices(strings(\"ア\",\"イ\",\"ウ\"),0)",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateChoicesOperation translation = new TranslateChoicesOperation(
				50, Arrays.asList("A", "B", "C"));
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
			new TranslateChoicesOperation(1, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			// OK
		}
	}

	@Test
	public void testTranslationOnNegativeLineThrowsException() {
		try {
			new TranslateChoicesOperation(-1, Arrays.asList("A", "B", "C"));
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}

	@Test
	public void testTranslationWithTooMuchChoicesThrowsException() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   ShowChoices(strings(\"ア\",\"イ\",\"ウ\"),0)",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateChoicesOperation translation = new TranslateChoicesOperation(
				1, Arrays.asList("A", "B", "C", "D"));
		try {
			translation.transform(original);
			fail("No exception thrown");
		} catch (InvalidOperationException e) {
			// OK
		}
	}

	@Test
	public void testTranslationWithTooFewChoicesThrowsException() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   ShowChoices(strings(\"ア\",\"イ\",\"ウ\"),0)",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateChoicesOperation translation = new TranslateChoicesOperation(
				1, Arrays.asList("A", "B"));
		try {
			translation.transform(original);
			fail("No exception thrown");
		} catch (InvalidOperationException e) {
			// OK
		}
	}
}
