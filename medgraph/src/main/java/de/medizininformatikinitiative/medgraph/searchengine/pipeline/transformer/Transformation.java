package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.MatchingPipelineStep;

import java.util.List;

/**
 * A transformation is a matching pipeline step which converts {@link Matchable Matchables} into different
 * {@link Matchable Matchables}. For example, substances may be converted to products via searching for producs with a
 * given active substance.
 *
 * @param <T> the type of matchable to which this transformation converted objects
 * @author Markus Budeus
 */
public record Transformation<T extends Matchable>(String name, String description,
                                                  List<T> result) implements MatchingPipelineStep {

	/**
	 * Returns a list of {@link Matchable}s which were generated as a result of the transformation.
	 */
	@Override
	public List<T> result() {
		return result;
	}
}
