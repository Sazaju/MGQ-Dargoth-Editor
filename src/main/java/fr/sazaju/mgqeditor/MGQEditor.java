package fr.sazaju.mgqeditor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import fr.sazaju.mgqeditor.util.LoggerUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import fr.vergne.translation.editor.Editor;
import fr.vergne.translation.util.ProjectLoader;
import fr.vergne.translation.util.Setting;
import fr.vergne.translation.util.Switcher;
import fr.vergne.translation.util.impl.PropertyFileSetting;

@SuppressWarnings("serial")
public class MGQEditor extends Editor<MapID, MGQEntry, MGQMap, MGQProject> {

	private static final Logger logger = Logger.getLogger(MGQEditor.class.getName());

	public MGQEditor(Setting<? super String> settings) {
		super(new ProjectLoader<MGQProject>() {

			@Override
			public MGQProject load(File directory) {
				return new MGQProject(directory);
			}
		}, settings);
	}

	public static void main(String[] args) {
		LoggerUtil.LoadLoggingProperties();
		new Thread(new Runnable() {
			public void run() {
				try {
					PropertyFileSetting settings = new PropertyFileSetting(new File("mgq-editor.ini"));
					settings.setFutureSwitcher("mapDir", Switcher.identity());
					settings.setFutureSwitcher("filter", Switcher.identity());
					settings.setFutureSwitcher("remainingFilter", Switcher.identity());
					settings.setFutureSwitcher("mapNamer", Switcher.identity());
					new MGQEditor(settings).setVisible(true);
				} catch (Exception e) {
					logger.log(Level.SEVERE, null, e);
				}
			}
		}).start();
	}
}
