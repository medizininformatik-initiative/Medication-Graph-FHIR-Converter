package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Indicates that multiple non-overlapping sections of the input string has been used by an operation.
 *
 * @author Markus Budeus
 * @see InputUsageStatement
 */
public class MultiSubstringUsageStatement extends AbstractUsageStatement<String> {

	/**
	 * The used ranges of characters from the original string that were used. Guaranteed to not overlap and sorted in
	 * ascending order (i.e. lower ranges pointing to earlier parts of the string coming first)
	 */
	@NotNull
	private final List<IntRange> usedRanges;

	/**
	 * Creates a new {@link MultiSubstringUsageStatement}.
	 *
	 * @param original   the original input string
	 * @param usedRanges the ranges of characters from the string that were used
	 * @throws NullPointerException     if any of the given ranges is null
	 * @throws IllegalArgumentException if any of the given ranges is out of bounds for the original string or if any of
	 *                                  the ranges overlap
	 */
	public MultiSubstringUsageStatement(@NotNull String original, @NotNull Set<IntRange> usedRanges) {
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
}
