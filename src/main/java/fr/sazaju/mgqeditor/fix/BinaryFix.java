package fr.sazaju.mgqeditor.fix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import fr.sazaju.mgqeditor.util.LoggerUtil;
import fr.vergne.collection.util.NaturalComparator;

public class BinaryFix {

	private static final String CHARSET = "UTF8";
	private static final Logger logger = Logger.getLogger(BinaryFix.class.getName());

	public static void main(String[] args) throws IOException {
		LoggerUtil.LoadLoggingProperties();
		File projectDirectory = new File("MGQ-Dargoth/Maps");

		List<File> files = new LinkedList<>();
		files.add(projectDirectory);
		while (!files.isEmpty()) {
			File file = files.remove(0);
			if (file.getName().startsWith(".")) {
				// ignore hidden files
			} else if (file.isDirectory()) {
				List<File> added = new ArrayList<>(Arrays.asList(file.listFiles()));
				Collections.sort(added, new NaturalComparator<>((f) -> f.getName()));
				files.addAll(added);
			} else if (file.getName().endsWith(".txt")) {
				logger.info("Check file " + file);
				String content = FileUtils.readFileToString(file, CHARSET);

				content = BinaryFix.removeBinary(content);

				FileUtils.write(file, content, CHARSET);
				logger.info("File written: " + file);
			} else {
				// ignore non-text files
			}
		}
	}

	public static String removeBinary(String content) {
		Matcher matcher = Pattern.compile("binary\"").matcher(content);
		StringBuffer buffer = new StringBuffer();
		int counter = 0;
		while (matcher.find()) {
			matcher.appendReplacement(buffer, "\"");
			counter++;
		}
		matcher.appendTail(buffer);
		logger.info("Binary removed: " + counter);
		return buffer.toString();
	}

}
