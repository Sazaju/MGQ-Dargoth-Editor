package fr.sazaju.mgqeditor.fix;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.commons.io.FileUtils;

import fr.sazaju.mgqeditor.parser.regex.Events;
import fr.sazaju.mgqeditor.parser.regex.Events.Event;
import fr.sazaju.mgqeditor.util.LoggerUtil;
import fr.vergne.collection.util.NaturalComparator;
import fr.vergne.flatmap.FlatMapSupport;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.translation.util.Setting.SettingKey;
import fr.vergne.translation.util.Switcher;
import fr.vergne.translation.util.impl.PropertyFileSetting;

@SuppressWarnings("serial")
public class EventsOrderingFix extends JPanel {
	private static final String CHARSET = "UTF8";

	private static final Logger logger = Logger.getLogger(EventsOrderingFix.class.getName());

	private final Map<File, Boolean> mapSelection = new LinkedHashMap<>();
	private final Collection<JCheckBox> checkBoxes = new LinkedList<>();
	private final Collection<JButton> featureButtons = new LinkedList<>();

	private final JPanel mapListPanel;

	public EventsOrderingFix(PropertyFileSetting settings, SettingKey<String> mapDirSetting) {
		setLayout(new GridLayout(1, 1));
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setBorder(null);
		add(splitPane);

		mapListPanel = new JPanel();

		splitPane.setLeftComponent(createFeatures(mapListPanel, settings, mapDirSetting));
		splitPane.setRightComponent(createLogArea());
		splitPane.setResizeWeight(0);
		addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(0.7);
				removeComponentListener(this);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// Nothing to do
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// Nothing to do
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// Nothing to do
			}

		});
	}

	private JPanel createFeatures(JPanel mapListPanel, PropertyFileSetting settings, SettingKey<String> mapDirSetting) {
		JPanel features = new JPanel();
		features.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		constraints.weighty = 1;
		features.add(createProjectFolderInput(settings, mapDirSetting), constraints);

		constraints.gridy++;
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.gridwidth = 1;
		constraints.gridheight = 4;
		constraints.weightx = 0;
		constraints.weighty = 1;
		JScrollPane scrollPane = new JScrollPane(mapListPanel) {
			@Override
			public Dimension getMinimumSize() {
				Component component = viewport.getComponent(0);
				int width = super.getMinimumSize().width + component.getMinimumSize().width;
				return new Dimension(width, super.getMinimumSize().height);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 300);
			}
		};
		scrollPane.setBorder(new TitledBorder("Maps"));
		features.add(scrollPane, constraints);

		constraints.gridx++;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridheight = 1;
		constraints.weightx = 1;
		constraints.weighty = 1;
		JButton selectAllButton = new JButton(new AbstractAction("Select all") {

			@Override
			public void actionPerformed(ActionEvent e) {
				process(() -> {
					logger.info("Select all maps.");
					checkBoxes.forEach((checkBox) -> {
						checkBox.setSelected(true);
					});
					mapSelection.forEach((file, isSelected) -> mapSelection.put(file, true));
					logger.info("Maps selected.");
				});
			}
		});
		features.add(selectAllButton, constraints);
		featureButtons.add(selectAllButton);

		constraints.gridy++;
		JButton deselectAllButton = new JButton(new AbstractAction("Deselect all") {

			@Override
			public void actionPerformed(ActionEvent e) {
				process(() -> {
					logger.info("Deselect all maps.");
					checkBoxes.forEach((checkBox) -> {
						checkBox.setSelected(false);
					});
					mapSelection.forEach((file, isSelected) -> mapSelection.put(file, false));
					logger.info("Maps deselected.");
				});
			}
		});
		features.add(deselectAllButton, constraints);
		featureButtons.add(deselectAllButton);

		constraints.gridy++;
		JButton sortingButon = createEventSortingButton();
		features.add(sortingButon, constraints);
		featureButtons.add(sortingButon);

		constraints.gridy++;
		JButton normalizeButton = createNormalizationButton();
		features.add(normalizeButton, constraints);
		featureButtons.add(normalizeButton);

		return features;
	}

	private Component createProjectFolderInput(PropertyFileSetting settings, SettingKey<String> mapDirSetting) {
		JTextField folderPathField = new JTextField();
		folderPathField.setEditable(false);
		String directory = settings.get(mapDirSetting);
		if (directory == null) {
			folderPathField.setText("Project folder...");
			GridBagConstraints constraints = new GridBagConstraints();
			mapListPanel.add(new JLabel("First select folder above."), constraints);
		} else {
			folderPathField.setText(directory);
			updateMapList(new File(directory));
		}

		JButton browseButton = new JButton(new AbstractAction("Browse") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String path = folderPathField.getText();
				JFileChooser fileChooser = new JFileChooser(new File(path.isEmpty() ? "." : path));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setFileHidingEnabled(true);
				fileChooser.setMultiSelectionEnabled(false);
				int answer = fileChooser.showDialog(EventsOrderingFix.this, "Browse");
				if (answer == JFileChooser.APPROVE_OPTION) {
					process(() -> {
						File directory = fileChooser.getSelectedFile();
						settings.set(mapDirSetting, directory.toString());
						folderPathField.setText(directory.toString());
						updateMapList(directory);
					});
				} else {
					// do not consider it
				}
			}

		});
		browseButton.setToolTipText("Select the MGQ Paradox folder.");

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		panel.add(folderPathField, constraints);
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx++;
		panel.add(browseButton, constraints);
		return panel;
	}

	protected void unlockFeatures() {
		featureButtons.forEach((b) -> b.setEnabled(true));
	}

	protected void lockFeatures() {
		featureButtons.forEach((b) -> b.setEnabled(false));
	}

	private JButton createEventSortingButton() {
		JButton button = new JButton(new AbstractAction("Sort events") {

			@Override
			public void actionPerformed(ActionEvent e) {
				process(() -> {
					logger.info("Sort selected maps.");
					mapSelection.forEach((file, isSelected) -> {
						if (isSelected) {
							reorderEvents(file);
						}
					});
					logger.info("Sort done.");
				});
			}
		});
		button.setToolTipText("Sort the events by numbers.");
		return button;
	}

	private JButton createNormalizationButton() {
		JButton button = new JButton(new AbstractAction("Normalize sizes") {

			@Override
			public void actionPerformed(ActionEvent e) {
				process(() -> {
					logger.info("Normalize selected maps.");
					mapSelection.forEach((file, isSelected) -> {
						if (isSelected) {
							normalizeSeparatingLines(file);
						}
					});
					logger.info("Normalization done.");
				});
			}
		});
		button.setToolTipText("Force the events to be separated by exactly 1 empty line.");
		return button;
	}

	private JScrollPane createLogArea() {
		JEditorPane logArea = new JEditorPane();
		logArea.setEditable(false);
		logArea.setContentType("text/html");
		logArea.setMinimumSize(new Dimension(0, 100));

		HTMLEditorKit editor = (HTMLEditorKit) logArea.getEditorKit();
		StyleSheet styleSheet = editor.getStyleSheet();
		styleSheet.addRule("p {margin:0; padding:0;}");
		styleSheet.addRule("p.error {color:red; font-weight:bold;}");

		HTMLDocument document = (HTMLDocument) logArea.getDocument();
		Element body = findBodyElement(document);
		Handler handler = new Handler() {

			@Override
			public void publish(LogRecord record) {
				try {
					if (record.getLevel() == Level.SEVERE) {
						document.insertBeforeEnd(body, "<p class='error'>" + record.getMessage() + "</p>");
						document.insertBeforeEnd(body, "<p class='error'>" + "[" + record.getSourceClassName() + "."
								+ record.getSourceMethodName() + "]" + "</p>");
					} else {
						document.insertBeforeEnd(body, "<p>" + record.getMessage() + "</p>");
					}

					Document doc = logArea.getDocument();
					String text = doc.getText(0, doc.getLength());
					logArea.setCaretPosition(text.lastIndexOf("\n") + 1);
				} catch (Exception cause) {
					throw new RuntimeException(cause);
				}
			}

			@Override
			public void flush() {
				// Nothing to flush
			}

			@Override
			public void close() throws SecurityException {
				// Nothing to close
			}
		};
		logger.addHandler(handler);

		JScrollPane scrollPane = new JScrollPane(logArea);
		scrollPane.setBorder(new TitledBorder("Logs"));
		return scrollPane;
	}

	private Element findBodyElement(HTMLDocument document) {
		Element[] roots = document.getRootElements();
		Element body = null;
		for (int i = 0; i < roots[0].getElementCount(); i++) {
			Element element = roots[0].getElement(i);
			if (element.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY) {
				body = element;
				break;
			}
		}
		return body;
	}

	private static Stream<Path> listPaths(Path p) {
		try {
			return Files.list(p);
		} catch (IOException cause) {
			throw new RuntimeException(cause);
		}
	}

	private static boolean isHidden(Path p) {
		try {
			return Files.isHidden(p);
		} catch (IOException cause) {
			throw new RuntimeException(cause);
		}
	}

	private void updateMapList(File mapDirectory) {
		logger.info("Search for maps in " + mapDirectory);
		List<File> maps = FlatMapSupport
				.recursiveFlatMapFromRoot(mapDirectory.toPath(), (p) -> !isHidden(p) && Files.isDirectory(p),
						(p) -> listPaths(p))
				.filter((p) -> p.toFile().getName().matches("Map[0-9]+\\.txt")).map((p) -> p.toFile())
				.sorted(new NaturalComparator<>()).collect(Collectors.toList());
		logger.info("Maps found: " + maps.size());

		logger.info("Updating graphical interface...");
		mapListPanel.removeAll();
		mapSelection.clear();
		checkBoxes.clear();

		mapListPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;

		constraints.gridx = 0;
		constraints.gridy = 0;
		for (File mapFile : maps) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setAction(new AbstractAction(mapFile.getName()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					mapSelection.put(mapFile, checkBox.isSelected());
				}
			});
			checkBox.setSelected(false);
			mapSelection.put(mapFile, false);

			mapListPanel.add(checkBox, constraints);
			checkBoxes.add(checkBox);

			constraints.gridy++;
		}
		revalidate();
		logger.info("Graphical interface updated");
	}

	public static void main(String[] args) throws IOException {
		LoggerUtil.LoadLoggingProperties();
		new Thread(new Runnable() {
			public void run() {
				try {
					PropertyFileSetting settings = new PropertyFileSetting(new File("mgq-editor.ini"));
					settings.setFutureSwitcher("mapDir", Switcher.identity());
					SettingKey<String> mapDirSetting = settings.registerKey("mapDir", null);

					JFrame frame = new JFrame("Event Ordering Fix");
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setLayout(new GridLayout(1, 1));
					frame.getContentPane().add(new EventsOrderingFix(settings, mapDirSetting));
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					logger.log(Level.SEVERE, null, e);
				}
			}
		}).start();
	}

	public static void reorderEvents(File file) {
		try {
			logger.info("Reorder events of " + file.getName());
			Events events = new Events();
			events.setContent(FileUtils.readFileToString(file, CHARSET));
			events.sort((e1, e2) -> Integer.compare(e1.getEventID(), e2.getEventID()));
			FileUtils.write(file, events.getContent(), CHARSET);
			logger.info("Reordered events: " + events.size());
		} catch (IOException cause) {
			logger.log(Level.SEVERE, "Impossible to read file: " + file, cause);
		} catch (ParsingException cause) {
			logger.log(Level.SEVERE, "Impossible to parse file: " + file, cause);
		}
	}

	public static void normalizeSeparatingLines(File file) {
		try {
			logger.info("Normalize separating lines of " + file.getName());
			String content = FileUtils.readFileToString(file, CHARSET);
			content = normalizeSeparatingLines(content);
			FileUtils.write(file, content, CHARSET);
		} catch (IOException cause) {
			logger.log(Level.SEVERE, "Impossible to read file: " + file, cause);
		} catch (ParsingException cause) {
			logger.log(Level.SEVERE, "Impossible to parse file: " + file, cause);
		}
	}

	public static String normalizeSeparatingLines(String content) {
		Events events = new Events();
		events.setContent(content);

		if (events.size() >= 2) {
			for (Event event : events.getEvents()) {
				if (!event.getLine(event.size() - 1).getContent().trim().isEmpty()) {
					event.addLine(event.size(), "");
				} else {
					while (event.getLine(event.size() - 2).getContent().trim().isEmpty()) {
						event.removeLine(event.size() - 1);
					}
				}
			}
			logger.info("Events separated: " + events.size());
		} else {
			logger.info("No event to separate");
		}

		return events.getContent();
	}

	private void process(Runnable process) {
		lockFeatures();
		Thread thread = new Thread(() -> {
			process.run();
			unlockFeatures();
		});
		thread.setDaemon(true);
		thread.start();
	}
}
