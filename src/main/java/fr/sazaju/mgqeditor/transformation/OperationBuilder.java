package fr.sazaju.mgqeditor.transformation;

import fr.sazaju.mgqeditor.fix.util.Version;

/**
 * An {@link OperationBuilder} aims at creating an {@link Operation} based on a
 * source {@link Version} and a target {@link Version}. because an
 * {@link Operation} usually focuses on few lines, it is important to tell from
 * which line the {@link Operation} should apply. Because the general assumption
 * is that all the lines before this one already correspond, this line is the
 * same for both the source and the target. Additionally to the
 * {@link Operation}, the {@link OperationBuilder} should tell how good is the
 * {@link Operation} to transform the source towards the target by providing the
 * line until which the two correspond once the {@link Operation} is applied.
 * The generated {@link Operation} and related information are provided through
 * an {@link OperationDescriptor}.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 *
 */
public interface OperationBuilder {

	/**
	 * 
	 * @return The verb corresponding to the {@link Operation} purpose
	 */
	public String getOperationName();

	/**
	 * @param source
	 *            the {@link Version} to transform
	 * @param start
	 *            the line from which the operation should transform the source
	 * @return the next step
	 * @throws InvalidSourceException
	 *             if no {@link Operation} can be generated for the given source at
	 *             the given start
	 * @throws IndexOutOfBoundsException
	 *             if the start index is negative or above size-1
	 */
	public TargetSelector createOperationToTransform(Version source, int start)
			throws InvalidSourceException, IndexOutOfBoundsException;

	/**
	 * Receive the target {@link Version} to come closer to and generate the
	 * corresponding {@link Operation}.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 *
	 */
	public static interface TargetSelector {
		/**
		 * 
		 * @param target
		 *            the {@link Version} to come closer to
		 * @return the {@link OperationDescriptor} providing the {@link Operation} and
		 *         other information
		 * @throws InvalidTargetException
		 *             if the generated {@link Operation} cannot help to come closer to
		 *             the target
		 */
		public OperationDescriptor into(Version target) throws InvalidTargetException;
	}

	/**
	 * Container for an {@link Operation} and other related information. It is
	 * mainly designed for an {@link OperationBuilder}.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 *
	 */
	public static interface OperationDescriptor {

		/**
		 * 
		 * @return the generated {@link Operation}
		 */
		public Operation getOperation();

		/**
		 * 
		 * @return the line until which the transformed source corresponds to the target
		 */
		public int getValidUntil();

	}

	@SuppressWarnings("serial")
	public static class InvalidSourceException extends Exception {
		public InvalidSourceException(String message) {
			super(message);
		}

		public InvalidSourceException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	@SuppressWarnings("serial")
	public static class InvalidTargetException extends Exception {
		public InvalidTargetException(String message) {
			super(message);
		}

		public InvalidTargetException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static interface Operation {
		public Version transform(Version original) throws InvalidOperationException;

	}

	@SuppressWarnings("serial")
	public static class InvalidOperationException extends RuntimeException {
		public InvalidOperationException() {
			super();
		}

		public InvalidOperationException(String message) {
			super(message);
		}

		public InvalidOperationException(Throwable cause) {
			super(cause);
		}

		public InvalidOperationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
