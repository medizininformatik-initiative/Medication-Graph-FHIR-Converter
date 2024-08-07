package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Provides information about a score generated during the match.
 *
 * @author Markus Budeus
 */
public class ScoreMatchInfo implements MatchInfo, Comparable<ScoreMatchInfo> {

	private final double score;

	public ScoreMatchInfo(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		ScoreMatchInfo that = (ScoreMatchInfo) object;
		return Double.compare(score, that.score) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(score);
	}

	@Override
	public String toString() {
		return String.valueOf(score);
	}

	@Override
	public int compareTo(@NotNull ScoreMatchInfo o) {
		return Double.compare(score, o.score);
	}
}
