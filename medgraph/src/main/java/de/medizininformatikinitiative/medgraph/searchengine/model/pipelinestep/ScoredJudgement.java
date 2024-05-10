package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.Judge;

import java.util.OptionalDouble;

/**
 * A judgement which works through a score being applied by the {@link Judge}.
 *
 * @author Markus Budeus
 */
public class ScoredJudgement implements Judgement {

	private final String name;
	private final String description;
	private final double score;
	private final Double passingScore;

	public ScoredJudgement(String name, String description, double score, Double passingScore) {
		this.name = name;
		this.description = description;
		this.score = score;
		this.passingScore = passingScore;
	}


	/**
	 * Returns the issued score.
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Returns the minimum score required to pass the judgement. May be absent, indicating that any score is a passing
	 * score.
	 */
	public OptionalDouble getPassingScore() {
		if (passingScore != null) return OptionalDouble.of(passingScore);
		return OptionalDouble.empty();
	}

	@Override
	public boolean isPassed() {
		return passingScore == null || score >= passingScore;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
