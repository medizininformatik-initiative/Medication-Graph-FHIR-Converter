package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

import java.util.Collections;
import java.util.List;

/**
 * A transformation is a matching pipeline step which converts {@link Matchable Matchables} into different
 * {@link Matchable Matchables}. For example, substances may be converted to products via searching for producs with a
 * given active substance.
 *
 * @author Markus Budeus
 */
public record Transformation(String name, String description, List<Matchable> result) implements MatchingPipelineStep {

	public Transformation(String name, String description, List<Matchable> result) {
		this.name = name;
		this.description = description;
		this.result = Collections.unmodifiableList(result);
	}

	/**
	 * Returns a list of {@link Matchable}s which were generated as a result of the transformation.
	 */
	@Override
	public List<Matchable> result() {
		return result;
	}
}
