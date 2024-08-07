package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Extension of {@link Match} which provided additional information about how the match came to be.
 *
 * @author Markus Budeus
 */
public class DetailedMatch<S extends Identifier<?>, T extends Identifier<?>, I extends MatchInfo> extends Match<S, T> {

	/**
	 * Additional information associated with this match. What this exactly is depends on the matcher used.
	 */
	private final I matchInfo;

	public DetailedMatch(@NotNull S searchTerm,
	                        @NotNull T matchedIdentifier,
	                        @NotNull I matchInfo) {
		super(searchTerm, matchedIdentifier);
		this.matchInfo = matchInfo;
	}

	public I getMatchInfo() {
		return matchInfo;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		DetailedMatch<?, ?, ?> that = (DetailedMatch<?, ?, ?>) object;
		return Objects.equals(matchInfo, that.matchInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), matchInfo);
	}
}
