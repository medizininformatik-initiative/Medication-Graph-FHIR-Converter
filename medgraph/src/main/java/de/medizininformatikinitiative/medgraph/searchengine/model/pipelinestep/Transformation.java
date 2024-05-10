package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

/**
 * A transformation is a matching pipeline step which converts
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable Matchables} into different
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable Matchables}
 *
 * @author Markus Budeus
 */
public class Transformation implements MatchingPipelineStep {

	private final String name;
	private final String description;

	public Transformation(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
