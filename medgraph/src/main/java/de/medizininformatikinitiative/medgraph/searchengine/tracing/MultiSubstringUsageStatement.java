package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Indicates that multiple possibly overlapping sections of the input string has been used by an operation.
 *
 * @author Markus Budeus
 * @see InputUsageStatement
 */
public class MultiSubstringUsageStatement extends SubstringUsageStatement {

	/**
	 * The used ranges of characters from the original string that were used.
	 */
	@NotNull
	private final List<IntRange> usedRanges;

	/**
	 * Creates a new {@link MultiSubstringUsageStatement}.
	 *
	 * @param original   the original input string
	 * @param usedRanges the ranges of characters from the string that were used
	 * @throws NullPointerException     if any of the given ranges is null
	 * @throws IllegalArgumentException if any of the given ranges is out of bounds for the original string
	 */
	public MultiSubstringUsageStatement(@NotNull String original, @NotNull Set<IntRange> usedRanges) {
		super(original);

		this.usedRanges = new ArrayList<>(usedRanges);
		this.usedRanges.sort(Comparator.comparing(IntRange::getFrom));

		int length = original.length();
		for (IntRange range : this.usedRanges) {
			if (range.getFrom() < 0 || range.getTo() > length) {
				throw new IllegalArgumentException("Got the range " + range + " as used range, " +
						"but it's out of bounds for the input string of length " + length);
			}
		}
	}

	@Override
	@NotNull
	public String getUnusedParts() {
		return getUsedOrUnusedParts(false);
	}

	@Override
	@NotNull
	public String getUsedParts() {
		return getUsedOrUnusedParts(true);
	}

	/**
	 * Returns a {@link DistinctMultiSubstringUsageStatement} which provides the same used parts of the same original
	 * string as this instance, but without overlapping regions, as those are merged into a single used region. Regions
	 * directly adjacent to each other are also merged. In that sense, information about parts having been used twice
	 * and/or with distinct other parts of the string, is lost.
	 *
	 * @return a {@link DistinctMultiSubstringUsageStatement} representing this usage statement without overlapping
	 * regions
	 */
	public DistinctMultiSubstringUsageStatement distinct() {
		boolean[] usageArray = buildUsageArray();
		boolean currentlyInRange = false;
		int start = 0;
		Set<IntRange> ranges = new HashSet<>();
		for (int i = 0; i <= usageArray.length; i++) {
			if (i < usageArray.length && usageArray[i]) {
				if (!currentlyInRange) {
					start = i;
					currentlyInRange = true;
				}
			} else {
				if (currentlyInRange) {
					ranges.add(new IntRange(start, i));
					currentlyInRange = false;
				}
			}
		}
		return new DistinctMultiSubstringUsageStatement(getOriginal(), ranges);
	}

	private String getUsedOrUnusedParts(boolean used) {
		StringBuilder builder = new StringBuilder();
		boolean[] usageArray = buildUsageArray();
		String original = getOriginal();
		for (int i = 0; i < original.length(); i++) {
			if (usageArray[i] == used) {
				builder.append(original.charAt(i));
			}
		}
		return builder.toString();
	}

	/**
	 * Creates an array of the exact same length as the original string. Each value indicates if the corresponding
	 * character is part of any used range.
	 */
	private boolean[] buildUsageArray() {
		boolean[] usageArray = new boolean[getOriginal().length()];
		for (IntRange range : usedRanges) {
			for (int i = range.getFrom(); i < range.getTo(); i++) {
				usageArray[i] = true;
			}
		}
		return usageArray;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		MultiSubstringUsageStatement that = (MultiSubstringUsageStatement) object;
		return Objects.equals(usedRanges, that.usedRanges);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), usedRanges);
	}

	@Override
	public String toString() {
		return "MultiSubstringUsageStatement{" +
				"original=" + getOriginal() + ", " +
				"usedRanges=" + usedRanges +
				'}';
	}

	@Override
	public Set<IntRange> getUsedRanges() {
		return new HashSet<>(usedRanges);
	}

}
