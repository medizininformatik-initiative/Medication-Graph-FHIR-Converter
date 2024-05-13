package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import java.util.function.Function;

/**
 * A transformer is meant to transform identifier terms.
 *
 * @author Markus Budeus
 */
public interface Transformer<S,T> extends Function<S, T> {

	Transformer<String, String> IDENTITY = new IdentityTransformer();

	T apply(S source);

	/**
	 * Creates a new transformer by chaining this instance with the given next transformer.
	 */
	default <V> Transformer<S,V> and(Transformer<T,V> next) {
		return new CompoundTransformer<>(this, next);
	}

}
