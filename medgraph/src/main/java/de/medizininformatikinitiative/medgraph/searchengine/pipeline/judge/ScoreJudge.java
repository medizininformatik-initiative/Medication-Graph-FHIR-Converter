package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

import java.util.List;

/**
 * Simple {@link Judge}-implementation, which works by having a score assigned to each {@link Matchable}.
 *
 * @param <S> the type of {@link Matchable} this judge supports
 * @author Markus Budeus
 */
public abstract class ScoreJudge<S extends  Matchable> implements Judge<S, ScoreJudgementInfo> {

	@Override
	public ScoreJudgementInfo judge(S matchable, SearchQuery query) {
		return new ScoreJudgementInfo(judgeInternal(matchable, query));
	}

	@Override
	public List<ScoreJudgementInfo> batchJudge(List<? extends S> matchables, SearchQuery query) {
		return batchJudgeInternal(matchables, query)
				.stream()
				.map(ScoreJudgementInfo::new)
				.toList();
	}

	/**
	 * Returns the score of the given {@link Matchable} based on the given search query.
	 *
	 * @param matchable the {@link Matchable} to judge
	 * @param query     the {@link SearchQuery} to utilize for the judgement, if required
	 * @return the assigned score
	 */
	protected abstract double judgeInternal(S matchable, SearchQuery query);

	/**
	 * Returns the scores of the given {@link Matchable}s based on the given search query.
	 *
	 * @param matchables the {@link Matchable}s to judge
	 * @param query      the {@link SearchQuery} to utilize for the judgement, if required
	 * @return the assigned score for each {@link Matchable}, with each entry in the list being the score of the
	 * {@link Matchable} in the input list at the same position
	 */
	protected List<Double> batchJudgeInternal(List<? extends S> matchables, SearchQuery query) {
		return matchables.stream().map(m -> judgeInternal(m, query)).toList();
	}

}
