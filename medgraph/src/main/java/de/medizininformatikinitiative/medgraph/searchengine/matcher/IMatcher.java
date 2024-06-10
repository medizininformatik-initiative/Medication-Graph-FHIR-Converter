package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierProvider;

import java.util.stream.Stream;

/**
 * A matcher which can be used to find matching objects for a given search term. It works by comparing the search term
 * against identifiers of type S, provided by a corresponding {@link IdentifierProvider}. S and T are often the same,
 * but this is not strictly necessary. For example, you might have a string search term and match it against sets of
 * strings as identifiers, by simply matching all identifiers who contain the given search term.
 *
 * @param <S> the type of search term the matcher uses
 * @param <T> the type of identifiers the matcher is capable of matching against
 * @param <R> the type of match results produced
 * @author Markus Budeus
 */
public interface IMatcher<S, T, R extends Match<S, T>> {

	/**
	 * Matches the identifiers from the given {@link IdentifierProvider} against the search term.
	 *
	 * @param searchTerm the search term to match against the identifiers
	 * @param provider   the provider from which to take identifiers to match the search term against
	 * @return a stream of matches found by the matcher
	 */
	Stream<R> match(Identifier<S> searchTerm, IdentifierProvider<T> provider);

}
