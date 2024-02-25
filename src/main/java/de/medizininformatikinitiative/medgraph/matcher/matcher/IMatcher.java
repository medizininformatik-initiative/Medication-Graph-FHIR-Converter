package de.medizininformatikinitiative.medgraph.matcher.matcher;

import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.matcher.model.Match;

/**
 * A matcher which can be used to find matching objects for a given house list entry. It works by extracting a search
 * term of type S from the {@link HouselistEntry} and comparing it against identifiers of type T, provided by a
 * corresponding {@link de.medizininformatikinitiative.medgraph.matcher.provider.IdentifierProvider}. S and T are often
 * the same, but this is not strictly necessary. For example, you might have a string search term and match it against
 * sets of strings as identifiers, by simply matching all identifiers who contain the given search term.
 *
 * @param <S> the type of search term used for matching
 * @param <T> the type of identifiers against whom the search term is matched
 * @param <R> the type of match result produced
 * @author Markus Budeus
 */
public interface IMatcher<S ,T, R extends Match<T>> {

	/**
	 * Attempts to match the given house list entry to identifiers of the given provider by whatever metric this
	 * specific matcher uses.
	 */
	R findMatch(HouselistEntry searchTerm, MatcherConfiguration<S, T> configuration);

}
