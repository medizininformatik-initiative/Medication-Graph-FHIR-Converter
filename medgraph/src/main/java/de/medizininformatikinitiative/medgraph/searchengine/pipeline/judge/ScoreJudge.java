package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Simple {@link Judge}-implementation, which works by having a score assigned to each
 *
 * @author Markus Budeus
 */
public abstract class ScoreJudge implements Judge<ScoredJudgement> {

	@Nullable
	private final Double passingScore;

	public ScoreJudge(@Nullable Double passingScore) {
		this.passingScore = passingScore;
	}

	@Override
	public ScoredJudgement judge(Matchable matchable, SearchQuery query) {
		return new ScoredJudgement(toString(), getDescription(), judgeInternal(matchable, query), passingScore);
	}

	@Override
	public List<ScoredJudgement> batchJudge(List<Matchable> matchables, SearchQuery query) {
		String name = toString();
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
	protected List<Double> batchJudgeInternal(List<Matchable> matchables, SearchQuery query) {
		return matchables.stream().map(m -> judgeInternal(m, query)).toList();
	}

	/**
	 * Returns the minimun required score to pass this judge or null if there is no minimum required score to pass.
	 */
	@Nullable
	public Double getPassingScore() {
		return passingScore;
	}
}
