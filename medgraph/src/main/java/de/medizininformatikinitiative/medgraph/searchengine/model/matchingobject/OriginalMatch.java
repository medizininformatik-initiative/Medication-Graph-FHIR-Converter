package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import org.jetbrains.annotations.NotNull;

/**
 * Original match found in the first step of the matching algorithm.
 *
 * @author Markus Budeus
 */
public class OriginalMatch extends MatchingObject {

	/**
	 * The source where this match originated from.
	 */
	@NotNull
	private final Source source;

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 *
	 * @param object the object to be managed by this instance
	 */
	public OriginalMatch(Matchable object) {
		this(object, Source.UNKNOWN);
	}

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 *
	 * @param object the object to be managed by this instance
	 * @param source the source this match originated from
	 */
	public OriginalMatch(@NotNull Matchable object, @NotNull Source source) {
		super(object);
		this.source = source;
	}

	@NotNull
	public Source getSource() {
		return source;
	}
}
