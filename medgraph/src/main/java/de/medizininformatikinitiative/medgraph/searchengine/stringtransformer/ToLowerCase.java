package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.MultiSubstringUsageStatement;

/**
 * This transformer makes the names lowercase.
 *
 * @author Markus Budeus
 */
public class ToLowerCase implements TraceableTransformer<String, String, MultiSubstringUsageStatement, MultiSubstringUsageStatement> {

	@Override
	public String apply(String source) {
		return source.toLowerCase();
	}

	@Override
	public MultiSubstringUsageStatement reverseTransformUsageStatement(String input,
	                                                                   MultiSubstringUsageStatement usageStatement) {
		Tools.ensureValidity(this, input, usageStatement);
		return new MultiSubstringUsageStatement(input, usageStatement.getUsedRanges());

	}
}
