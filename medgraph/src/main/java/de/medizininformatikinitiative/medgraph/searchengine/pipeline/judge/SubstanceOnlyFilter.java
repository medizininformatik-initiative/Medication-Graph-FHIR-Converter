package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;

/**
 * Filter which only product matches pass.
 *
 * @author Markus Budeus
 */
public class SubstanceOnlyFilter implements Filter {

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
