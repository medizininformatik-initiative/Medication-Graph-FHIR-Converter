package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;

import java.util.stream.Stream;

/**
 * An {@link MappedIdentifierStream} with parallel processing enabled.
 *
 * @author Markus Budeus
 */
public class ParallelMappedIdentifierStream<S, T extends Identifiable> implements MappedIdentifierStream<S ,T> {

	private final MappedIdentifierStream<S, T> source;

	public ParallelMappedIdentifierStream(MappedIdentifierStream<S, T> source) {
		this.source = source;
	}

	@Override
	public Stream<MappedIdentifier<S, T>> getIdentifiers() {
		return source.getIdentifiers().parallel();
	}

}
