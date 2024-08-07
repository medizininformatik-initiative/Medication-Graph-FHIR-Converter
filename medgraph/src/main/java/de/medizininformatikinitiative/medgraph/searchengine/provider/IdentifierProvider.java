package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;

import java.util.stream.Stream;

/**
 * Provides {@link Identifier}s.
 *
 * @param <S> the type of identifiers provided
 * @author Markus Budeus
 */
public interface IdentifierProvider<S extends Identifier<?>> {

	/**
	 * Returns the identifiers of this provider as a stream. Note this function generates a new stream
	 * every time it's called, which also means that any steps in the stream pipeline need to be processed
	 * every time a stream is generated like this.
	 *
	 * @return a newly generated stream of identifiers
	 */
	Stream<S> getIdentifiers();

}
