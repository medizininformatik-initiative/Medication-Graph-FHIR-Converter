package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

/**
 * A Judgement is a matching pipeline step which judges the object in question by some metric, usually
 * based on the corresponding search query. For example, a score may be applied to a product based on
 * how well its product name overlaps with the search query.
 *
 * @author Markus Budeus
 */
public interface Judgement extends MatchingPipelineStep {

	/**
	 * Whether the object in question has passed the judgement.
	 * Objects which do not pass a judgement are typically removed from the matching.
	 */
	boolean passed();

}
