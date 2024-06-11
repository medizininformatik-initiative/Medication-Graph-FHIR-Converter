package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Transformer which removes all tokens which have less than a specified length.
 *
 * @author Markus Budeus
 */
public class MinimumTokenLength implements TraceableTransformer<List<String>, List<String>,
		StringListUsageStatement, StringListUsageStatement> {
	private final int minLength;

	public MinimumTokenLength(int minLength) {
		this.minLength = minLength;
	}

	@Override
	public List<String> apply(List<String> source) {
		List<String> newList = new ArrayList<>(source);
		newList.removeIf(s -> s.length() < minLength);
		return newList;
	}

	@Override
	public StringListUsageStatement reverseTransformUsageStatement(List<String> input,
	                                                               StringListUsageStatement usageStatement) {
		Tools.ensureValidity(this, input, usageStatement);

		Set<Integer> sourceUsedIndices = usageStatement.getUsedIndices();
		Set<Integer> usedIndices = new HashSet<>();
		int offset = 0;
		for (int i = 0; i < input.size(); i++) {
			if (input.get(i).length() < minLength) {
				offset++;
			} else if (sourceUsedIndices.contains(i - offset)) {
				usedIndices.add(i);
			}
		}
		return new StringListUsageStatement(input, usedIndices);
	}

	public int getMinLength() {
		return minLength;
	}
}
