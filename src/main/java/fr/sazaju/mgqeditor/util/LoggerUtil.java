package fr.sazaju.mgqeditor.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.LogManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class LoggerUtil {

	private static final String CHARSET = "UTF-8";

	public static void LoadLoggingProperties() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream printer = new PrintStream(stream);
		printer.println(".level = INFO");
		printer.println("java.level = OFF");
		printer.println("javax.level = OFF");
		printer.println("sun.level = OFF");

		printer.println("handlers = java.util.logging.FileHandler, java.util.logging.ConsoleHandler");
		printer.println("formatters = java.util.logging.SimpleFormatter");
		printer.println(
				"java.util.logging.SimpleFormatter.format = %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s: %5$s [%2$s]%6$s%n");

		printer.println("java.util.logging.FileHandler.pattern = mgq-editor.%u.%g.log");
		printer.println("java.util.logging.FileHandler.level = ALL");
		printer.println("java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter");

		printer.println("java.util.logging.ConsoleHandler.level = ALL");

		File file = new File("logging.properties");
		if (file.exists()) {
			try {
				printer.println(FileUtils.readFileToString(file, CHARSET));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			// use only default configuration
		}
		printer.close();

		LogManager manager = LogManager.getLogManager();
		try {
			manager.readConfiguration(
					IOUtils.toInputStream(new String(stream.toByteArray(), Charset.forName(CHARSET)), CHARSET));
		} catch (SecurityException | IOException e) {
			throw new RuntimeException(e);
		}

	}
}
