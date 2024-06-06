package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A node of the {@link SubSortingTree}.
 *
 * @param <S> the type of objects within the tree
 * @author Markus Budeus
 */
public abstract class Node<S> {

	static final String INDENT = "    ";

	public abstract String getSortingGroup();

	abstract void setSortingGroup(String sortingGroup);

	/**
	 * Searches for the keys of the given map in this node. If found, they are removed from their node and replaced by
	 * the given list of entries.
	 */
	abstract void batchReplace(Map<S, List<S>> replacements);

	abstract void clearDuplicates(HashSet<S> alreadyFoundObjects);

	public abstract List<S> getTopContents();

	public List<S> getContents() {
		List<S> result = new ArrayList<>();
		appendContents(result);
		return result;
	}

	protected abstract void appendContents(List<S> list);

	public abstract boolean isEmpty();

	/**
	 * Performs a sorting step at this node. Returns the new node to be used at this position, since a node may be split
	 * upon sorting.
	 */
	protected abstract Node<S> sort(SortDirective<S> sortDirective);

	protected void appendDescription(StringBuilder sb, int depth) {
		sb.append(INDENT.repeat(depth));

		if (getSortingGroup() != null) {
			sb.append(getSortingGroup()).append(" -> ");
		}

		String name = getName();
		if (name != null) {
			sb.append("(").append(name).append(") ");
		}

		sb.append("[");
		if (isEmpty()) {
			sb.append("]");
		} else {
			sb.append("\n");
			appendDescriptionInternal(sb, depth + 1);
			sb.append("\n");
			sb.append(INDENT.repeat(depth)).append("]");
		}

	}

	protected abstract void forEach(Consumer<? super S> action);

	protected abstract void appendDescriptionInternal(StringBuilder sb, int depth);

	protected abstract String getName();

	/**
	 * Clones this node including all children, but <b>does not</b> clone the elements managed by the tree. As a result,
	 * both this instance and the returned instance reference the same objects as elements.
	 *
	 * @return a clone of this node and all children
	 */
	protected abstract Node<S> cloneTree();
}