package fr.sazaju.mgqeditor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import fr.vergne.translation.editor.Editor;
import fr.vergne.translation.util.ProjectLoader;
import fr.vergne.translation.util.Setting;
import fr.vergne.translation.util.impl.IdentitySwitcher;
import fr.vergne.translation.util.impl.PropertyFileSetting;

@SuppressWarnings("serial")
public class MGQEditor extends Editor<MapID, MGQEntry, MGQMap, MGQProject> {

	private static final Logger logger = Logger.getLogger(MGQEditor.class
			.getName());

	public MGQEditor(Setting<? super String> settings) {
		super(new ProjectLoader<MGQProject>() {

			@Override
			public MGQProject load(File directory) {
				return new MGQProject(directory);
			}
		}, settings);
	}

	public static void main(String[] args) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream printer = new PrintStream(stream);
		printer.println(".level = INFO");
		printer.println("java.level = OFF");
		printer.println("javax.level = OFF");
		printer.println("sun.level = OFF");

		printer.println("handlers = java.util.logging.FileHandler, java.util.logging.ConsoleHandler");
		printer.println("formatters = java.util.logging.SimpleFormatter");
		printer.println("java.util.logging.SimpleFormatter.format = %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s: %5$s [%2$s]%6$s%n");

		printer.println("java.util.logging.FileHandler.pattern = vh-editor.%u.%g.log");
		printer.println("java.util.logging.FileHandler.level = ALL");

		printer.println("java.util.logging.ConsoleHandler.level = ALL");

		File file = new File("logging.properties");
		if (file.exists()) {
			try {
				printer.println(FileUtils.readFileToString(file));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			// use only default configuration
		}
		printer.close();

		LogManager manager = LogManager.getLogManager();
		try {
			manager.readConfiguration(IOUtils.toInputStream(new String(stream
					.toByteArray(), Charset.forName("UTF-8"))));
		} catch (SecurityException | IOException e) {
			throw new RuntimeException(e);
		}

		new Thread(new Runnable() {
			public void run() {
				try {
					PropertyFileSetting settings = new PropertyFileSetting(
							new File("mgq-editor.ini"));
					IdentitySwitcher<String> identity = new IdentitySwitcher<String>();
					settings.setFutureSwitcher("mapDir", identity);
					settings.setFutureSwitcher("filter", identity);
					settings.setFutureSwitcher("remainingFilter", identity);
					settings.setFutureSwitcher("mapNamer", identity);
					new MGQEditor(settings).setVisible(true);
				} catch (Exception e) {
					logger.log(Level.SEVERE, null, e);
				}
			}
		}).start();
	}
}
