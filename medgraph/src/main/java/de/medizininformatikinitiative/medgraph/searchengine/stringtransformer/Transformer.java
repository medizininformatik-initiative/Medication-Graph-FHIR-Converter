package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TransformedIdentifier;

import java.util.function.Function;

/**
 * A transformer is meant to transform identifier terms.
 *
 * @author Markus Budeus
 */
@FunctionalInterface
public interface Transformer<S, T> extends Function<S, T> {

	/**
	 * Transforms the given {@link Identifier} into a {@link TransformedIdentifier}.
	 *
	 * @param identifier the {@link Identifier}-instance to transform
	 * @return a new {@link TransformedIdentifier} holding information about the transformed identifier and the
	 * transformation that took place
	 */
	default TransformedIdentifier<S, T> apply(Identifier<S> identifier) {
		return new TransformedIdentifier<>(apply(identifier.getIdentifier()), identifier, this);
	}

	T apply(S source);

	/**
	 * Creates a new transformer by chaining this instance with the given next transformer.
	 */
	default <V> Transformer<S, V> and(Transformer<T, V> next) {
		return new CompoundTransformer<>(this, next);
	}

}
