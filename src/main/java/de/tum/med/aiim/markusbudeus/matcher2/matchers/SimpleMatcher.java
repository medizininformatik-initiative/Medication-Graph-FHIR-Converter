package de.tum.med.aiim.markusbudeus.matcher2.matchers;

import de.tum.med.aiim.markusbudeus.matcher2.matchers.model.Match;
import de.tum.med.aiim.markusbudeus.matcher2.provider.IdentifierProvider;

public abstract class SimpleMatcher<S, R extends Match<S>> extends Matcher<S, S, R> {

	protected abstract R findMatch(S searchTerm, IdentifierProvider<S> identifierProvider);

}
