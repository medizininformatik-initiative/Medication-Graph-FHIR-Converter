package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringSetUsageStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A transformer which turns a list of strings into a set, thereby eliminating order information and duplicates.
 *
 * @author Markus Budeus
 */
public class ListToSet implements TraceableTransformer<List<String>, Set<String>,
		StringListUsageStatement, StringSetUsageStatement> {
	@Override
	public Set<String> apply(List<String> source) {
		return new HashSet<>(source);
	}

	@Override
	public StringListUsageStatement reverseTransformUsageStatement(List<String> input,
	                                                               StringSetUsageStatement usageStatement) {
		if (!apply(input).equals(usageStatement.getOriginal())) {
			throw new IllegalArgumentException("The given output usage statement's original list is different than " +
					"what this class would produce for the given input!");
		}
		Set<String> usedTokens = usageStatement.getUsedParts();
		Set<Integer> usedIndices = new HashSet<>(usedTokens.size());
		for (int i = 0; i < input.size(); i++) {
			if (usedTokens.contains(input.get(i)))
				usedIndices.add(i);
		}
		return new StringListUsageStatement(input, usedIndices);
	}
}
