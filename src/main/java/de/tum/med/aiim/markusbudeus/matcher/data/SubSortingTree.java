package de.tum.med.aiim.markusbudeus.matcher.data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class represents a list of objects, which can be repeatedly sorted by different metrics. Meaning you can sort
 * the list once. All elements which are considered equal by that sorting will be affected by the next sorting step, but
 * the sort order in regard to the first sorting will remain stable.
 * <p>
 * For example, assume we have a list of the following strings: [4A, 2B, 3A, 2A, 4C, 1A] First, we assing
 * scores by the first number, getting us this list: [4A, 4C, 3A, 2B, 2A, 1A]
 * <p>
 * Next, we sort using scores of the alphabet position of the second character. However, the ordering of the first
 * sort is fixed, therefore different numbers won't swap position, leading to this result:
 * [4C, 4A, 3A, 2B, 2A, 1A]
 * <p>
 * Due to the nature of the list, insertion of values at a later time is not easily possible.
 */
public class SubSortingTree<S> {

	private Node<S> root;

	public SubSortingTree(List<S> contents) {
		root = new Leaf<>(contents, null);
	}

	/**
	 * Returns the contents in the first group according to previously applied sortings.
	 */
	public List<S> getTopContents() {
		return root.getTopContents();
	}

	public List<S> getContents() {
		return root.getContents();
	}

	/**
	 * Searches for the keys of the given map in this node. If found, the entry is removed from this tree
	 * and the values of that key in the map are inserted at its location instead.
	 */
	public void batchReplace(Map<S, List<S>> replacements) {
		root.batchReplace(replacements);
	}

	/**
	 * Searches for duplicate values in this tree and removes duplicates. When duplicates are present, the first
	 * element in the list will survive and later elements are removed.
	 */
	public void clearDuplicates() {
		root.clearDuplicates(new HashSet<>());
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given sort directive, creating a new tree layer.
	 *
	 * @param sortDirective the sort directive to apply
	 */
	public void applySortingStep(SortDirective<S> sortDirective) {
		root = root.sort(sortDirective);
	}

	/**
	 * Returns a human-readable textual representation of the current tree.
	 */
	public String describe() {
		StringBuilder sb = new StringBuilder();
		root.appendDescription(sb, 0);
		return sb.toString();
	}

	@Override
	public String toString() {
		return getContents().toString();
	}

}
