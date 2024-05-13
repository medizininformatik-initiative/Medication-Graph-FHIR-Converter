package de.medizininformatikinitiative.medgraph.searchengine.provider;

import java.util.stream.Stream;

/**
 * An {@link IdentifierStream} with parallel processing enabled.
 *
 * @author Markus Budeus
 */
public class ParallelIdentifierStream<S> implements IdentifierStream<S> {

	private final IdentifierStream<S> source;

	public ParallelIdentifierStream(IdentifierStream<S> source) {
		this.source = source;
	}

	@Override
	public Stream<MappedIdentifier<S>> getIdentifiers() {
		return source.getIdentifiers().parallel();
	}

}
