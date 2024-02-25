package de.medizininformatikinitiative.medgraph.matcher.matcher;

import de.medizininformatikinitiative.medgraph.matcher.matcher.model.Match;
import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.provider.IdentifierProvider;

/**
 * @see IMatcher
 * @author Markus Budeus
 */
public abstract class Matcher<S, T, R extends Match<T>> implements IMatcher<S, T, R> {

	@Override
	public R findMatch(HouselistEntry searchTerm, MatcherConfiguration<S, T> configuration) {
		return findMatch(configuration.getFeatureExtractor().apply(searchTerm), configuration.getIdentifierProvider());
	}

	protected abstract R findMatch(S searchTerm, IdentifierProvider<T> identifierProvider);

}
