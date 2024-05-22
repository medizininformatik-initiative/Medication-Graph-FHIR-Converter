package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;

import java.util.*;

/**
 * This transformer transforms all {@link Product}-instances passed to it into {@link DetailedProduct}-instances.
 *
 * @author Markus Budeus
 */
public class ProductDetailsResolver extends MatchTransformer {

	private final Database database;

	public ProductDetailsResolver(Database database) {
		this.database = database;
	}

	@Override
	protected List<Matchable> transformInternal(Matchable matchable, SearchQuery query) {
		if (isApplicable(matchable)) {
			return new ArrayList<>(database.getDetailedProductInfo(Set.of(((Product) matchable).getId())));
		} else return List.of(matchable);
	}

	@Override
	protected List<List<Matchable>> batchTransformInternal(List<Matchable> matchables, SearchQuery query) {
		Set<Long> productIds = new HashSet<>();
		matchables.forEach(m -> {
			if (isApplicable(m)) productIds.add(((Product) m).getId());
		});

		Set<DetailedProduct> detailedProducts = database.getDetailedProductInfo(productIds);
		Map<Long, DetailedProduct> detailedProductsById = new HashMap<>();
		detailedProducts.forEach(d -> detailedProductsById.put(d.getId(), d));

		List<List<Matchable>> outList = new ArrayList<>();

		matchables.forEach(m -> {
			if (isApplicable(m)) {
				DetailedProduct detailedProduct = detailedProductsById.get(((Product) m).getId());
				if (detailedProduct != null) {
					outList.add(List.of(detailedProduct));
				} else {
					System.out.println("Warning: Transformation of product with id " + ((Product) m).getId() +
							" was requested, but the database provided no info on this product upon querying.");
					outList.add(List.of());
				}
			} else {
				outList.add(List.of(m));
			}
		});

		return outList;
	}

	private boolean isApplicable(Matchable matchable) {
		return matchable instanceof Product && !(matchable instanceof DetailedProduct);
	}

	@Override
	public String getDescription() {
		return "Acquires additional information about products from the database.";
	}
}
