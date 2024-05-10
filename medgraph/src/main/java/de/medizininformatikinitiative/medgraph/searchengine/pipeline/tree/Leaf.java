package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import java.util.*;
import java.util.function.Consumer;

/**
 * A leaf node of the {@link SubSortingTree}.
 *
 * @param <S> the type of objects within the tree
 * @author Markus Budeus
 */
class Leaf<S> extends Node<S> {

	private List<S> contents;
	private final String sortingGroup;

	Leaf(List<? extends S> contents, String sortingGroup) {
		this.contents = new ArrayList<>(contents);
		this.sortingGroup = sortingGroup;
	}

	@Override
	public String getSortingGroup() {
		return sortingGroup;
	}

	@Override
	void batchReplace(Map<S, List<S>> replacements) {
		List<S> result = new ArrayList<>();
		for (S entry : contents) {
			List<S> replacement = replacements.get(entry);
			if (replacement != null) {
				result.addAll(replacement);
			} else {
				result.add(entry);
			}
		}
		this.contents = result;
	}

	@Override
	void clearDuplicates(HashSet<S> alreadyFoundObjects) {
		// Remove objects which already exist in other nodes
		contents.removeAll(alreadyFoundObjects);

		// Remove duplicates within this node by copying to a set which disallows duplicates,
		// then copying back.
		Set<S> contentSet = new LinkedHashSet<>(contents);
		contents.clear();
		contents.addAll(contentSet);

		// Add remaining objects to list of already known objects
		alreadyFoundObjects.addAll(contents);
	}

	@Override
	public List<S> getTopContents() {
		return contents;
	}

	@Override
	protected void appendContents(List<S> list) {
		list.addAll(contents);
	}

	@Override
	public boolean isEmpty() {
		return contents.isEmpty();
	}

	@Override
	protected Node<S> sort(SortDirective<S> sortDirective) {
		List<Node<S>> newChildren = sortDirective.sortIntoLeaves(contents);

		if (newChildren.isEmpty()) {
			// All elements were filtered away.
			contents.clear();
			return this;
		}

		return new InnerNode<>(newChildren, this.sortingGroup, sortDirective);
	}

	@Override
	protected void forEach(Consumer<? super S> action) {
		this.contents.forEach(action);
	}

	@Override
	protected void appendDescriptionInternal(StringBuilder sb, int depth) {
		contents.forEach(c -> {
			sb.append(INDENT.repeat(depth));
			sb.append(c);
			sb.append("\n");
		});
		int l = sb.length();
		sb.delete(l - 1, l);
	}

	@Override
	protected String getName() {
		return null;
	}

}