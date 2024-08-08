package de.medizininformatikinitiative.medgraph.searchengine.model;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.JudgedObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgementStep;

/**
 * Represents a strategy how a {@link JudgedObject} calculates its score based on its base object score and the score
 * assigned by the corresponding {@link ScoredJudgementStep}.
 *
 * @author Markus Budeus
 */
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
	 * The assigned score is the judgement score, ignoring the base object's score.
	 */
	ScoreIncorporationStrategy OVERWRITE = (a, b) -> b;

	/**
	 * Calculates the score to assign to a {@link JudgedObject} whose base object has the given score and whose
	 * judgement has the other given score
	 *
	 * @param baseObjectScore the score of the base object which was judged
	 * @param judgementScore  the score assigned to that object by the judge
	 * @return the score to assign to the judged object
	 */
	double calculateScore(double baseObjectScore, double judgementScore);

}
