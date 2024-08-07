package de.medizininformatikinitiative.medgraph.searchengine.model;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.JudgedObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgementStep;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link JudgedObject} which has been judged via a {@link ScoredJudgementStep}.
 *
 * @author Markus Budeus
 */
public class ScoreJudgedObject<S extends Matchable> extends JudgedObject<S> {

	public ScoreJudgedObject(@NotNull MatchingObject<S> source, ScoredJudgementStep judgement) {
		super(source, judgement, judgement.getConfiguration().calculateScore(source.getScore(), judgement.getScore()));
	}

	@Override
	public ScoredJudgementStep getJudgement() {
		return (ScoredJudgementStep) super.getJudgement();
	}
}
