package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageStatement;

/**
 * @author Markus Budeus
 */
public interface TraceableTransformer<S, T, U extends InputUsageStatement<S>, V extends InputUsageStatement<T>>
		extends Transformer<S, T> {
	// TODO Javadoc

	U traceTransformation(S input, V outputUsageStatement);

	default <A, B extends InputUsageStatement<A>> TraceableTransformer<S, A, U, B> andTraceable(
			TraceableTransformer<T, A, V, B> transformer) {
		return new TraceableCompoundTransformer<>(this, transformer);
	}

}
