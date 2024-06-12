package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

/**
 * The result of a filtering step which filters out some {@link Matchable}s.
 *
 * @author Markus Budeus
 */
public record Filtering(String name, String description, boolean passed) implements Judgement {

}
