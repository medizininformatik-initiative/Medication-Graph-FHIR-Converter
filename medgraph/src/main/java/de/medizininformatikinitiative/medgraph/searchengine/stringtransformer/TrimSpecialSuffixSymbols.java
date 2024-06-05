package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;

import java.util.List;

/**
 * This transformer removes special characters (',', ';', '|', '-', ':', '.', '®') from the end of each string if
 * present. Note this may leave strings blank!
 *
 * @author Markus Budeus
 */
public class TrimSpecialSuffixSymbols implements TraceableTransformer<List<String>, List<String>,
		StringListUsageStatement, StringListUsageStatement> {

	@Override
	public List<String> apply(List<String> source) {
		if (source.isEmpty()) return source;
		return source.stream().map(this::trimSeperators)
		             .toList();
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
		return new StringListUsageStatement(input, usageStatement.getUsedIndices());
	}
}
