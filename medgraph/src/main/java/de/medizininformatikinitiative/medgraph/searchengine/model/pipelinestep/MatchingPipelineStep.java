package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

/**
 * Represents information about a matching pipeline step (e.g., a filtering step) applied to a
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject} during the
 * matching.
 *
 * @author Markus Budeus
 */
public interface MatchingPipelineStep {

	/**
	 * Returns a human-readable name of this matching step.
	 */
	String name();

	/**
	 * Returns a human-readable short description of this step.
	 */
	String description();

}
