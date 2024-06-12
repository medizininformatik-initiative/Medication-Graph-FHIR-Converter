package de.medizininformatikinitiative.medgraph.searchengine.tracing;

/**
 * Represents a range between two integers, the lower bound being inclusive and the upper bound being exclusive.
 *
 * @param from The lower bound of the range (inclusive).
 * @param to   The upper bound of the range (exclusive). Guaranteed to not be less than {@link #from}.
 * @author Markus Budeus
 */
public record IntRange(int from, int to) {

	/**
	 * Creates a new integer range.
	 *
	 * @param from the value to start the range at (inclusive)
	 * @param to   the end value of the range (exclusive)
	 * @throws IllegalArgumentException if from is larger than to
	 */
	public IntRange {
		if (from > to) throw new IllegalArgumentException(
				"The range's lower bound must be less than or equal to the upper bound!");
	}

	/**
	 * Returns the size of this range, i.e. the value of <code>{@link #to ()} - {@link #from ()}</code>
	 */
	public int getSize() {
		return to - from;
	}

	@Override
	public String toString() {
		return "[" + from + ":" + to + "]";
	}
}
