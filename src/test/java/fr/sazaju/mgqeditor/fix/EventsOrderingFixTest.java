package fr.sazaju.mgqeditor.fix;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.sazaju.mgqeditor.util.LoggerUtil;

public class EventsOrderingFixTest {

	private static final String CHARSET = "UTF8";
	private final File testFolder = new File("src/test/resources");

	@BeforeClass
	public static void configureLogging() {
		LoggerUtil.LoadLoggingProperties();
	}

	@Test
	public void testEventsWithoutSeparatingLinesGetSeparated() throws IOException {
		String source = FileUtils.readFileToString(new File(testFolder, "Normalize-missing-before.txt"), CHARSET);
		String expected = FileUtils.readFileToString(new File(testFolder, "Normalize-missing-after.txt"), CHARSET);
		String actual = EventsOrderingFix.normalizeSeparatingLines(source);
		assertEquals(expected, actual);
	}

	@Test
	public void testEventsWithTooMuchSeparatingLinesGetReducedToOneSeparatingLine() throws IOException {
		String source = FileUtils.readFileToString(new File(testFolder, "Normalize-tooMuch-before.txt"), CHARSET);
		String expected = FileUtils.readFileToString(new File(testFolder, "Normalize-tooMuch-after.txt"), CHARSET);
		String actual = EventsOrderingFix.normalizeSeparatingLines(source);
		assertEquals(expected, actual);
	}

}
