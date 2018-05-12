package fr.sazaju.mgqeditor.fix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import fr.vergne.parsing.layer.util.Newline;

public class NewlinesFix {

	public static void main(String[] args) throws IOException {
		File projectDirectory = new File("MGQ-Dargoth");

		List<File> files = new LinkedList<>();
		files.add(projectDirectory);
		while (!files.isEmpty()) {
			File file = files.remove(0);
			if (file.getName().startsWith(".")) {
				// ignore hidden files
			} else if (file.isDirectory()) {
				files.addAll(Arrays.asList(file.listFiles()));
			} else if (file.getName().endsWith(".txt")) {
				System.out.println(file);
				String content = FileUtils.readFileToString(file, "UTF8");
				content = mergeSuccessiveLines(content);
				content = adaptActorNamesFormat(content);
				FileUtils.write(file, content, "UTF8");
				System.out.println("OK");
			} else {
				// ignore non-text files
			}
		}
	}

	private static String adaptActorNamesFormat(String content) {
		// Prefixes of spoken texts
		String prefix = "(?<=ShowMessage\\(|_word => \\[|:word_[0-9]{1,2} => \\[)";

		// Use custom name format for proper display
		content = content.replaceAll(prefix
				+ "(\")((?:【[^】]*】|\\[[^\\]]*\\]|<[^>]*>)[^\"]*\")",
				"$1\\\\\\\\n$2");
		content = content
				.replaceAll(
						prefix
								+ "(\"\\\\\\\\n)(?:【([^】]*)】|\\[([^\\]]*)\\]|<([^>]*)>)([^\"]*\")",
						"$1<$2$3$4>$5");

		// Remove newlines made obsolete due to custom format
		content = content.replaceAll(prefix
				+ "(\"\\\\\\\\n<[^>]*>)\\\\n([^\"]*\")", "$1$2");

		return content;
	}

	private static String mergeSuccessiveLines(String original)
			throws IOException {
		String nl = new Newline().getRegex();
		String replacement = original;
		do {
			original = replacement;
			replacement = original
					.replaceAll(
							"(ShowMessage\\(\"(?:\\\\\\\\n)?(?:【[^】]*】|\\[[^\\]]*\\]|<[^>]*>)[^\"]*(?!\\\\D))\"\\)"
									+ nl + "\\s*ShowMessage\\(\"([^\"]*\"\\))",
							"$1\\\\n$2");
		} while (original.length() != replacement.length());
		return replacement;
	}
}
