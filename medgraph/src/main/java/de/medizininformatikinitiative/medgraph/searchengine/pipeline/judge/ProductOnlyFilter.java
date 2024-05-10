package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;

import java.util.List;

/**
 * Filter which only product matches pass.
 *
 * @author Markus Budeus
 */
public class ProductOnlyFilter implements Filter {

	@Override
	public boolean passesFilter(Matchable matchable, SearchQuery query) {
		return matchable instanceof Product;
	}

	@Override
	public String getName() {
		return "Product Only Filter";
	}

	@Override
	public String getDescription() {
		return "Removes all matches which are not products.";
	}
}
