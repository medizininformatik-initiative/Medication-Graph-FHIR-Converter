package de.tum.med.aiim.markusbudeus.matcher2.matchers;

import de.tum.med.aiim.markusbudeus.matcher2.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher2.matchers.model.Match;

public interface IMatcher<S ,T, R extends Match<T>> {

	/**
	 * Attempts to match the given house list entry to identifiers of the given provider by whatever metric this
	 * specific matcher uses.
	 */
	R findMatch(HouselistEntry searchTerm, MatcherConfiguration<S, T> configuration);

}
