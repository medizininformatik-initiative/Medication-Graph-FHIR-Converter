package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A transformer which removes blank strings from a list.
 *
 * @author Markus Budeus
 */
public class RemoveBlankStrings implements TraceableTransformer<List<String>, List<String>,
		StringListUsageStatement, StringListUsageStatement> {

	@Override
	public List<String> apply(List<String> source) {
		ArrayList<String> result = new ArrayList<>(source);
		for (int i = 0; i < result.size(); i++) {
			String entry = result.get(i);
			if (entry == null || entry.isBlank()) {
				result.remove(i);
				i--;
			}
		}
		return result;
	}

	@Override
	public StringListUsageStatement reverseTransformUsageStatement(List<String> input,
	                                                               StringListUsageStatement usageStatement) {

		Tools.ensureValidity(this, input, usageStatement);
		if (input.size() == usageStatement.getOriginal().size()) {
			return usageStatement;
		}

		// Blank strings have been filtered out, we need to know at which indices
		Set<Integer> usedIndices = new HashSet<>();
		int encounteredBlanks = 0;
		for (int i = 0; i < input.size(); i++) {
			if (input.get(i).isBlank()) {
				encounteredBlanks++;
			} else {
				if (usageStatement.getUsedIndices().contains(i - encounteredBlanks)) usedIndices.add(i);
			}
		}
		return new StringListUsageStatement(input, usedIndices);
	}
}
