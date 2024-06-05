package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;

/**
 * @author Markus Budeus
 */
public class PharmaceuticalDoseFormJudge extends ScoreJudge {

	// TODO Duh...
	public PharmaceuticalDoseFormJudge(Double passingScore) {
		super(passingScore);
	}

	@Override
	protected double judgeInternal(Matchable matchable, SearchQuery query) {
		// TODO
		return 0;
	}

	@Override
	public String getDescription() {
		return "Judges products based on how many of their drugs' pharmaceutical dose forms match the searched " +
				"dose forms.";
	}
}
