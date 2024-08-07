package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

/**
 * The result of a filtering step which filters out some {@link Matchable}s.
 *
 * @author Markus Budeus
 */
public record FilteringInfo(boolean passed) implements JudgementInfo {

}
