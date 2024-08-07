package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import java.util.Objects;

/**
 * Result of a {@link ScoreJudge} judging an object.
 *
 * @author Markus Budeus
 */
public class ScoreJudgementInfo implements JudgementInfo {

	/**
	 * The score that was applied by the judge.
	 */
	private final double score;

	public ScoreJudgementInfo(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		ScoreJudgementInfo that = (ScoreJudgementInfo) object;
		return Double.compare(score, that.score) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(score);
	}
}
