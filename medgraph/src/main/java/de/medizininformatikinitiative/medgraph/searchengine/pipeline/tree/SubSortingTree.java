package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * This class represents a list of objects, which can be repeatedly sorted by different metrics. Meaning you can sort
 * the list once. All elements which are considered equal by that sorting will be affected by the next sorting step, but
 * the sort order in regard to the first sorting will remain stable.
 * <p>
 * For example, assume we have a list of the following strings: [4A, 2B, 3A, 2A, 4C, 1A] First, we assing scores by the
 * first number, getting us this list: [4A, 4C, 3A, 2B, 2A, 1A]
 * <p>
 * Next, we sort using scores of the alphabet position of the second character. However, the ordering of the first sort
 * is fixed, therefore different numbers won't swap position, leading to this result: [4C, 4A, 3A, 2B, 2A, 1A]
 * <p>
 * Due to the nature of the list, insertion of values at a later time is not easily possible.
 *
 * @author Markus Budeus
 */
public class SubSortingTree<S> implements Iterable<S> {

	/**
	 * Merges all the given trees into a new one. This creates a new tree which, at the top level, is split into the
	 * given trees. This function copies the tree structure, meaning changes to the original trees will not affect the
	 * merged tree. However, it <b>does not</b> copy the contents, meaning the merged tree references the same elements
	 * as its contents as the original tree.
	 *
	 * @param trees the trees to merge
	 */
	@SafeVarargs
	public static <S> SubSortingTree<S> merge(SubSortingTree<S>... trees) {
		List<Node<S>> newRootChildren = new ArrayList<>();
		int i = 0;
		for (SubSortingTree<S> tree : trees) {
			Node<S> otherClone = tree.root.cloneTree();
			otherClone.setSortingGroup("merge" + i);
			newRootChildren.add(otherClone);
			i++;
		}

		return new SubSortingTree<>(new InnerNode<>(newRootChildren, null, new Merge()));
	}

	private Node<S> root;

	public SubSortingTree(List<? extends S> contents) {
		root = new Leaf<>(contents, null);
	}

	private SubSortingTree(Node<S> rootNode) {
		root = rootNode;
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
	 * Searches for the keys of the given map in this tree. If found, the entry is removed from this tree and the values
	 * of that key in the map are inserted at its location instead. No transitive replacement is happening.
	 * <p>
	 * For example, if the list is ["A", "B", "C"] and you replace "A" with ["B"] and "B" with ["C"], you get ["B", "C",
	 * "C"] Similarly, replacing "A" by ["A", "A"] in that same list yiels ["A", "A", "B", "C"].
	 */
	public void batchReplace(Map<S, List<S>> replacements) {
		root.batchReplace(replacements);
	}

	/**
	 * Searches for duplicate values in this tree and removes duplicates. When duplicates are present, the first element
	 * in the list will survive and later elements are removed.
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

	@Override
	@NotNull
	public Iterator<S> iterator() {
		return getContents().iterator();
	}

	@Override
	public void forEach(Consumer<? super S> action) {
		root.forEach(action);
	}

}
