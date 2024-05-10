package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;

import java.util.LinkedList;

/**
 * Carrier object for {@link Matchable} during the matching process. Stores information about
 * judgements and transformations applied to the object during the matching.
 *
 * @author Markus Budeus
 */
public abstract class MatchingObject {

	private final Matchable object;
	private final LinkedList<Judgement> appliedJudgements = new LinkedList<>();

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 * @param object the object to be managed by this instance
	 */
	protected MatchingObject(Matchable object) {
		this.object = object;
	}

	/**
	 * Returns the {@link Matchable} managed by this instance.
	 */
	public Matchable getObject() {
		return object;
	}

	/**
	 * Returns all judgements applied to this object in the order of being applied.
	 */
	public LinkedList<Judgement> getAppliedJudgements() {
		return appliedJudgements;
	}

	/**
	 * Adds the given judgement to this object, effectively stating the given judgement has been applied
	 * to this object.
	 * @param judgement the judgement to document having been applied to this object
	 */
	public void addJudgement(Judgement judgement) {
		appliedJudgements.add(judgement);
	}

}
