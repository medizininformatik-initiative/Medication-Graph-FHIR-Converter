package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

import java.util.stream.Stream;

/**
 * Provides {@link MappedIdentifier}s, which are a term of any type mapped to a
 * {@link Matchable}.
 *
 * @param <S> the type of identifier term
 * @author Markus Budeus
 */
public interface IdentifierProvider<S> {

	/**
	 * Returns the identifiers of this provider as a stream. Note this function generates a new stream
	 * every time it's called, which also means that any steps in the stream pipeline need to be processed
	 * every time a stream is generated like this.
	 *
	 * @return a newly generated stream of identifiers
	 */
	Stream<MappedIdentifier<S>> getIdentifiers();

}
