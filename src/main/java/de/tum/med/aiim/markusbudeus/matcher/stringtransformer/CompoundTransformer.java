package de.tum.med.aiim.markusbudeus.matcher.stringtransformer;

public class CompoundTransformer<S,V,T> implements Transformer<S,T> {

	private final Transformer<S,V> transformer1;
	private final Transformer<V,T> transformer2;

	CompoundTransformer(Transformer<S, V> transformer1, Transformer<V, T> transformer2) {
		this.transformer1 = transformer1;
		this.transformer2 = transformer2;
	}

	@Override
	public T transform(S source) {
		return transformer2.transform(transformer1.transform(source));
	}

}
