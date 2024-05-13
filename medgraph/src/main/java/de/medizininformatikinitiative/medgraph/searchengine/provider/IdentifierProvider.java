package de.medizininformatikinitiative.medgraph.searchengine.provider;

import java.util.stream.Stream;

/**
 * Provides {@link MappedIdentifier}s, which are a term of any type mapped to a
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable}.
 *
 * @param <S> the type of identifier term
 * @author Markus Budeus
 */
public interface IdentifierProvider<S> {

	/**
	 * Returns the identifiers of this provider as a stream. Note this function generates a new stream
	 * every time it's called.
	 * @return a newly generated stream of identifiers
	 */
	Stream<MappedIdentifier<S>> getIdentifiers();

}
