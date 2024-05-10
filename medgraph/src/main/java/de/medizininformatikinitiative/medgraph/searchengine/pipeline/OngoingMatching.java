package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Filtering;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.Judge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.BinarySortDirective;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.ScoreSortDirective;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.SubSortingTree;

import java.util.List;

/**
 * This class manages intermediate matching results during a run of the matching algorithm.
 * It exposes methods which can be used to apply matching pipeline steps to the intermediate results, thereby
 * executing parts of the matching algorithm.
 *
 * @author Markus Budeus
 */
public class OngoingMatching {

	private final SubSortingTree<MatchingObject> currentMatches;
	private final SearchQuery query;

	public OngoingMatching(List<OriginalMatch> matchList, SearchQuery query) {
		currentMatches = new SubSortingTree<>(matchList);
		this.query = query;
	}

	/**
	 * Filters the current matches using the given filter.
	 *
	 * @param judge          the judge to use
	 * @param ensureSurvival if this is true, it prevents the elimination of all matches in case no match passes the
	 *                       judgement; instead, nothing gets eliminated, although the failure to pass the judgement is
	 *                       still documented
	 */
	public void applyScoreJudge(ScoreJudge judge, boolean ensureSurvival) {
		boolean anyPass = judgeMatches(judge);
		Double retainThreshold = (anyPass || !ensureSurvival) ? judge.getPassingScore() : null;
		sortMatchesByLatestScoredJudgement(judge.getName(), retainThreshold);
	}

	/**
	 * Filters the current matches using the given filter.
	 *
	 * @param filter         the filter which to use
	 * @param ensureSurvival if this is true, it prevents the elimination of all matches in case no match passes the
	 *                       filter; instead, nothing gets eliminated, although the failure to pass the filter is still
	 *                       documented
	 */
	public void applyFilter(Judge<? extends Filtering> filter, boolean ensureSurvival) {
		boolean anyPass = judgeMatches(filter);
		if (anyPass || !ensureSurvival) {
			removeMatchesWhichFailedTheLastFiltering(filter.getName());
		}
	}

	/**
	 * Runs the given judge against all current matches and assigns its judgement to the corresponding
	 * {@link MatchingObject}s.
	 *
	 * @param judge the judge to use
	 * @return true, if at least one object has passed the judgement, false otherwise
	 */
	private boolean judgeMatches(Judge<?> judge) {
		List<MatchingObject> objects = currentMatches.getContents();
		List<Matchable> matchables = objects.stream().map(MatchingObject::getObject).toList();
		List<? extends Judgement> judgements = judge.batchJudge(matchables, query);
		assert judgements.size() == objects.size();
		boolean atLeastOnePass = false;
		for (int i = 0; i < objects.size(); i++) {
			Judgement judgement = judgements.get(i);
			objects.get(i).addJudgement(judgement);
			atLeastOnePass = atLeastOnePass || judgement.isPassed();
		}
		return atLeastOnePass;
	}

	/**
	 * Applies a sorting step to the current matches, removing all which have not passed the last applied judgement.
	 * Requires that at least one judgement has been applied and no result transformation has happened since then.
	 *
	 * @param name the name to apply to the sort directive
	 */
	private void removeMatchesWhichFailedTheLastFiltering(String name) {
		currentMatches.applySortingStep(
				new BinarySortDirective<>(name,
						matchingObject -> matchingObject.getAppliedJudgements().getLast().isPassed(),
						true)
		);
	}

	/**
	 * Applies a sorting step to the current matches, sorting along the score of the last applied judgement. Requires
	 * that the last applied judgement creates {@link ScoredJudgement}-instances and no result transformation has
	 * happened since then.
	 *
	 * @param name            the name to apply to the sort directive
	 * @param retainThreshold the retain threshold to use
	 */
	private void sortMatchesByLatestScoredJudgement(String name, Double retainThreshold) {
		currentMatches.applySortingStep(
				new ScoreSortDirective<>(name,
						matchingObject -> ((ScoredJudgement) matchingObject.getAppliedJudgements()
						                                                   .getLast()).getScore(),
						retainThreshold)
		);
	}

	/**
	 * Returns the current intermediate matches.
	 */
	public List<MatchingObject> getCurrentMatches() {
		return currentMatches.getContents();
	}

}
