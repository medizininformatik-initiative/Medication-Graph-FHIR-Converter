package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;

import java.util.List;

/**
 * Simple {@link Judge}-implementation, which works by having a score assigned to each
 *
 * @author Markus Budeus
 */
public abstract class SimpleJudge implements Judge<ScoredJudgement> {

	private final Double passingScore;

	public SimpleJudge(Double passingScore) {
		this.passingScore = passingScore;
	}

	@Override
	public ScoredJudgement judge(Matchable matchable, SearchQuery query) {
		return new ScoredJudgement(getName(), getDescription(), judgeInternal(matchable, query), passingScore);
	}

	@Override
	public List<ScoredJudgement> batchJudge(List<Matchable> matchables, SearchQuery query) {
		String name = getName();
		String desc = getDescription();
		return batchJudgeInternal(matchables, query)
				.stream()
				.map(score -> new ScoredJudgement(name, desc, score, passingScore))
				.toList();
	}

	/**
	 * Returns the score of the given {@link Matchable} based on the given search query.
	 *
	 * @param matchable the {@link Matchable} to judge
	 * @param query     the {@link SearchQuery} to utilize for the judgement, if required
	 * @return the assigned score
	 */
	protected abstract double judgeInternal(Matchable matchable, SearchQuery query);

	/**
	 * Returns the scores of the given {@link Matchable}s based on the given search query.
	 *
	 * @param matchables the {@link Matchable}s to judge
	 * @param query      the {@link SearchQuery} to utilize for the judgement, if required
	 * @return the assigned score for each {@link Matchable}, with each entry in the list being the score of the
	 * {@link Matchable} in the input list at the same position
	 */
	protected abstract List<Double> batchJudgeInternal(List<Matchable> matchables, SearchQuery query);

	/**
	 * Returns a short name of this judge.
	 */
	protected abstract String getName();

	/**
	 * Returns a short description of what this judge does.
	 */
	protected abstract String getDescription();

}
