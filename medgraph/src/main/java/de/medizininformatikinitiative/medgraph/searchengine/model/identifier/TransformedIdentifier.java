package de.medizininformatikinitiative.medgraph.searchengine.model.identifier;

import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.Transformer;

/**
 * An {@link Identifier} that has been transformed by a {@link Transformer}.
 *
 * @author Markus Budeus
 */
public class TransformedIdentifier<S, T> extends Identifier<T> {

	/**
	 * The original identifier which was transformed into this one.
	 */
	private final Identifier<S> original;
	/**
	 * The transformer which transformed the given original identifier into this one.
	 */
	private final Transformer<S, T> transformer;

	public TransformedIdentifier(T identifier, Identifier<S> original, Transformer<S, T> transformer) {
		super(identifier);
		this.original = original;
		this.transformer = transformer;
	}

	public Identifier<S> getOriginal() {
		return original;
	}

	public Transformer<S, T> getTransformer() {
		return transformer;
	}
}
