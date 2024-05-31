package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Indicates that a single, specific section of the input string has been used by an operation.
 *
 * @author Markus Budeus
 * @see InputUsageStatement
 */
public class SubstringUsageStatement extends AbstractUsageStatement<String> {

	@NotNull
	private final IntRange usedRange;

	/**
	 * Creates a new {@link SubstringUsageStatement}.
	 *
	 * @param original  the original input string
	 * @param usedRange the range of characters from the string that was used
	 * @throws IllegalArgumentException if the given range is out of bounds for the original string
	 */
	public SubstringUsageStatement(@NotNull String original, @NotNull IntRange usedRange) {
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
}
