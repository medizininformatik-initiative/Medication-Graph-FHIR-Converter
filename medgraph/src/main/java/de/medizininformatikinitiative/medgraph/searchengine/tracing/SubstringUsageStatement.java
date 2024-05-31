package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Markus Budeus
 */
public class SubstringUsageStatement extends AbstractUsageStatement<String> {

	// TODO Javadoc
	// TODO Test

	private final List<IntRange> usedRanges;

	public SubstringUsageStatement(String input, List<IntRange> usedRanges) {
		super(input);

		this.usedRanges = new ArrayList<>(usedRanges);
		// TODO verufy usedRanges are valid!
		this.usedRanges.sort(Comparator.comparing(IntRange::getFrom).reversed());
	}

	@Override
	public String getUnusedParts() {
		StringBuilder builder = new StringBuilder(getInput());
		for (IntRange range : usedRanges) {
			builder.delete(range.getFrom(), range.getTo());
		}
		return builder.toString();
	}

	@Override
	public String getUsedParts() {
		StringBuilder builder = new StringBuilder();
		for (IntRange range : usedRanges) {
			builder.append(getInput(), range.getFrom(), range.getTo());
		}
		return builder.toString();
	}
}
