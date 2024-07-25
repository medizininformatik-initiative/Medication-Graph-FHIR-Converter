package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;

import java.util.*;

/**
 * This transformer transforms all {@link Product}-instances passed to it into {@link DetailedProduct}-instances.
 *
 * @author Markus Budeus
 */
public class ProductDetailsResolver extends MatchTransformer<Product, DetailedProduct> {

	private final Database database;

	public ProductDetailsResolver(Database database) {
		this.database = database;
	}

	@Override
	protected List<DetailedProduct> transformInternal(Product matchable, SearchQuery query) {
		if (matchable instanceof DetailedProduct p) return List.of(p);
		return new ArrayList<>(database.getDetailedProductInfo(Set.of(matchable.getId())));
	}

	@Override
	protected List<List<DetailedProduct>> batchTransformInternal(List<? extends Product> matchables,
	                                                             SearchQuery query) {
		Set<Long> productIds = new HashSet<>();
		matchables.forEach(m -> {
			if (!(m instanceof DetailedProduct)) productIds.add(m.getId());
		});

		Set<DetailedProduct> detailedProducts = database.getDetailedProductInfo(productIds);
		Map<Long, DetailedProduct> detailedProductsById = new HashMap<>();
		detailedProducts.forEach(d -> detailedProductsById.put(d.getId(), d));

		List<List<DetailedProduct>> outList = new ArrayList<>();

		matchables.forEach(m -> {
			if (m instanceof DetailedProduct d) {
				outList.add(List.of(d));
			} else {
				DetailedProduct detailedProduct = detailedProductsById.get(((Product) m).getId());
				if (detailedProduct != null) {
					outList.add(List.of(detailedProduct));
				} else {
					System.out.println("Warning: Transformation of product with id " + m.getId() +
							" was requested, but the database provided no info on this product upon querying.");
					outList.add(List.of());
				}
			}
		});

		return outList;
	}

	@Override
	public String getDescription() {
		return "Acquires additional information about products from the database.";
	}
}
