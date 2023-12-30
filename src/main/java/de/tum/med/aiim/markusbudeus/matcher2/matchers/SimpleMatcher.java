package de.tum.med.aiim.markusbudeus.matcher2.matchers;

import de.tum.med.aiim.markusbudeus.matcher2.matchers.model.Match;
import de.tum.med.aiim.markusbudeus.matcher2.provider.IdentifierProvider;

public abstract class SimpleMatcher<S> extends Matcher<S, S> {

	protected abstract Match<S> findMatch(S searchTerm, IdentifierProvider<S> identifierProvider);

}
