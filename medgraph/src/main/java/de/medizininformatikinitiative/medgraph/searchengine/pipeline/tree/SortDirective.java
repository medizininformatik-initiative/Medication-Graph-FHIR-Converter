package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import java.util.List;

/**
 * A sort directive specifies a technique which is used to sort entries of a {@link SubSortingTree}. This class is
 * immutable and all implementing classes should be immutable as well.
 *
 * @param <S> the type of objects in the tree to be sorted
 * @author Markus Budeus
 */
public abstract class SortDirective<S> implements SplitCause {

	private final String name;

	protected SortDirective(String name) {
		this.name = name;
	}

	abstract List<Node<S>> sortIntoLeaves(List<S> list);

	@Override
	public String getName() {
		return name;
	}
}
