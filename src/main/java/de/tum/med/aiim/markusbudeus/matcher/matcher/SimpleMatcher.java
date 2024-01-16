package de.tum.med.aiim.markusbudeus.matcher.matcher;

import de.tum.med.aiim.markusbudeus.matcher.matcher.model.Match;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

public abstract class SimpleMatcher<S, R extends Match<S>> extends Matcher<S, S, R> {

	protected abstract R findMatch(S searchTerm, IdentifierProvider<S> identifierProvider);

}