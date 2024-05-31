package de.medizininformatikinitiative.medgraph.searchengine.tracing;

/**
 * Represents a range between two integers, the lower bound being inclusive and the upper bound being exclusive.
 *
 * @author Markus Budeus
 */
public class IntRange {

	/**
	 * The lower bound of the range (inclusive).
	 */
	private final int from;
	/**
	 * The upper bound of the range (exclusive). Guaranteed to not be less than {@link #from}.
	 */
	private final int to;

	/**
	 * Creates a new integer range.
	 *
	 * @param from the value to start the range at (inclusive)
	 * @param to   the end value of the range (exclusive)
	 * @throws IllegalArgumentException if from is larger than to
	 */
	public IntRange(int from, int to) {
		if (from > to) throw new IllegalArgumentException(
				"The range's lower bound must be less than or equal to the upper bound!");
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	@Override
	public String toString() {
		return "[" + from + ":" + to + "]";
	}
}
