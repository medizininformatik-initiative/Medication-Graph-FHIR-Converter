package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This transformer removes special characters (',', ';', '|', '-', ':', '.', '®') from the end of each string if
 * present. If this were to leave a string empty, it is removed from the set.
 *
 * @author Markus Budeus
 */
public class TrimSpecialSuffixSymbols implements TraceableTransformer<List<String>, List<String>,
		StringListUsageStatement, StringListUsageStatement> {

	@Override
	public List<String> apply(List<String> source) {
		if (source.isEmpty()) return source;
		List<String> result = source.stream().map(this::trimSeperators)
		                            .filter(s -> !s.isBlank())
		                            .toList();
		if (result.isEmpty())
			System.err.println("Warning! List " + source + " became empty after trimming separators!");
		return result;
	}

	private String trimSeperators(String s) {
		int lastChar = s.length() - 1;
		while (lastChar >= 0) {
			switch (s.charAt(lastChar)) {
				case ',':
				case ';':
				case '|':
				case '-':
				case ':':
				case '.':
				case '®':
					s = s.substring(0, lastChar);
					lastChar -= 1;
					break;
				default:
					return s;
			}
		}
		return s;
	}

	@Override
	public StringListUsageStatement reverseTransformUsageStatement(List<String> input,
	                                                               StringListUsageStatement usageStatement) {
		Tools.ensureValidity(this, input, usageStatement);
		if (input.size() == usageStatement.getUsedIndices().size()) {
			return new StringListUsageStatement(input, usageStatement.getUsedIndices());
		}

		// Blank strings have been filtered out, we need to know at which indices
		List<String> trimmedInputs = input.stream().map(this::trimSeperators).toList();
		Set<Integer> usedIndices = new HashSet<>();
		int encounteredBlanks = 0;
		for (int i = 0; i < trimmedInputs.size(); i++) {
			if (trimmedInputs.get(i).isBlank()) {
				encounteredBlanks++;
			} else {
				if (usageStatement.getUsedIndices().contains(i - encounteredBlanks)) usedIndices.add(i);
			}
		}
		return new StringListUsageStatement(input, usedIndices);
	}
}
