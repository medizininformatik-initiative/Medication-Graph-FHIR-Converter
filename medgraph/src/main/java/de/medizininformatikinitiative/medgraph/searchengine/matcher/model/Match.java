package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.IMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

/**
 * Any successful match produced by a {@link IMatcher}.
 *
 * @author Markus Budeus
 */
public abstract class Match<S> {

	private final MappedIdentifier<S> matchedIdentifier;

	protected Match(MappedIdentifier<S> matchedIdentifier) {
		this.matchedIdentifier = matchedIdentifier;
	}

	/**
	 * Returns the {@link MappedIdentifier} that was matched.
	 */
	public MappedIdentifier<S> getMatchedIdentifier() {
		return matchedIdentifier;
	}

}
