package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Filtering;

import java.util.List;

/**
 * A {@link Judge}-implementation which only passes or denies specific {@link Matchable}s
 *
 * @author Markus Budeus
 */
public abstract class Filter implements Judge<Filtering> {

	@Override
	public Filtering judge(Matchable matchable, SearchQuery query) {
		return new Filtering(getName(), getDescription(), passesFilter(matchable, query));
	}

	@Override
	public List<Filtering> batchJudge(List<Matchable> matchables, SearchQuery query) {
		String name = getName();
		String desc = getDescription();
		return batchPassesFilter(matchables, query)
				.stream()
				.map(pass -> new Filtering(name, desc, pass))
				.toList();
	}

	/**
	 * Decides whether the given {@link Matchable} passes this filter.
	 *
	 * @param matchable the {@link Matchable} to run against this filter
	 * @param query     the query to consider when filtering
	 * @return true if the {@link Matchable} passes this filter, false otherwise
	 */
	protected abstract boolean passesFilter(Matchable matchable, SearchQuery query);

	/**
	 * Decides whether the given {@link Matchable}s pass this filter.
	 *
	 * @param matchables the {@link Matchable}s to run against this filter
	 * @param query      the query to consider when filtering
	 * @return A list containing true for each {@link Matchable} which passes the filter and false for each that does
	 * not
	 */
	protected abstract List<Boolean> batchPassesFilter(List<Matchable> matchables, SearchQuery query);

	/**
	 * Returns a short name of this filter.
	 */
	protected abstract String getName();

	/**
	 * Returns a short description of what this filter does.
	 */
	protected abstract String getDescription();

}
