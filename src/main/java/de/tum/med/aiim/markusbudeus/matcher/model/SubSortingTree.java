package de.tum.med.aiim.markusbudeus.matcher.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * Due to the nature of the list, insertion of values at a later time is not possible.
 */
public class SubSortingTree<S> {

	private Node<S> root;

	public SubSortingTree(List<S> contents) {
		root = new Leaf<>(contents, null);
	}

	public List<S> getContents() {
		List<S> result = new ArrayList<>();
		root.appendContents(result);
		return result;
	}

	@Override
	public String toString() {
		return getContents().toString();
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given scoring, creating a new tree layer.
	 *
	 * @param scores the scores to assign to the entries. Missing entries implicitly score zero. all entries
	 */
	public void applySortingStep(Map<S, Double> scores) {
		applySortingStep(scores, null);
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given scoring, creating a new tree layer.
	 *
	 * @param scores          the scores to assign to the entries. Missing entries implicitly score zero.
	 * @param retainThreshold the minimum score required for an entry to not be eliminated from the list or null to keep
	 *                        all entries
	 */
	public void applySortingStep(Map<S, Double> scores, Double retainThreshold) {
		applySortingStep(null, scores, retainThreshold);
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given scoring, creating a new tree layer.
	 *
	 * @param name   The name of the sorting step, only for display purposes
	 * @param scores the scores to assign to the entries. Missing entries implicitly score zero.
	 */
	public void applySortingStep(String name, Map<S, Double> scores) {
		applySortingStep(name, scores, null);
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given scoring, creating a new tree layer.
	 *
	 * @param scoringFunction a function to assign scores to the entries. Assigning null is interpreted as zero. all
	 *                        entries
	 */
	public void applySortingStep(Function<S, Double> scoringFunction) {
		applySortingStep(scoringFunction, null);
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given scoring, creating a new tree layer.
	 *
	 * @param scoringFunction a function to assign scores to the entries. Assigning null is interpreted as zero.
	 * @param retainThreshold the minimum score required for an entry to not be eliminated from the list or null to keep
	 *                        all entries
	 */
	public void applySortingStep(Function<S, Double> scoringFunction, Double retainThreshold) {
		applySortingStep(null, scoringFunction, retainThreshold);
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given scoring, creating a new tree layer.
	 *
	 * @param name            The name of the sorting step, only for display purposes
	 * @param scoringFunction a function to assign scores to the entries. Assigning null is interpreted as zero.
	 */
	public void applySortingStep(String name, Function<S, Double> scoringFunction) {
		applySortingStep(name, scoringFunction, null);
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given scoring, creating a new tree layer.
	 *
	 * @param name            The name of the sorting step, only for display purposes
	 * @param scores          the scores to assign to the entries. Missing entries implicitly score zero.
	 * @param retainThreshold the minimum score required for an entry to not be eliminated from the list or null to keep
	 *                        all entries
	 */
	public void applySortingStep(String name, Map<S, Double> scores, Double retainThreshold) {
		applySortingStep(name, scores::get, retainThreshold);
	}

	/**
	 * Applies a new sorting step. All elements in leaf nodes of the tree are sorted inside the leaf according to the
	 * given scoring, creating a new tree layer.
	 *
	 * @param name            The name of the sorting step, only for display purposes
	 * @param scoringFunction a function to assign scores to the entries. Assigning null is interpreted as zero.
	 * @param retainThreshold the minimum score required for an entry to not be eliminated from the list or null to keep
	 *                        all entries
	 */
	public void applySortingStep(String name, Function<S, Double> scoringFunction, Double retainThreshold) {
		root = root.sort(new SortDirective<>(name, scoringFunction, retainThreshold));
	}

	public String describe() {
		StringBuilder sb = new StringBuilder();
		root.appendDescription(sb, 0);
		return sb.toString();
	}

	public static class SortDirective<S> {
		final String name;
		/**
		 * A function providing the scores for each object. All objects which are in the SubSortingTree but not in this
		 * map are implicitly assigned a score of zero.
		 */
		final Function<S, Double> scoringFunction;
		/**
		 * The minimum number required for the entries to be retained. Entries with a score below this are eliminated.
		 * If this is null, no entries are eliminated.
		 */
		private final Double retainThreshold;

		public SortDirective(String name, Function<S, Double> scoringFunction, Double retainThreshold) {
			this.name = name;
			this.scoringFunction = scoringFunction;
			this.retainThreshold = retainThreshold;
		}
	}

	private static abstract class Node<S> {

		static final String INDENT = "    ";

		public abstract Double getScore();

		public abstract void appendContents(List<S> list);

		public abstract boolean isEmpty();

		/**
		 * Performs a sorting step at this node. Returns the new node to be used at this position, since a node may be
		 * split upon sorting.
		 */
		public abstract Node<S> sort(SortDirective<S> sortDirective);

		public void appendDescription(StringBuilder sb, int depth) {
			sb.append(INDENT.repeat(depth));

			if (getScore() != null) {
				sb.append(getScore()).append(" -> ");
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

		public abstract void appendDescriptionInternal(StringBuilder sb, int depth);

		protected abstract String getName();

	}

	private static class InnerNode<S> extends Node<S> {

		private final List<Node<S>> children;
		private final Double score;
		private final SortDirective<S> appliedSortDirective;

		private InnerNode(List<Node<S>> children, Double score, SortDirective<S> appliedSortDirective) {
			this.children = children;
			this.score = score;
			this.appliedSortDirective = appliedSortDirective;
		}

		@Override
		public Double getScore() {
			return score;
		}

		@Override
		public void appendContents(List<S> list) {
			children.forEach(c -> c.appendContents(list));
		}

		@Override
		public boolean isEmpty() {
			return children.isEmpty();
		}

		@Override
		public Node<S> sort(SortDirective<S> sortDirective) {
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
		public void appendDescriptionInternal(StringBuilder sb, int depth) {
			children.forEach(c -> {
				c.appendDescription(sb, depth);
				sb.append(",\n");
			});
			int l = sb.length();
			sb.delete(l - 2, l);
		}

		@Override
		protected String getName() {
			return appliedSortDirective.name;
		}

	}

	private static class Leaf<S> extends Node<S> {

		private List<S> contents;
		private Double score;

		private Leaf(List<S> contents, Double score) {
			this.contents = contents;
			this.score = score;
		}

		@Override
		public Double getScore() {
			return score;
		}

		@Override
		public void appendContents(List<S> list) {
			list.addAll(contents);
		}

		@Override
		public boolean isEmpty() {
			return contents.isEmpty();
		}

		@Override
		public Node<S> sort(SortDirective<S> sortDirective) {
			Stream<Map.Entry<Double, List<S>>> groupedResultStream =
					contents.stream()
					        .collect(Collectors.groupingBy(s -> {
						        Double score = sortDirective.scoringFunction.apply(s);
						        if (score == null) score = 0.0;
						        return score;
					        }))
					        .entrySet()
					        .stream();

			if (sortDirective.retainThreshold != null)
				groupedResultStream = groupedResultStream.filter(e -> e.getKey() >= sortDirective.retainThreshold);

			List<Map.Entry<Double, List<S>>> sortedSublists =
					groupedResultStream
							.sorted(Comparator.comparing(e -> -e.getKey()))
							.toList();

			if (sortedSublists.isEmpty()) {
				// All elements were filtered away.
				contents.clear();
				return this;
			}

			Double parentScore = this.score;
			List<Node<S>> newChildren = new ArrayList<>();

			this.score = sortedSublists.get(0).getKey();
			this.contents = sortedSublists.get(0).getValue();
			newChildren.add(this);

			for (int i = 1; i < sortedSublists.size(); i++) {
				Map.Entry<Double, List<S>> entry = sortedSublists.get(i);
				newChildren.add(new Leaf<>(entry.getValue(), entry.getKey()));
			}

			return new InnerNode<>(newChildren, parentScore, sortDirective);
		}

		@Override
		public void appendDescriptionInternal(StringBuilder sb, int depth) {

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

}
