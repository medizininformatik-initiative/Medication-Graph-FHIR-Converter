package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation;

import java.util.List;

/**
 * Class which transforms {@link Matchable Matchables} into different {@link Matchable Matchables}. Generates a
 * {@link Transformation} as a result.
 *
 * @author Markus Budeus
 * @see Transformation
 */
public interface IResultTransformer {

	/**
	 * Creates a {@link Transformation} for a single {@link Matchable}. When transforming multiple {@link Matchable}s,
	 * consider using {@link #batchTransform(List, SearchQuery)}.
	 *
	 * @param matchable the {@link Matchable} to transform
	 * @param query     the query from which to use information which may affect the way the transformation is
	 *                  performed
	 * @return a {@link Transformation} with the transformation results
	 */
	Transformation transform(Matchable matchable, SearchQuery query);

	/**
	 * Like {@link #transform(Matchable, SearchQuery)}, but transforms multiple matchables at the same time. This allows
	 * implementations to optimize the transformations, e.g. by running a single batch database access instead of
	 * querying the database once for every object.
	 *
	 * @param matchables the {@link Matchable}s to transform
	 * @param query      the query from which to use information which may affect the way the transformation is
	 *                   performed
	 * @return a list of {@link Transformation}s with the transformation results, each entry corresponds to the
	 * {@link Matchable} in the input list at the same position
	 */
	default List<Transformation> batchTransform(List<Matchable> matchables, SearchQuery query) {
		return matchables.stream().map(m -> transform(m, query)).toList();
	}

}
