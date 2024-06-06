package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An inner node of the {@link SubSortingTree}.
 * @param <S> the type of objects within the tree
 *
 * @author Markus Budeus
 */
class InnerNode<S> extends Node<S> {

	private final List<Node<S>> children;
	private String sortingGroup;
	/**
	 * The reason why the tree splits at this node. The usual reason is a sorting step having been applied, in which
	 * case this is a {@link SortDirective}.
	 */
	private final SplitCause splitCause;

	InnerNode(List<? extends Node<S>> children, String sortingGroup, SplitCause splitCause) {
		this.children = new ArrayList<>(children);
		this.sortingGroup = sortingGroup;
		this.splitCause = splitCause;
	}

	@Override
	public String getSortingGroup() {
		return sortingGroup;
	}

	@Override
	void setSortingGroup(String sortingGroup) {
		this.sortingGroup = sortingGroup;
	}

	@Override
	void batchReplace(Map<S, List<S>> replacements) {
		executeActionOnChildrenAndRemoveIfEmpty(c -> c.batchReplace(replacements));
	}

	@Override
	void clearDuplicates(HashSet<S> alreadyFoundObjects) {
		executeActionOnChildrenAndRemoveIfEmpty(c -> c.clearDuplicates(alreadyFoundObjects));
	}

	@Override
	public List<S> getTopContents() {
		if (isEmpty()) return new ArrayList<>();
		return children.getFirst().getTopContents();
	}

	@Override
	protected void appendContents(List<S> list) {
		children.forEach(c -> c.appendContents(list));
	}

	@Override
	public boolean isEmpty() {
		return children.isEmpty();
	}

	@Override
	protected Node<S> sort(SortDirective<S> sortDirective) {
		int l = children.size();
		for (int i = l - 1; i >= 0; i--) {
			Node<S> newChild = children.get(i).sort(sortDirective);
			if (newChild.isEmpty()) {
				children.remove(i);
			} else {
				children.set(i, newChild);
			}
		}
		return this;
	}

	@Override
	protected void forEach(Consumer<? super S> action) {
		children.forEach(c -> c.forEach(action));
	}

	private void executeActionOnChildrenAndRemoveIfEmpty(Consumer<Node<S>> action) {
		for (int i = 0; i < children.size();) {
			Node<S> child = children.get(i);
			action.accept(child);
			if (child.isEmpty()) {
				children.remove(i);
			} else {
				i++;
			}
		}
	}

	@Override
	protected void appendDescriptionInternal(StringBuilder sb, int depth) {
		children.forEach(c -> {
			c.appendDescription(sb, depth);
			sb.append(",\n");
		});
		int l = sb.length();
		sb.delete(l - 2, l);
	}

	@Override
	protected String getName() {
		return splitCause.getName();
	}

	@Override
	protected Node<S> cloneTree() {
		List<Node<S>> newChildren = new ArrayList<>();
		children.forEach(child -> newChildren.add(child.cloneTree()));
		return new InnerNode<>(newChildren, sortingGroup, splitCause);
	}

}