package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgeConfiguration;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgementInfo;

import java.util.OptionalDouble;

/**
 * Represents the info that a {@link de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.Judge} was
 * applied to the {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject}
 * carrying this instance.
 *
 * @author Markus Budeus
 */
public class ScoredJudgementStep implements Judgement {

	private final String name;
	private final String description;
	/**
	 * Additional information provided by the judge.
	 */
	private final ScoreJudgementInfo scoreJudgementInfo;
	/**
	 * The configuration that was set when using the judge.
	 */
	private final ScoreJudgeConfiguration configuration;

	public ScoredJudgementStep(String name, String description, ScoreJudgementInfo scoreJudgementInfo,
	                           ScoreJudgeConfiguration configuration) {
		this.name = name;
		this.description = description;
		this.scoreJudgementInfo = scoreJudgementInfo;
		this.configuration = configuration;
	}

	/**
	 * Returns the issued score.
	 */
	public double getScore() {
		return scoreJudgementInfo.getScore();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public boolean passed() {
		Double passingScore = configuration.passingScore();
		return passingScore == null || getScore() >= passingScore;
	}

	@Override
	public ScoreJudgementInfo getJudgementInfo() {
		return scoreJudgementInfo;
	}

	public ScoreJudgeConfiguration getConfiguration() {
		return configuration;
	}
}
