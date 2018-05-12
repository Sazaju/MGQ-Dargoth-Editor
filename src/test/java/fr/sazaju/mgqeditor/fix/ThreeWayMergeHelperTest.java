package fr.sazaju.mgqeditor.fix;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fr.sazaju.mgqeditor.util.LoggerUtil;

public class ThreeWayMergeHelperTest {

	private static final String CHARSET = "UTF8";
	private final File testFolder = new File("src/test/resources");
	
	@BeforeClass
	public static void configureLogging() {
		LoggerUtil.LoadLoggingProperties();
	}

	@Test
	public void testMergeTrivialTranslation() throws IOException {
		String source = FileUtils.readFileToString(new File(testFolder,
				"Merge-trivial-before.txt"), CHARSET);
		String expected = FileUtils.readFileToString(new File(testFolder,
				"Merge-trivial-after.txt"), CHARSET);
		String actual = new ThreeWayMergeHelper().fixConflicts(source);
		assertEquals(expected, actual);
	}

	@Test
	public void testMergeSplitTranslation() throws IOException {
		String source = FileUtils.readFileToString(new File(testFolder,
				"Merge-split-before.txt"), CHARSET);
		String expected = FileUtils.readFileToString(new File(testFolder,
				"Merge-split-after.txt"), CHARSET);
		String actual = new ThreeWayMergeHelper().fixConflicts(source);
		assertEquals(expected, actual);
	}

	@Test
	public void testMergeTranslationWithModifiedLines() throws IOException {
		String source = FileUtils.readFileToString(new File(testFolder,
				"Merge-changed-before.txt"), CHARSET);
		String expected = FileUtils.readFileToString(new File(testFolder,
				"Merge-changed-after.txt"), CHARSET);
		String actual = new ThreeWayMergeHelper().fixConflicts(source);
		assertEquals(expected, actual);
	}

	@Test
	public void testMergeTranslationWithModifiedStructure() throws IOException {
		String source = FileUtils.readFileToString(new File(testFolder,
				"Merge-restructured-before.txt"), CHARSET);
		String expected = FileUtils.readFileToString(new File(testFolder,
				"Merge-restructured-after.txt"), CHARSET);
		String actual = new ThreeWayMergeHelper().fixConflicts(source);
		assertEquals(expected, actual);
	}

	@Ignore
	@Test
	public void testMergeTranslationWithMoreContent() throws IOException {
		String source = FileUtils.readFileToString(new File(testFolder,
				"Merge-increased-before.txt"), CHARSET);
		String expected = FileUtils.readFileToString(new File(testFolder,
				"Merge-increased-after.txt"), CHARSET);
		String actual = new ThreeWayMergeHelper().fixConflicts(source);
		assertEquals(expected, actual);
	}

	@Ignore
	@Test
	public void testMergeTranslationWithMovedContent() throws IOException {
		String source = FileUtils.readFileToString(new File(testFolder,
				"Merge-moved-before.txt"), CHARSET);
		String expected = FileUtils.readFileToString(new File(testFolder,
				"Merge-moved-after.txt"), CHARSET);
		String actual = new ThreeWayMergeHelper().fixConflicts(source);
		assertEquals(expected, actual);
	}

}
