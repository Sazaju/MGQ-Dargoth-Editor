package fr.sazaju.mgqeditor.transformation;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.fix.util.Version.Revision;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidOperationException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidSourceException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.InvalidTargetException;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.Operation;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.OperationDescriptor;
import fr.vergne.heterogeneousmap.HeterogeneousMap;

// TODO Check Junit 5 features to see if I can make repeated tests with the interface functions
public interface OperationBuilderTest {

	public OperationBuilder createBuilder(HeterogeneousMap context);

	public Iterable<Version> createValidSources(HeterogeneousMap context);

	default boolean hasInvalidSources() {
		return true;
	}

	default Iterable<Version> createInvalidSources(HeterogeneousMap context) {
		return Arrays.asList(Version.from(Arrays.asList("*****")));
	}

	public Iterable<Integer> getValidStartsFor(Version source, HeterogeneousMap context);

	default Iterable<Integer> getInvalidStartsFor(Version source, HeterogeneousMap context) {
		List<Integer> starts = new LinkedList<>();
		for (int i = 0; i < source.size(); i++) {
			starts.add(i);
		}
		for (Integer i : getValidStartsFor(source, context)) {
			starts.remove(i);
		}
		return starts;
	}

	public Iterable<Version> createValidTargetsFor(Version source, int start, HeterogeneousMap context);

	default boolean hasInvalidTargets() {
		return true;
	}

	default Iterable<Version> createInvalidTargetsFor(Version source, int start, HeterogeneousMap context) {
		return Arrays.asList(Version.from(Arrays.asList("*****")));
	}

	@Test
	default void testCreateNonNullBuilder() {
		HeterogeneousMap context = new HeterogeneousMap();
		assertNotNull(createBuilder(context));
	}

	@Test
	default void testCreateNonNullSources() {
		HeterogeneousMap context = new HeterogeneousMap();
		Iterable<Version> sources = createValidSources(context);
		assertNotNull(sources);
		for (Version source : sources) {
			assertNotNull(source);
		}
	}

	@Test
	default void testCreateNonNullTargets() {
		HeterogeneousMap context = new HeterogeneousMap();
		for (Version source : createValidSources(context)) {
			for (Integer start : getValidStartsFor(source, context)) {
				Iterable<Version> targets = createValidTargetsFor(source, start, context);
				assertNotNull(targets);
				for (Version target : targets) {
					assertNotNull(target);
				}
			}
		}
	}

	@Test
	default void testSourceAndTargetAllowToCreateOperation() throws InvalidTargetException, InvalidSourceException {
		HeterogeneousMap context = new HeterogeneousMap();
		OperationBuilder builder = createBuilder(context);
		for (Version source : createValidSources(context)) {
			for (Integer start : getValidStartsFor(source, context)) {
				for (Version target : createValidTargetsFor(source, start, context)) {
					OperationDescriptor descriptor = builder.createOperationToTransform(source, start).into(target);
					assertNotNull(descriptor);
					assertNotNull(descriptor.getOperation());
				}
			}
		}
	}

	@Test
	default void testOperationResultIsNotNull() throws InvalidTargetException, InvalidSourceException {
		HeterogeneousMap context = new HeterogeneousMap();
		OperationBuilder builder = createBuilder(context);
		for (Version source : createValidSources(context)) {
			for (Integer start : getValidStartsFor(source, context)) {
				for (Version target : createValidTargetsFor(source, start, context)) {
					OperationDescriptor descriptor = builder.createOperationToTransform(source, start).into(target);
					Version actual = descriptor.getOperation().transform(source);
					assertNotNull(actual);
				}
			}
		}
	}

	@Test
	default void testOperationTransformsSourceIntoTarget() throws InvalidTargetException, InvalidSourceException {
		HeterogeneousMap context = new HeterogeneousMap();
		OperationBuilder builder = createBuilder(context);
		for (Version source : createValidSources(context)) {
			for (Integer start : getValidStartsFor(source, context)) {
				for (Version target : createValidTargetsFor(source, start, context)) {
					OperationDescriptor descriptor = builder.createOperationToTransform(source, start).into(target);
					Version actual = descriptor.getOperation().transform(source);
					assertEquals(target, actual);
				}
			}
		}
	}

	@Test
	default void testOperationResultIsDifferentInstance() throws InvalidTargetException, InvalidSourceException {
		HeterogeneousMap context = new HeterogeneousMap();
		OperationBuilder builder = createBuilder(context);
		for (Version source : createValidSources(context)) {
			for (Integer start : getValidStartsFor(source, context)) {
				for (Version target : createValidTargetsFor(source, start, context)) {
					OperationDescriptor descriptor = builder.createOperationToTransform(source, start).into(target);
					Version actual = descriptor.getOperation().transform(source);
					assertFalse(source == actual);
					assertFalse(target == actual);
				}
			}
		}
	}

	@Test
	default void testOperationDoesNotAffectSource() throws InvalidTargetException, InvalidSourceException {
		HeterogeneousMap context = new HeterogeneousMap();
		OperationBuilder builder = createBuilder(context);
		for (Version source : createValidSources(context)) {
			Version replica = Version.from(source.toList());
			for (Integer start : getValidStartsFor(source, context)) {
				for (Version target : createValidTargetsFor(source, start, context)) {
					OperationDescriptor descriptor = builder.createOperationToTransform(source, start).into(target);
					descriptor.getOperation().transform(source);
					assertEquals(replica, source);
				}
			}
		}
	}

	@Test
	default void testOperationDoesNotTransformPreviousLines() throws InvalidTargetException, InvalidSourceException {
		HeterogeneousMap context = new HeterogeneousMap();
		OperationBuilder builder = createBuilder(context);

		for (final Version source : createValidSources(context)) {
			for (final Integer start : getValidStartsFor(source, context)) {
				for (final Version target : createValidTargetsFor(source, start, context)) {
					for (Version addendum : createValidSources(context)) {
						int size = addendum.size();
						Revision insertAddendumAtTheBeginning = (src, i) -> i < size ? addendum.get(i)
								: src.get(i - size);
						Version enrichedSource = new Version(source, insertAddendumAtTheBeginning);
						Version enrichedTarget = new Version(target, insertAddendumAtTheBeginning);
						int enrichedStart = start + size;

						Operation merging = builder.createOperationToTransform(enrichedSource, enrichedStart)
								.into(enrichedTarget).getOperation();
						Version actual = merging.transform(enrichedSource);
						for (int i = 0; i < size; i++) {
							assertEquals(addendum.get(i), actual.get(i));
						}
					}
				}
			}
		}
	}

	@Test
	default void testOperationBuildingOnNegativeLineThrowsException()
			throws InvalidTargetException, InvalidSourceException {
		HeterogeneousMap context = new HeterogeneousMap();
		OperationBuilder builder = createBuilder(context);
		for (Version source : createValidSources(context)) {
			int start = -1;
			try {
				builder.createOperationToTransform(source, start);
				fail("No exception thrown");
			} catch (IndexOutOfBoundsException cause) {
				// OK
			}
		}
	}

	@Test
	default void testOperationBuildingOnInvalidLinesThrowsException() throws InvalidTargetException {
		HeterogeneousMap context = new HeterogeneousMap();
		OperationBuilder builder = createBuilder(context);
		for (Version source : createValidSources(context)) {
			for (Integer start : getInvalidStartsFor(source, context)) {
				try {
					builder.createOperationToTransform(source, start);
					fail("No exception thrown");
				} catch (InvalidSourceException cause) {
					// OK
				}
			}
		}
	}

	@Test
	default void testOperationBuildingOnInvalidSourceThrowsException() throws InvalidTargetException {
		if (!hasInvalidSources()) {
			// Nothing to test
		} else {
			HeterogeneousMap context = new HeterogeneousMap();
			OperationBuilder builder = createBuilder(context);
			for (Version source : createInvalidSources(context)) {
				for (int start = 0; start < source.size(); start++) {
					try {
						builder.createOperationToTransform(source, start);
						fail("No exception thrown");
					} catch (InvalidSourceException cause) {
						// OK
					}
				}
			}
		}
	}

	@Test
	default void testOperationOnInvalidSourceThrowsException() throws InvalidTargetException, InvalidSourceException {
		if (!hasInvalidSources()) {
			// Nothing to test
		} else {
			HeterogeneousMap context = new HeterogeneousMap();
			OperationBuilder builder = createBuilder(context);
			for (Version source : createValidSources(context)) {
				for (Integer start : getValidStartsFor(source, context)) {
					for (Version target : createValidTargetsFor(source, start, context)) {
						Operation merging = builder.createOperationToTransform(source, start).into(target)
								.getOperation();
						for (Version invalidSource : createInvalidSources(context)) {
							try {
								merging.transform(invalidSource);
								fail("No exception thrown for " + merging + " with source:\n" + invalidSource);
							} catch (InvalidOperationException cause) {
								// OK
							}
						}
					}
				}
			}
		}
	}

	@Test
	default void testOperationOnInvalidTargetThrowsException() throws InvalidSourceException {
		if (!hasInvalidTargets()) {
			// Nothing to test
		} else {
			HeterogeneousMap context = new HeterogeneousMap();
			OperationBuilder builder = createBuilder(context);
			for (Version source : createValidSources(context)) {
				for (Integer start : getValidStartsFor(source, context)) {
					for (Version target : createInvalidTargetsFor(source, start, context)) {
						try {
							builder.createOperationToTransform(source, start).into(target);
							fail("No exception thrown for transforming from line " + start + " source:\n" + source
									+ "\ninto target:\n" + target);
						} catch (InvalidTargetException cause) {
							// OK
						}
					}
				}
			}
		}
	}
}
