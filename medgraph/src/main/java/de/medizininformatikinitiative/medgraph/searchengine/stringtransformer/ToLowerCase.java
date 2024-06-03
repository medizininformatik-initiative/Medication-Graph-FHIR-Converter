package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;

/**
 * This transformer makes the names lowercase.
 *
 * @author Markus Budeus
 */
public class ToLowerCase implements TraceableTransformer<String, String, DistinctMultiSubstringUsageStatement, DistinctMultiSubstringUsageStatement> {

	@Override
	public String apply(String source) {
		return source.toLowerCase();
	}

	@Override
	public DistinctMultiSubstringUsageStatement reverseTransformUsageStatement(String input,
	                                                                           DistinctMultiSubstringUsageStatement usageStatement) {
		Tools.ensureValidity(this, input, usageStatement);
		return new DistinctMultiSubstringUsageStatement(input, usageStatement.getUsedRanges());

	}
}
