package de.medizininformatikinitiative.medgraph.matcher.matcher;

import de.medizininformatikinitiative.medgraph.matcher.matcher.model.Match;
import de.medizininformatikinitiative.medgraph.matcher.provider.IdentifierProvider;

/**
 * A simple matcher uses the same type of search term and identifiers which the search term is matched against.
 * @param <S> the type of search term and identifiers used
 * @param <R> the type of match result provided
 * @author Markus Budeus
 */
public abstract class SimpleMatcher<S, R extends Match<S>> extends Matcher<S, S, R> {

	protected abstract R findMatch(S searchTerm, IdentifierProvider<S> identifierProvider);

}
