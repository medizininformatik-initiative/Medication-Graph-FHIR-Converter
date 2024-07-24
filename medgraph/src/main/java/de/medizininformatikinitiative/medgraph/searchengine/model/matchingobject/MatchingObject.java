package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Carrier object for {@link Matchable} during the matching process. Stores information about
 * judgements and transformations applied to the object during the matching as well as a global score assigned
 * to this objecdt.
 *
 * @param <T> the type of object carried
 * @author Markus Budeus
 */
public abstract class MatchingObject<T extends Matchable> {

	@NotNull
	private final T object;
	@NotNull
	private final LinkedList<Judgement> appliedJudgements = new LinkedList<>();
	/**
	 * The current score of this match.
	 */
	private final double score;

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 * @param object the object to be managed by this instance
	 */
	protected MatchingObject(@NotNull T object, double score) {
		this.object = object;
		this.score = score;
	}

	/**
	 * Returns the {@link Matchable} managed by this instance.
	 */
	@NotNull
	public T getObject() {
		return object;
	}

	/**
	 * Returns the score assigned to this match at the current time.
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Returns all judgements applied to this object in the order of being applied.
	 */
	@NotNull
	public List<Judgement> getAppliedJudgements() {
		return Collections.unmodifiableList(appliedJudgements);
	}

	/**
	 * Adds the given judgement to this object, effectively stating the given judgement has been applied
	 * to this object.
	 * @param judgement the judgement to document having been applied to this object
	 */
	public void addJudgement(Judgement judgement) {
		appliedJudgements.add(judgement);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;
		MatchingObject<?> object1 = (MatchingObject<?>) other;
		return Objects.equals(object, object1.object) && Objects.equals(appliedJudgements,
				object1.appliedJudgements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(object, appliedJudgements);
	}
}
