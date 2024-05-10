package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A sort directive which works as a binary filter.
 *
 * @author Markus Budeus
 */
public class BinarySortDirective<S> extends SortDirective<S> {

	private final Predicate<S> filterPredicate;
	private final boolean eliminateNegatives;

	public BinarySortDirective(String name, Predicate<S> filterPredicate, boolean eliminateNegatives) {
		super(name);
		this.filterPredicate = filterPredicate;
		this.eliminateNegatives = eliminateNegatives;
	}

	@Override
	List<Node<S>> sortIntoLeaves(List<S> list) {
		List<S> positives = new ArrayList<>();
		List<S> negatives = new ArrayList<>();

		list.forEach(e -> {
			if (filterPredicate.test(e)) positives.add(e);
			else negatives.add(e);
		});

		List<Node<S>> resultList = new ArrayList<>();
		resultList.add(new Leaf<>(positives, "true"));

		if (eliminateNegatives) {
			resultList.add(new Leaf<>(negatives, "false"));
		}
		return resultList;
	}

}
