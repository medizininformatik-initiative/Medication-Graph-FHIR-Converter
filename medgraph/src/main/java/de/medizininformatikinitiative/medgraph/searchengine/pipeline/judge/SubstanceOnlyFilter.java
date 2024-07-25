package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;

/**
 * Filter which only substance matches pass.
 *
 * @author Markus Budeus
 */
public class SubstanceOnlyFilter implements Filter<Matchable> {

	public static final String NAME = "Substance Only Filter";
	public static final String DESCRIPTION = "Removes all matches which are not substances.";

	@Override
	public boolean passesFilter(Matchable matchable, SearchQuery query) {
		return matchable instanceof Substance;
	}

	@Override
	public String toString() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}
}
