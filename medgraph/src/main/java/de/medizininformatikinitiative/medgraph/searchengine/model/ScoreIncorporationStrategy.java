package de.medizininformatikinitiative.medgraph.searchengine.model;

import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;

/**
 * Represents a strategy how a
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.JudgedObject JudgedObject}
 * calculates its score based on its base object score and the score assigned by the corresponding
 * {@link ScoredJudgement}.
 *
 * @author Markus Budeus
 */
@FunctionalInterface
public interface ScoreIncorporationStrategy {

	/**
	 * The assigned score is the base object's score plus the score assigned by the judgement.
	 */
	ScoreIncorporationStrategy ADD = Double::sum;
	/**
	 * The assigned score is the base object's score multiplied by the judgement score.
	 */
	ScoreIncorporationStrategy MULTIPLY = (a, b) -> a * b;

	/**
	 * Calculates the score to assign to a
	 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.JudgedObject} whose base object
	 * has the given score and whose judgement has the other given score
	 *
	 * @param baseObjectScore the score of the base object which was judged
	 * @param judgementScore  the score assigned to that object by the judge
	 * @return the score to assign to the judged object
	 */
	double calculateScore(double baseObjectScore, double judgementScore);

}
