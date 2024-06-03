package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * Indicates that a single, specific section of the input string has been used by an operation.
 *
 * @author Markus Budeus
 * @see InputUsageStatement
 */
public class SingleSubstringUsageStatement extends SubstringUsageStatement {

	@NotNull
	private final IntRange usedRange;

	/**
	 * Creates a new {@link SingleSubstringUsageStatement}.
	 *
	 * @param original  the original input string
	 * @param usedRange the range of characters from the string that was used
	 * @throws IllegalArgumentException if the given range is out of bounds for the original string
	 */
	public SingleSubstringUsageStatement(@NotNull String original, @NotNull IntRange usedRange) {
		super(original);
		this.usedRange = usedRange;

		if (usedRange.getFrom() < 0)
			throw new IllegalArgumentException("The given range cannot include negative values!");
		if (usedRange.getTo() > original.length()) throw new IllegalArgumentException(
				"The given range is out of bounds for the string! " +
						"(Range " + usedRange + ", String length: " + original.length() + ")");
	}

	@Override
	@NotNull
	public String getUnusedParts() {
		StringBuilder builder = new StringBuilder(getOriginal());
		builder.delete(usedRange.getFrom(), usedRange.getTo());
		return builder.toString();
	}

	@Override
	@NotNull
	public String getUsedParts() {
		return getOriginal().substring(usedRange.getFrom(), usedRange.getTo());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), usedRange);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		SingleSubstringUsageStatement that = (SingleSubstringUsageStatement) object;
		return Objects.equals(usedRange, that.usedRange);
	}

	@Override
	public Set<IntRange> getUsedRanges() {
		return Set.of(usedRange);
	}
}
