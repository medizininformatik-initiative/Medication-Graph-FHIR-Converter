package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;

/**
 * Filter which only product matches pass.
 *
 * @author Markus Budeus
 */
public class ProductOnlyFilter implements Filter {

	public static final String NAME = "Product Only Filter";
	public static final String DESCRIPTION = "Removes all matches which are not products.";

	@Override
	public boolean passesFilter(Matchable matchable, SearchQuery query) {
		return matchable instanceof Product;
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
