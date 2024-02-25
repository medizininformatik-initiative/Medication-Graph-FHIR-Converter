package de.medizininformatikinitiative.medgraph.matcher.data;

import java.util.List;

/**
 * A sort directive specifies a technique which is used to sort entries of a {@link SubSortingTree}.
 * @param <S> the type of objects in the tree to be sorted
 * @author Markus Budeus
 */
public abstract class SortDirective<S> {

	final String name;

	protected SortDirective(String name) {
		this.name = name;
	}

	abstract List<Node<S>> sortIntoLeaves(List<S> list);

}
