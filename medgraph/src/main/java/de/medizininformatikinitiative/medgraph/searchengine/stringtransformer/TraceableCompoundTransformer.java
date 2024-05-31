package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageStatement;

/**
 * @author Markus Budeus
 */
public class TraceableCompoundTransformer<S, V, T,
		U extends InputUsageStatement<S>, W extends InputUsageStatement<V>, X extends InputUsageStatement<T>>
extends CompoundTransformer<S, V, T> implements TraceableTransformer<S, T, U, X> {

	// TODO Javadoc

	public TraceableCompoundTransformer(TraceableTransformer<S, V, U, W> transformer1, TraceableTransformer<V, T, W, X> transformer2) {
		super(transformer1, transformer2);
	}

	@Override
	public U traceTransformation(S input, X outputUsageStatement) {
		V input2 = transformer1.apply(input);
		W tracing2 = getTransformer2().traceTransformation(input2, outputUsageStatement);
		return getTransformer1().traceTransformation(input, tracing2);
	}

	@SuppressWarnings("unchecked")
	private TraceableTransformer<S, V, U, W> getTransformer1() {
		return (TraceableTransformer<S, V, U, W>) transformer1;
	}
	@SuppressWarnings("unchecked")
	private TraceableTransformer<V, T, W, X> getTransformer2() {
		return (TraceableTransformer<V, T, W, X>) transformer2;
	}
}
