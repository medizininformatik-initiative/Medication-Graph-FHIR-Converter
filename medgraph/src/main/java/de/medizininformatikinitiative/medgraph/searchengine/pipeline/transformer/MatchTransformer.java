package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation;

import java.util.List;

/**
 * Extension of {@link IMatchTransformer} which simplifies implementation by taking care of creating the
 * {@link Transformation} objects.
 *
 * @param <S> the type of source objects this transformer suports
 * @param <T> the type of target objects this transformer converts to
 * @author Markus Budeus
 */
public abstract class MatchTransformer<S extends Matchable, T extends  Matchable> implements IMatchTransformer<S, T> {
	@Override
	public Transformation<T> transform(S matchable, SearchQuery query) {
		return new Transformation<>(toString(), getDescription(), transformInternal(matchable, query));
	}

	@Override
	public List<Transformation<T>> batchTransform(List<? extends S> matchables, SearchQuery query) {
		String name = toString();
		String desc = getDescription();
		return batchTransformInternal(matchables, query).stream()
		                                                .map(result -> new Transformation<>(name, desc, result))
		                                                .toList();
	}

	/**
	 * Transforms the given {@link Matchable} into new {@link Matchable}s.
	 *
	 * @param matchable the {@link Matchable} to transform
	 * @param query     the search query to consider for the transformation
	 * @return a list of {@link Matchable}s into which the input has been transformed
	 */
	protected abstract List<T> transformInternal(S matchable, SearchQuery query);

	/**
	 * Transforms each of the given {@link Matchable} into new {@link Matchable}s. Must provide the same results as if
	 * {@link #transformInternal(Matchable, SearchQuery)} were called for each {@link Matchable}.
	 *
	 * @param matchables the list of {@link Matchable}s to transform
	 * @param query      the search query to consider for the transformation
	 * @return a list of {@link Matchable}s into which the input has been transformed
	 */
	protected List<List<T>> batchTransformInternal(List<? extends S> matchables, SearchQuery query) {
		return matchables.stream().map(m -> transformInternal(m, query)).toList();
	}

	/**
	 * Retunrs a human-readable description of what this transformer does.
	 */
	public abstract String getDescription();

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
