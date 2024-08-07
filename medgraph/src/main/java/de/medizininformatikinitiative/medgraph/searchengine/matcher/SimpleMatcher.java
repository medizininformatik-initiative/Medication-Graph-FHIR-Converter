package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.MatchInfo;

/**
 * A simple matcher uses the same type of search term and identifiers which the search term is matched against.
 *
 * @param <S> the type of search term and identifiers used
 * @param <R> the type of match info provided
 * @author Markus Budeus
 */
public abstract class SimpleMatcher<S, R extends MatchInfo> extends ExtendedMatcher<S, S, R> {

}
