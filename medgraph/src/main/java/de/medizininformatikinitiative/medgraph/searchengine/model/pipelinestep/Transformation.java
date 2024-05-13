package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;

import java.util.Collections;
import java.util.List;

/**
 * A transformation is a matching pipeline step which converts
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable Matchables} into different
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable Matchables}.
 * For example, substances may be converted to products via searching for producs with a given active substance.
 *
 * @author Markus Budeus
 */
public class Transformation implements MatchingPipelineStep {

	private final String name;
	private final String description;

	private final List<Matchable> result;

	public Transformation(String name, String description, List<Matchable> result) {
		this.name = name;
		this.description = description;
		this.result = Collections.unmodifiableList(result);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Returns a list of {@link Matchable}s which were generated as a result of the transformation.
	 */
	public List<Matchable> getResult() {
		return result;
	}
}
