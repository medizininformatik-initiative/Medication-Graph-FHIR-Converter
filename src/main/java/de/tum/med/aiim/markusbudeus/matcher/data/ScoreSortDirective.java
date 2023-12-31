package de.tum.med.aiim.markusbudeus.matcher.data;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A sort directive which works by applying a floating-point score to every entry.
 */
public class ScoreSortDirective<S> extends SortDirective<S> {
	/**
	 * A function providing the scores for each object. All objects which are in the SubSortingTree but not in this map
	 * are implicitly assigned a score of zero.
	 */
	final Function<S, Double> scoringFunction;
	/**
	 * The minimum number required for the entries to be retained. Entries with a score below this are eliminated. If
	 * this is null, no entries are eliminated.
	 */
	private final Double retainThreshold;

	public ScoreSortDirective(String name, Function<S, Double> scoringFunction, Double retainThreshold) {
		super(name);
		this.scoringFunction = scoringFunction;
		this.retainThreshold = retainThreshold;
	}

	@Override
	List<Node<S>> sortIntoLeaves(List<S> list) {
		Stream<Map.Entry<Double, List<S>>> groupedResultStream =
				list.stream()
				        .collect(Collectors.groupingBy(s -> {
					        Double score = scoringFunction.apply(s);
					        if (score == null) score = 0.0;
					        return score;
				        }))
				        .entrySet()
				        .stream();

		if (retainThreshold != null)
			groupedResultStream = groupedResultStream.filter(e -> e.getKey() >= retainThreshold);

		return groupedResultStream
						.sorted(Comparator.comparing(e -> -e.getKey()))
						.map(entry -> (Node<S>) new Leaf<>(entry.getValue(), String.valueOf(entry.getKey())))
						.toList();
	}

}
