package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.MatchingPipelineComponent;

import java.util.List;

/**
 * An instance of this class judges {@link Matchable}s using any metric and possibly using information from the search
 * query.
 *
 * @param <S> the type of {@link Matchable}s this instance is capable of judging
 * @param <T> the type of judgement produced by this instance
 * @author Markus Budeus
 * @see Judgement
 */
public interface Judge<S extends Matchable, T extends Judgement> extends MatchingPipelineComponent {

	/**
	 * Judges the given {@link Matchable} using information from the {@link SearchQuery} if required. When judging
	 * multiple {@link Matchable}s, consider using {@link #batchJudge(List, SearchQuery)}.
	 *
	 * @param matchable the {@link Matchable} to judge
	 * @param query     the {@link SearchQuery} from which to take information needed for the judgement
	 * @return the {@link Judgement}
	 */
	T judge(S matchable, SearchQuery query);

	/**
	 * Like {@link #judge(Matchable, SearchQuery)}, but runs on multiple {@link Matchable}s at once to allow the
	 * implementation to optimize, e.g. by batching expensive database accesses.
	 *
	 * @param matchables the {@link Matchable}s to judge
	 * @param query      the {@link SearchQuery} from which to take information needed for the judgement
	 * @return a list of {@link Judgement}s, each one corresponding to the {@link Matchable} in the input list at the
	 * same position
	 */
	default List<T> batchJudge(List<? extends S> matchables, SearchQuery query) {
		return matchables.stream().map(m -> judge(m, query)).toList();
	}

}
