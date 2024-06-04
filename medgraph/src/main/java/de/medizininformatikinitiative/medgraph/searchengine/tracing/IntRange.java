package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import java.util.Objects;

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

	/**
	 * Returns the size of this range, i.e. the value of <code>{@link #getTo()} - {@link #getFrom()}</code>
	 */
	public int getSize() {
		return to - from;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		IntRange range = (IntRange) object;
		return from == range.from && to == range.to;
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}

	@Override
	public String toString() {
		return "[" + from + ":" + to + "]";
	}
}
