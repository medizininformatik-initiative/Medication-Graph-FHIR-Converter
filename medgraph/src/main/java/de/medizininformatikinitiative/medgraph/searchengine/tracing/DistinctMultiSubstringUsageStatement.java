package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Indicates that multiple non-overlapping sections of the input string has been used by an operation.
 *
 * @author Markus Budeus
 * @see InputUsageStatement
 */
public class DistinctMultiSubstringUsageStatement extends SubstringUsageStatement {

	/**
	 * The used ranges of characters from the original string that were used. Guaranteed to not overlap and sorted in
	 * ascending order (i.e. lower ranges pointing to earlier parts of the string coming first)
	 */
	@NotNull
	private final List<IntRange> usedRanges;

	/**
	 * Creates a new {@link DistinctMultiSubstringUsageStatement}.
	 *
	 * @param original   the original input string
	 * @param usedRanges the ranges of characters from the string that were used
	 * @throws NullPointerException     if any of the given ranges is null
	 * @throws IllegalArgumentException if any of the given ranges is out of bounds for the original string or if any of
	 *                                  the ranges overlap
	 */
	public DistinctMultiSubstringUsageStatement(@NotNull String original, @NotNull Set<IntRange> usedRanges) {
		super(original);

		this.usedRanges = new ArrayList<>(usedRanges);
		this.usedRanges.sort(Comparator.comparing(IntRange::getFrom));

		int lastTo = 0;
		int length = original.length();
		for (IntRange range : this.usedRanges) {
			if (range.getFrom() < 0 || range.getTo() > length) {
				throw new IllegalArgumentException("Got the range " + range + " as used range, " +
						"but it's out of bounds for the input string of length " + length);
			}
			if (range.getFrom() < lastTo) {
				throw new IllegalArgumentException("The given used ranges overlap!");
			}
			lastTo = range.getTo();
		}
	}

	@Override
	@NotNull
	public String getUnusedParts() {
		StringBuilder builder = new StringBuilder(getOriginal());
		for (IntRange range : usedRanges.reversed()) {
			builder.delete(range.getFrom(), range.getTo());
		}
		return builder.toString();
	}

	@Override
	@NotNull
	public String getUsedParts() {
		StringBuilder builder = new StringBuilder();
		for (IntRange range : usedRanges) {
			builder.append(getOriginal(), range.getFrom(), range.getTo());
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		DistinctMultiSubstringUsageStatement that = (DistinctMultiSubstringUsageStatement) object;
		return Objects.equals(usedRanges, that.usedRanges);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), usedRanges);
	}

	@Override
	public String toString() {
		return "DistinctMultiSubstringUsageStatement{" +
				"original=" + getOriginal() + ", " +
				"usedRanges=" + usedRanges +
				'}';
	}

	@Override
	public Set<IntRange> getUsedRanges() {
		return new HashSet<>(usedRanges);
	}

	/**
	 * Returns the used ranges as sorted list, with the ranges sorted by their start numbers, in ascending order.
	 */
	public List<IntRange> getUsedRangesAsList() {
		return new ArrayList<>(usedRanges);
	}

}
