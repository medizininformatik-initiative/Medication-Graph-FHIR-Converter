package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import java.util.Collection;
import java.util.stream.DoubleStream;

/**
 * Represents a strategy on how the scores of individual objects are merged into a score of a {@link Merge} object.
 *
 * @author Markus Budeus
 */
@FunctionalInterface
public interface ScoreMergingStrategy {

	/**
	 * Merges scores by calculating the sum.
	 */
	ScoreMergingStrategy SUM = DoubleStream::sum;
	/**
	 * Merges scores by calculating the maximum value.
	 */
	ScoreMergingStrategy MAX = scores -> scores.max().orElse(0);

	/**
	 * Calculates the score of a {@link Merge} object based on the scores of its source objects, which are passed to
	 * this function.
	 *
	 * @param scores the scores of the source objects
	 * @return the score of the merge of those objects
	 */
	double mergeScores(DoubleStream scores);


}
