package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.IMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.jetbrains.annotations.NotNull;

/**
 * Any successful match produced by a {@link IMatcher}.
 *
 * @param <S> the type of search term identifier used
 * @param <T> the type of identifier that was matched against
 * @author Markus Budeus
 */
public abstract class Match<S, T> {

	@NotNull
	private final Identifier<S> searchTerm;
	@NotNull
	private final MappedIdentifier<T> matchedIdentifier;

	protected Match(@NotNull Identifier<S> searchTerm, @NotNull MappedIdentifier<T> matchedIdentifier) {
		this.searchTerm = searchTerm;
		this.matchedIdentifier = matchedIdentifier;
	}

	/**
	 * Returns the {@link MappedIdentifier} that was matched.
	 */
	@NotNull
	public MappedIdentifier<T> getMatchedIdentifier() {
		return matchedIdentifier;
	}

	/**
	 * Returns the search term identifier against which the matcher was run.
	 */
	@NotNull
	public Identifier<S> getSearchTerm() {
		return searchTerm;
	}
}
