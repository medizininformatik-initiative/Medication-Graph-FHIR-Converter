package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.Transformer;

import java.util.stream.Stream;

/**
 * A {@link IdentifierProvider} which forwards the identifiers from the given provider and with the transformation
 * given by the {@link Transformer} applied to them.
 *
 * @author Markus Budeus
 */
public class TransformedMappedIdentifierStream<S, Q, T extends Identifiable> implements MappedIdentifierStream<Q, T> {

	private final IdentifierProvider<? extends MappedIdentifier<S, T>> base;
	private final Transformer<S, Q> transformer;

	public TransformedMappedIdentifierStream(IdentifierProvider<? extends MappedIdentifier<S, T>> base, Transformer<S, Q> transformer) {
		this.base = base;
		this.transformer = transformer;
	}

	@Override
	public Stream<MappedIdentifier<Q, T>> getIdentifiers() {
		return base.getIdentifiers().map(m -> new MappedIdentifier<>(transformer.apply(m.trackableIdentifier), m.target));
	}
}
