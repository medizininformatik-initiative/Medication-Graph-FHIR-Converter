package de.tum.med.aiim.markusbudeus.matcher.matchers;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.matchers.model.Match;

public interface IMatcher<S ,T, R extends Match<T>> {

	/**
	 * Attempts to match the given house list entry to identifiers of the given provider by whatever metric this
	 * specific matcher uses.
	 */
	R findMatch(HouselistEntry searchTerm, MatcherConfiguration<S, T> configuration);

}
