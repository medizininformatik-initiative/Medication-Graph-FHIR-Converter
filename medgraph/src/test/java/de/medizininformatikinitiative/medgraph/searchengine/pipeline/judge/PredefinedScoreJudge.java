package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

import java.util.Map;

/**
 * Test-only implementation of {@link ScoreJudge}, which assigns a pre-defined score to each object.
 *
 * @author Markus Budeus
 */
public class PredefinedScoreJudge extends ScoreJudge {

	public static final String DESC = "Assigns a pre-defined score";

	private final Map<Matchable, Double> scoreMap;

	public PredefinedScoreJudge(Map<Matchable, Double> scoreMap, Double passingScore) {
		super(passingScore);
		this.scoreMap = scoreMap;
	}

	@Override
	protected double judgeInternal(Matchable matchable, SearchQuery query) {
		Double score = scoreMap.get(matchable);
		if (score == null) score = 0.0;
		return score;
	}

	@Override
	public String getDescription() {
		return DESC;
	}
}
