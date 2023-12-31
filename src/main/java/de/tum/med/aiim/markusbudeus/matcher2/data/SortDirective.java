package de.tum.med.aiim.markusbudeus.matcher2.data;

import java.util.List;

public abstract class SortDirective<S> {

	final String name;

	protected SortDirective(String name) {
		this.name = name;
	}

	abstract List<Node<S>> sortIntoLeaves(List<S> list);

}
