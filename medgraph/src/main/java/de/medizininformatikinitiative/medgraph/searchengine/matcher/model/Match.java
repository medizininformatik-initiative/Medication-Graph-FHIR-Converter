package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.IMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Any successful match produced by a {@link IMatcher}.
 *
 * @param <S> the type of search term identifier used
 * @param <T> the type of identifier that was matched against
 * @author Markus Budeus
 */
public abstract class Match<S extends Identifier<?>, T extends Identifier<?>> {

	@NotNull
	private final S searchTerm;
	@NotNull
	private final T matchedIdentifier;

	protected Match(@NotNull S searchTerm, @NotNull T matchedIdentifier) {
		this.searchTerm = searchTerm;
		this.matchedIdentifier = matchedIdentifier;
	}

	/**
	 * Returns the target that was matched.
	 */
	@NotNull
	public T getMatchedIdentifier() {
		return matchedIdentifier;
	}

	/**
	 * Returns the search term identifier against which the matcher was run.
	 */
	@NotNull
	public S getSearchTerm() {
		return searchTerm;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Match<?, ?> match = (Match<?, ?>) object;
		return Objects.equals(searchTerm, match.searchTerm) && Objects.equals(matchedIdentifier,
				match.matchedIdentifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(searchTerm, matchedIdentifier);
	}
}
