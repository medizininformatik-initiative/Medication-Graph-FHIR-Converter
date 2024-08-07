package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

import java.util.List;

/**
 * A {@link Judge}-implementation which only passes or denies specific {@link Matchable}s
 *
 * @param <S> the type of {@link Matchable} this filter supports
 * @author Markus Budeus
 */
public interface Filter<S extends Matchable> extends Judge<S, FilteringInfo> {

	@Override
	default FilteringInfo judge(S matchable, SearchQuery query) {
		return new FilteringInfo(passesFilter(matchable, query));
	}

	@Override
	default List<FilteringInfo> batchJudge(List<? extends S> matchables, SearchQuery query) {
		return batchPassesFilter(matchables, query)
				.stream()
				.map(FilteringInfo::new)
				.toList();
	}

	/**
	 * Decides whether the given {@link Matchable} passes this filter.
	 *
	 * @param matchable the {@link Matchable} to run against this filter
	 * @param query     the query to consider when filtering
	 * @return true if the {@link Matchable} passes this filter, false otherwise
	 */
	boolean passesFilter(S matchable, SearchQuery query);

	/**
	 * Decides whether the given {@link Matchable}s pass this filter.
	 *
	 * @param matchables the {@link Matchable}s to run against this filter
	 * @param query      the query to consider when filtering
	 * @return A list containing true for each {@link Matchable} which passes the filter and false for each that does
	 * not
	 */
	default List<Boolean> batchPassesFilter(List<? extends S> matchables, SearchQuery query) {
		return matchables.stream().map(matchable -> passesFilter(matchable, query)).toList();
	}

}
