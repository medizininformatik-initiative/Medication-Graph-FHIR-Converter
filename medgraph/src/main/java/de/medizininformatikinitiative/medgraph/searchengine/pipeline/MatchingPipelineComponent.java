package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

/**
 * Any component which executes matching pipeline steps.
 *
 * @author Markus Budeus
 */
public interface MatchingPipelineComponent {

	/**
	 * Returns a short name of this component.
	 */
	String getName();

	/**
	 * Returns a short description of what this component does.
	 */
	String getDescription();

}
