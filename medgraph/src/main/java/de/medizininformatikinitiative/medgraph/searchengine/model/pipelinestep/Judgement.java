package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.JudgementInfo;

/**
 * A Judgement is a matching pipeline step which assignes a score to the object in question via some metric, usually
 * based on the corresponding search query. For example, a score may be applied to a product based on how well its
 * product name overlaps with the search query.
 *
 * @author Markus Budeus
 */
public interface Judgement extends MatchingPipelineStep {

	/**
	 * Returns whether this judgement was passed.
	 */
	boolean passed();

	/**
	 * Returns additional information provided by the
	 * {@link de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.Judge Judge}.
	 */
	JudgementInfo getJudgementInfo();

}
