package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.Transformer;

import java.util.stream.Stream;

/**
 * A {@link IdentifierProvider} which forwards the identifiers from the given provider and with the transformation
 * given by the {@link Transformer} applied to them.
 *
 * @author Markus Budeus
 */
public class TransformedProvider<S, T> implements IdentifierProvider<T> {

	private final IdentifierProvider<S> base;
	private final Transformer<S, T> transformer;

	public TransformedProvider(IdentifierProvider<S> base, Transformer<S, T> transformer) {
		this.base = base;
		this.transformer = transformer;
	}

	@Override
	public Stream<MappedIdentifier<T>> getIdentifiers() {
		return base.getIdentifiers().map(m -> new MappedIdentifier<>(transformer.apply(m.identifier), m.target));
	}
}
