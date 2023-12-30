package de.tum.med.aiim.markusbudeus.matcher2.matchers;

import de.tum.med.aiim.markusbudeus.matcher2.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher2.matchers.model.Match;
import de.tum.med.aiim.markusbudeus.matcher2.provider.IdentifierProvider;

public abstract class Matcher<S, T> implements IMatcher<S, T> {

	@Override
	public Match<T> findMatch(HouselistEntry searchTerm, MatcherConfiguration<S, T> configuration) {
		return findMatch(configuration.getFeatureExtractor().apply(searchTerm), configuration.getIdentifierProvider());
	}

	protected abstract Match<T> findMatch(S searchTerm, IdentifierProvider<T> identifierProvider);

}
