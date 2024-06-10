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
	 * The origin where this match originated from.
	 */
	@NotNull
	private final Origin origin;

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 *
	 * @param object the object to be managed by this instance
	 */
	public OriginalMatch(Matchable object) {
		this(object, Origin.UNKNOWN);
	}

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 *
	 * @param object the object to be managed by this instance
	 * @param origin the source this match originated from
	 */
	public OriginalMatch(@NotNull Matchable object, @NotNull Origin origin) {
		super(object);
		this.origin = origin;
	}

	@NotNull
	public Origin getOrigin() {
		return origin;
	}
}
