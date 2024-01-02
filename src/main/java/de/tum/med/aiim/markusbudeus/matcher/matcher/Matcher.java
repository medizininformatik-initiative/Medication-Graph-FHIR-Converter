package de.tum.med.aiim.markusbudeus.matcher.matcher;

import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.matcher.model.Match;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

public abstract class Matcher<S, T, R extends Match<T>> implements IMatcher<S, T, R> {

	@Override
	public R findMatch(HouselistEntry searchTerm, MatcherConfiguration<S, T> configuration) {
		return findMatch(configuration.getFeatureExtractor().apply(searchTerm), configuration.getIdentifierProvider());
	}

	protected abstract R findMatch(S searchTerm, IdentifierProvider<T> identifierProvider);

}
