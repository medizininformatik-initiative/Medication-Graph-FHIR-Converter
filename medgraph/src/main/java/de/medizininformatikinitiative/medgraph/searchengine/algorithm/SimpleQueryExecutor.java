package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.InitialMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining.MatchRefiner;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge;

import java.util.*;
import java.util.stream.Stream;

/**
 * Takes care of running a whole search by chaining an {@link InitialMatchFinder} and a {@link MatchRefiner}.
 *
 * @author Markus Budeus
 */
public class SimpleQueryExecutor implements QueryExecutor {

	private final InitialMatchFinder initialMatchFinder;
	private final MatchRefiner matchRefiner;

	public SimpleQueryExecutor(InitialMatchFinder initialMatchFinder, MatchRefiner matchRefiner) {
		this.initialMatchFinder = initialMatchFinder;
		this.matchRefiner = matchRefiner;
	}

	public List<MatchingObject<?>> executeQuery(RefinedQuery query) {
		List<MatchingObject<?>> initialMatches = mergeDuplicates(initialMatchFinder.findInitialMatches(query.toSearchQuery()));
		return matchRefiner.refineMatches(initialMatches, query).getContents();
	}

	/**
	 * Consumes the given stream and merges all {@link MatchingObject}s which reference the same {@link Matchable}.
	 *
	 * @param stream the stream from which to take the objects to merge
	 * @return a list of remaining {@link MatchingObject}s
	 */
	private List<MatchingObject<?>> mergeDuplicates(Stream<? extends MatchingObject<?>> stream) {
		Map<Matchable, LinkedList<MatchingObject<?>>> matchesByMatchable = Collections.synchronizedMap(
				new LinkedHashMap<>());
		stream.forEach(
				match -> matchesByMatchable.compute(match.getObject(), (key, value) -> {
					if (value == null) value = new LinkedList<>();
					value.add(match);
					return value;
				})
		);

		List<MatchingObject<?>> mergedInitialMatches = new ArrayList<>();
		matchesByMatchable.values().forEach(list -> {
			assert !list.isEmpty();
			if (list.size() == 1) mergedInitialMatches.add(list.getFirst());
			else mergedInitialMatches.add(new Merge<>(list));
		});

		return mergedInitialMatches;
	}

}
