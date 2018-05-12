package fr.sazaju.mgqeditor.fix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import fr.sazaju.mgqeditor.parser.regex.Actors;
import fr.sazaju.mgqeditor.parser.regex.Actors.Actor;

public class ActorsNameFix {

	private static final String CHARSET = "UTF8";

	public static void main(String[] args) throws IOException {
		File projectDirectory = new File("MGQ-Dargoth");
		File actorFile = new File(projectDirectory, "Actors.txt");
		Actors actors = new Actors();
		actors.setContent(FileUtils.readFileToString(actorFile, CHARSET));

		List<File> files = new LinkedList<>();
		files.add(projectDirectory);
		while (!files.isEmpty()) {
			File file = files.remove(0);
			if (file.getName().startsWith(".")) {
				// ignore hidden files
			} else if (file.isDirectory()) {
				files.addAll(Arrays.asList(file.listFiles()));
			} else if (file.equals(actorFile)) {
				// ignore actor file
			} else if (file.getName().endsWith(".txt")) {
				replaceIntegerIDs(file, actors);
			} else {
				// ignore non-text files
			}
		}
	}

	private static void replaceIntegerIDs(File file, Actors actors)
			throws IOException {
		System.out.println(file);
		String content = FileUtils.readFileToString(file, CHARSET);
		Matcher matcher = Pattern.compile("\\\\\\\\n\\[([0-9]+)\\]").matcher(
				content);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			int id = Integer.parseInt(matcher.group(1));
			Actor actor = actors.getActor(id);
			String name = actor.getField("Name");
			matcher.appendReplacement(buffer, name);
		}
		matcher.appendTail(buffer);
		FileUtils.write(file, buffer.toString(), CHARSET);
		System.out.println("OK");
	}
}
