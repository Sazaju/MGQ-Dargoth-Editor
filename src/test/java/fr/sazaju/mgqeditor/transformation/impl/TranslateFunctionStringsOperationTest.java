package fr.sazaju.mgqeditor.transformation.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;

public class TranslateFunctionStringsOperationTest {

	@Test
	public void testAppliedTranslationProvidesCorrectContent() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")",
				"   655(\"\\\"Add to party\\\",\")",
				"   655(\"\\\"View present list\\\",\\\"Request\\\",\")",
				"   655(\"\\\"Never mind\\\"])\")",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateFunctionStringsOperation translation = new TranslateFunctionStringsOperation(1,
				Arrays.asList("A", "B", "C", "D", "E"));
		Version translated = translation.transform(original);

		assertEquals("   ShowChoiceFace(\"\",0,0,2,20)", translated.get(0));
		assertEquals("   355(\"unlimited_choices(11, [\\\"A\\\",\")", translated.get(1));
		assertEquals("   655(\"\\\"B\\\",\")", translated.get(2));
		assertEquals("   655(\"\\\"C\\\",\\\"D\\\",\")", translated.get(3));
		assertEquals("   655(\"\\\"E\\\"])\")", translated.get(4));
		assertEquals("   ShowChoiceFace(\"\",0,0,2,21)", translated.get(5));
	}
	
	@Test
	public void testAppliedTranslationDoesNotAffectOriginal() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")",
				"   655(\"\\\"Add to party\\\",\")",
				"   655(\"\\\"View present list\\\",\\\"Request\\\",\")",
				"   655(\"\\\"Never mind\\\"])\")",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateFunctionStringsOperation translation = new TranslateFunctionStringsOperation(1,
				Arrays.asList("A", "B", "C", "D", "E"));
		translation.transform(original);

		assertEquals("   ShowChoiceFace(\"\",0,0,2,20)", original.get(0));
		assertEquals("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")", original.get(1));
		assertEquals("   655(\"\\\"Add to party\\\",\")", original.get(2));
		assertEquals("   655(\"\\\"View present list\\\",\\\"Request\\\",\")", original.get(3));
		assertEquals("   655(\"\\\"Never mind\\\"])\")", original.get(4));
		assertEquals("   ShowChoiceFace(\"\",0,0,2,21)", original.get(5));
	}

	@Test
	public void testTranslationOnInvalidLineThrowsException() {
		// TODO Manage partial cases too
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")",
				"   655(\"\\\"Add to party\\\",\")",
				"   655(\"\\\"View present list\\\",\\\"Request\\\",\")",
				"   655(\"\\\"Never mind\\\"])\")",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateFunctionStringsOperation translation = new TranslateFunctionStringsOperation(0,
				Arrays.asList("A", "B", "C"));
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
				"   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")",
				"   655(\"\\\"Add to party\\\",\")",
				"   655(\"\\\"View present list\\\",\\\"Request\\\",\")",
				"   655(\"\\\"Never mind\\\"])\")",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateFunctionStringsOperation translation = new TranslateFunctionStringsOperation(50,
				Arrays.asList("A", "B", "C", "D"));
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
			new TranslateFunctionStringsOperation(1, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			// OK
		}
	}

	@Test
	public void testTranslationOnNegativeLineThrowsException() {
		try {
			new TranslateFunctionStringsOperation(-1, Arrays.asList("A", "B", "C"));
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}

	@Test
	public void testTranslationWithTooMuchFunctionStringsThrowsException() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")",
				"   655(\"\\\"Add to party\\\",\")",
				"   655(\"\\\"View present list\\\",\\\"Request\\\",\")",
				"   655(\"\\\"Never mind\\\"])\")",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateFunctionStringsOperation translation = new TranslateFunctionStringsOperation(1,
				Arrays.asList("A", "B", "C", "D", "E", "F", "G"));
		try {
			translation.transform(original);
			fail("No exception thrown");
		} catch (InvalidOperationException e) {
			// OK
		}
	}

	@Test
	public void testTranslationWithTooFewFunctionStringsThrowsException() {
		Version original = Version.from(Arrays.asList(
				"   ShowChoiceFace(\"\",0,0,2,20)",
				"   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")",
				"   655(\"\\\"Add to party\\\",\")",
				"   655(\"\\\"View present list\\\",\\\"Request\\\",\")",
				"   655(\"\\\"Never mind\\\"])\")",
				"   ShowChoiceFace(\"\",0,0,2,21)"));

		TranslateFunctionStringsOperation translation = new TranslateFunctionStringsOperation(1,
				Arrays.asList("A", "B"));
		try {
			translation.transform(original);
			fail("No exception thrown");
		} catch (InvalidOperationException e) {
			// OK
		}
	}
}
