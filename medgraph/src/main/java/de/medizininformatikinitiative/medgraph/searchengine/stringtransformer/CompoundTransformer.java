package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TransformedIdentifier;

/**
 * Two {@link Transformer}s chained together.
 *
 * @param <S> the source type of the first transformer
 * @param <V> the target type of the first transformer and source type of the second transformer
 * @param <T> the target type of the second transformer
 * @author Markus Budeus
 */
public class CompoundTransformer<S,V,T> implements Transformer<S,T> {

	protected final Transformer<S,V> transformer1;
	protected final Transformer<V,T> transformer2;

	CompoundTransformer(Transformer<S, V> transformer1, Transformer<V, T> transformer2) {
		this.transformer1 = transformer1;
		this.transformer2 = transformer2;
	}

	@Override
	public TransformedIdentifier<?, T> apply(Identifier<S> identifier) {
		return transformer2.apply(transformer1.apply(identifier));
	}

	@Override
	public T apply(S source) {
		return transformer2.apply(transformer1.apply(source));
	}

}
