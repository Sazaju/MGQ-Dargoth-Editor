package fr.sazaju.mgqeditor.fix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.parser.regex.Events.EventLine;
import fr.sazaju.mgqeditor.transformation.OperationBuilder;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidSourceException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidTargetException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.Operation;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.OperationDescriptor;
import fr.sazaju.mgqeditor.transformation.Transformation;
import fr.sazaju.mgqeditor.transformation.impl.MergeMessageLinesOperationBuilder;
import fr.sazaju.mgqeditor.transformation.impl.PreserveLineOperationBuilder;
import fr.sazaju.mgqeditor.transformation.impl.ReplaceLineOperationBuilder;
import fr.sazaju.mgqeditor.transformation.impl.ReplaceLineOperation;
import fr.sazaju.mgqeditor.transformation.impl.TranslateChoicesOperationBuilder;
import fr.sazaju.mgqeditor.transformation.impl.TranslateFunctionStringsOperationBuilder;
import fr.sazaju.mgqeditor.transformation.impl.TranslateMessageOperationBuilder;
import fr.sazaju.mgqeditor.util.LoggerUtil;
import fr.vergne.collection.util.NaturalComparator;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.util.Newline;
import fr.vergne.parsing.layer.util.SeparatedLoop;
import fr.vergne.progress.Progress;
import fr.vergne.progress.impl.ManualProgress;
import fr.vergne.progress.impl.ProgressUtil;
import fr.vergne.progress.impl.ProgressUtil.Displayer;

public class ThreeWayMergeHelper {

	private static final String CHARSET = "UTF8";
	private static final Logger logger = Logger.getLogger(ThreeWayMergeHelper.class.getName());
	private static final int PROGRESS_PERIOD_IN_MS = 1000;

	public static void main(String[] args) throws IOException {
		LoggerUtil.LoadLoggingProperties();
		// File projectDirectory = new File("MGQ-Dargoth/Maps");
		File projectDirectory = new File("MGQ-Dargoth/Maps/Map277.txt");

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
				// System.in.read();
				String content = FileUtils.readFileToString(file, CHARSET);

				ThreeWayMergeHelper helper = new ThreeWayMergeHelper();
				content = helper.fixConflicts(content);

				File tempFile = new File(file.getPath() + "2");
				FileUtils.write(tempFile, content, CHARSET);
				logger.info("File written: " + tempFile);
			} else {
				// ignore non-text files
			}
		}
	}

	public String fixConflicts(String original) throws IOException {
		if (!original.contains("<<<<<<< HEAD")) {
			logger.info("No conflict to merge.");
			return original;
		} else {
			Pattern pattern = Pattern
					.compile("(?s)<{7} HEAD\n(.*?)\n?+\\|{7} merged common ancestors\n(.*?)\n?+={7}\n(.*?)\n?+>{7} EN");
			Matcher matcher = pattern.matcher(original);
			StringBuilder replacement = new StringBuilder(original.length());
			int lastCut = 0;
			while (matcher.find()) {
				replacement.append(original.substring(lastCut, matcher.start()));
				logger.info("Conflict at line "
						+ (original.substring(0, matcher.start()).replaceAll("[^\n]+", "").length() + 1));
				String resolution;
				try {
					resolution = resolve(matcher);
				} catch (UnableToFindOperationsException | UnableToUpdateException e) {
					// TODO Pass for now. Later, display and ask user to fix or
					// pass.
					logger.info("Unable to resolve, keep the conflict.");
					resolution = matcher.group();
				}
				replacement.append(resolution);
				lastCut = matcher.end();
			}
			replacement.append(original.substring(lastCut, original.length()));

			if (lastCut == 0) {
				throw new RuntimeException("The regex went wrong: no conflict found.");
			}

			return replacement.toString();
		}
	}

	private String resolve(Matcher matcher) throws UnableToFindOperationsException, UnableToUpdateException {
		SeparatedLoop<EventLine, Newline> original1 = new SeparatedLoop<>(Quantifier.POSSESSIVE,
				(Supplier<EventLine>) () -> new EventLine(), () -> new Newline());
		original1.setContent(matcher.group(2));
		SeparatedLoop<EventLine, Newline> translated1 = new SeparatedLoop<>(Quantifier.POSSESSIVE,
				(Supplier<EventLine>) () -> new EventLine(), () -> new Newline());
		translated1.setContent(matcher.group(3));
		logger.finest("Original version:\n" + addLineNumbers(original1.getContent()) + "\n");
		logger.finest("Translated version:\n" + addLineNumbers(translated1.getContent()) + "\n");
		Transformation translationTransformation = buildTranslationTransformation(original1, translated1);
		if (translationTransformation == null) {
			logger.info("Unable to retrieve translation transformation.");
			return null;
		} else {
			SeparatedLoop<EventLine, Newline> original2 = new SeparatedLoop<>(Quantifier.POSSESSIVE,
					(Supplier<EventLine>) () -> new EventLine(), () -> new Newline());
			original2.setContent(matcher.group(1));
			logger.finest("Version to translate:\n" + addLineNumbers(original2.getContent()) + "\n");
			Transformation updateTransformation;
			updateTransformation = buildStructureTransformation(original1, original2);
			if (updateTransformation == null) {
				logger.info("Unable to retrieve structure transformation.");
				return null;
			} else {
				translationTransformation = updateTransformation(translationTransformation, updateTransformation);
				SeparatedLoop<EventLine, Newline> translated2 = translationTransformation.apply(original2);
				logger.finest("Translation:\n" + addLineNumbers(translated2.getContent()) + "\n");

				logger.info("Translation rebuilt.");
				return translated2.getContent();
			}
		}
	}

	private String addLineNumbers(String content) {
		int[] index = { 0 };
		String[] split = content.split("\n");
		int width = (int) Math.floor(Math.log10(split.length)) + 1;
		return Arrays.stream(split).map((s) -> String.format("%1$" + width + "d", ++index[0]) + s)
				.reduce((a, b) -> a + "\n" + b).get();
	}

	@SuppressWarnings("serial")
	private class UnableToFindOperationsException extends Exception {
		public UnableToFindOperationsException(String message) {
			super(message);
		}
	}

	private Transformation buildTranslationTransformation(SeparatedLoop<EventLine, Newline> original,
			SeparatedLoop<EventLine, Newline> transformed) throws UnableToFindOperationsException {
		Set<OperationBuilder> operationFactories = new LinkedHashSet<>();
		operationFactories.add(new PreserveLineOperationBuilder());
		operationFactories.add(new TranslateMessageOperationBuilder());
		operationFactories.add(new TranslateChoicesOperationBuilder());
		operationFactories.add(new TranslateFunctionStringsOperationBuilder());
		/*
		 * Merging should be an update operation, not a translation one, but it is also
		 * made during translation, so we consider it as a translation one. It can also
		 * be reused as an update operation, as long as we find evidences that such
		 * updates occur.
		 */
		operationFactories.add(new MergeMessageLinesOperationBuilder());

		logger.fine("Start retrieval of translation transformation.");
		ManualProgress<Integer> progress = new ManualProgress<>(0, transformed.size());
		Displayer d = new Displayer() {

			@Override
			public <Value extends Number> void display(Progress<Value> progress) {
				double percent = progress.getCurrentNormalizedValue() * 100;
				Value current = progress.getCurrentValue();
				Value max = progress.getMaxValue();
				logger.info("Translation retrieval: " + current + "/" + max + " = " + percent + "%");
			}
		};
		ProgressUtil.displayProgress(progress, PROGRESS_PERIOD_IN_MS, d);
		Version initialVersion = new Version((i) -> original.get(i).getContent(), original.size());
		Version expectedVersion = new Version((i) -> transformed.get(i).getContent(), transformed.size());
		List<List<Operation>> validTransformations = findOperationsFor(initialVersion, expectedVersion,
				operationFactories, progress);
		progress.finish();
		logger.info("Translation transformations retrieved: " + validTransformations.size());

		if (validTransformations.isEmpty()) {
			logger.info("Translation retrieval failed: no valid transformation retrieved.");
			throw new UnableToFindOperationsException("No translation transformation found");
		} else if (validTransformations.size() == 1) {
			List<Operation> operations = validTransformations.get(0);
			logger.info("Translation retrieval successful: use the only one available: " + operations);
			return Transformation.createFromOperations(operations);
		} else {
			logger.info("Several valid translations retrieved, not managed.");
			throw new UnableToFindOperationsException("Not implemented yet");
		}
	}

	private Transformation buildStructureTransformation(SeparatedLoop<EventLine, Newline> original,
			SeparatedLoop<EventLine, Newline> transformed) throws UnableToFindOperationsException {
		Set<OperationBuilder> operationDesigners = new LinkedHashSet<OperationBuilder>();
		operationDesigners.add(new PreserveLineOperationBuilder());
		operationDesigners.add(new ReplaceLineOperationBuilder());

		logger.fine("Start retrieval of structure transformation.");
		ManualProgress<Integer> progress = new ManualProgress<>(0, transformed.size());
		Displayer d = new Displayer() {

			@Override
			public <Value extends Number> void display(Progress<Value> progress) {
				double percent = progress.getCurrentNormalizedValue() * 100;
				Value current = progress.getCurrentValue();
				Value max = progress.getMaxValue();
				logger.info("Structure retrieval: " + current + "/" + max + " = " + percent + "%");
			}
		};
		ProgressUtil.displayProgress(progress, PROGRESS_PERIOD_IN_MS, d);
		Version initialVersion = new Version((i) -> original.get(i).getContent(), original.size());
		Version expectedVersion = new Version((i) -> transformed.get(i).getContent(), transformed.size());
		List<List<Operation>> validTransformations = findOperationsFor(initialVersion, expectedVersion,
				operationDesigners, progress);
		progress.finish();
		logger.info("Structure transformations retrieved: " + validTransformations.size());

		if (validTransformations.isEmpty()) {
			logger.info("Structure retrieval failed: no valid transformation retrieved.");
			throw new UnableToFindOperationsException("No structure transformation retrieved");
		} else if (validTransformations.size() == 1) {
			List<Operation> operations = validTransformations.get(0);
			logger.info("Structure retrieval successful: use the only one available: " + operations);
			return Transformation.createFromOperations(operations);
		} else {
			logger.info("Several valid structure transformations retrieved, not managed.");
			throw new RuntimeException("Not implemented yet");
		}
	}

	@SuppressWarnings("serial")
	private class UnableToUpdateException extends Exception {
		public UnableToUpdateException(String message) {
			super(message);
		}
	}

	private Transformation updateTransformation(Transformation translationTransformation,
			Transformation updateTransformation) throws UnableToUpdateException {
		if (!updateTransformation.iterator().hasNext()) {
			return translationTransformation;
		} else {
			for (Operation operation : updateTransformation) {
				if (operation instanceof ReplaceLineOperation) {
					// TODO
					throw new UnableToUpdateException("Not implemented yet");
				} else {
					throw new UnableToUpdateException("Not managed yet: " + operation);
				}
			}
			return translationTransformation;
		}
	}

	private class X {

		private final List<Operation> operations;
		private final Version result;
		private final int checkFrom;

		public X(List<Operation> previousOperations, Operation newOperation, Version result, int checkFrom) {
			this.operations = new LinkedList<>(previousOperations);
			this.operations.add(newOperation);
			this.result = result;
			this.checkFrom = checkFrom;
		}

		public X(Version initial) {
			this(Collections.emptyList(), (v) -> v, initial, 0);
		}

		public Version getVersion() {
			return result;
		}

		public int getCheckFrom() {
			return checkFrom;
		}

		public List<Operation> getOperations() {
			return operations;
		}

	}

	private List<List<Operation>> findOperationsFor(Version initialVersion, Version expectedVersion,
			Set<OperationBuilder> operationFactories, ManualProgress<Integer> lineProgress) {
		List<X> validTransformations = new LinkedList<>();
		List<X> incompleteTransformations = new LinkedList<>();
		incompleteTransformations.add(new X(initialVersion));
		logger.fine("Initialized with empty transformation");

		while (!incompleteTransformations.isEmpty()) {
			logger.finer("Transformations to expand: " + incompleteTransformations.size());
			List<X> previousTransformations = incompleteTransformations;
			incompleteTransformations = new LinkedList<>();
			int allTransformationsValidUntil = Integer.MAX_VALUE;

			int counter = 0;
			for (X x : previousTransformations) {
				counter++;
				logger.finest("Retrieved transformation " + counter);

				if (x.getCheckFrom() > x.getVersion().size()) {
					throw new RuntimeException(
							"This case should not happen: more lines have been validated than the ones produced");
				} else if (x.getCheckFrom() == x.getVersion().size()) {
					// Transformation finished
					if (expectedVersion.size() == x.getVersion().size()) {
						logger.finest("Transformation complete, save it.");
						validTransformations.add(x);
					} else if (expectedVersion.size() > x.getVersion().size()) {
						logger.finest("Transformation unable to cover the expected version, discard it.");
						// Don't store again
					} else {
						logger.finest("Transformation longer than the expected version, discard.");
						// Don't store again
					}
				} else if (x.getCheckFrom() >= expectedVersion.size()) {
					logger.finest("Transformation does not consider all the expected version, discard it.");
				} else {
					Version currentVersion = x.getVersion();
					int checkFrom = x.getCheckFrom();
					logger.finest("Transformation incomplete, searching for valid expansions from line " + checkFrom);
					for (OperationBuilder factory : operationFactories) {
						try {
							logger.finest(factory.getOperationName() + " line " + checkFrom + ": "
									+ currentVersion.get(checkFrom));
							OperationDescriptor descriptor = factory
									.createOperationToTransform(currentVersion, checkFrom).into(expectedVersion);
							Operation newOperation = descriptor.getOperation();
							Version result = newOperation.transform(x.getVersion());
							logger.finest("Result: " + result.get(checkFrom).trim());

							int nextCheck = descriptor.getValidUntil() + 1;
							X validTransformation = new X(x.getOperations(), newOperation, result, nextCheck);
							incompleteTransformations.add(validTransformation);
							logger.finest("Operation valid: store transformation for further expansion.");

							allTransformationsValidUntil = Math.min(allTransformationsValidUntil,
									descriptor.getValidUntil());
						} catch (InvalidSourceException e) {
							logger.finest("Unapplicable to the current version: " + e.getMessage());
						} catch (InvalidTargetException e) {
							logger.finest("Unable to come closer to the expected version: " + e.getMessage());
						}
					}
				}
			}

			if (allTransformationsValidUntil == Integer.MAX_VALUE) {
				logger.finer("No more incomplete transformation to expand, stop search.");
				lineProgress.finish();
			} else {
				lineProgress.setCurrentValue(allTransformationsValidUntil + 1);
			}
		}
		logger.fine("Valid transformations found: " + validTransformations.size());
		List<List<Operation>> transformations = new LinkedList<>();
		for (X transformation : validTransformations) {
			transformations.add(transformation.getOperations());
		}
		return transformations;
	}

}
