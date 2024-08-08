package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.*;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * Transformer which transforms substance matches to product matches by resolving all products which use the given
 * substance as active ingredient.
 *
 * @author Markus Budeus
 */
public class SubstanceToProductResolver extends MatchTransformer<Substance, Product> {

	// TODO Ideally modify class to use the Database interface instead of Session

	private final Session session;

	public SubstanceToProductResolver(Session session) {
		this.session = session;
	}

	@Override
	public List<List<Product>> batchTransformInternal(List<? extends Substance> targets, SearchQuery query) {
		List<Long> substanceMmiIds = new ArrayList<>(targets.size());
		for (Matchable t : targets) {
			if (t instanceof Substance s)
				substanceMmiIds.add(s.getId());
		}
		Map<Long, List<Product>> remappedSubstances = queryAndParseProductsForSubstanceMmiIds(substanceMmiIds);

		List<List<Product>> resultList = new ArrayList<>(targets.size());
		for (Substance target : targets) {
			List<Product> remaps = remappedSubstances.get(target.getId());
			if (remaps == null) {
				remaps = new ArrayList<>();
			}
			resultList.add(remaps);
		}

		return resultList;
	}

	@Override
	public List<Product> transformInternal(Substance target, SearchQuery query) {
		List<Product> results = queryAndParseProductsForSubstanceMmiIds(List.of(target.getId())).get(target.getId());
		if (results == null) return Collections.emptyList();
		return results;
	}

	private Map<Long, List<Product>> queryAndParseProductsForSubstanceMmiIds(List<Long> substanceMmiIds) {
		Result result = queryProductsForSubstanceMmiIds(substanceMmiIds);
		Map<Long, List<Product>> res = new HashMap<>();
		while (result.hasNext()) {
			Record record = result.next();
			Long id = record.get(0).asLong();
			Product target = new Product(record.get(1).asLong(), record.get(2).asString());
			res.computeIfAbsent(id, i -> new ArrayList<>()).add(target);
		}
		return res;
	}

	private synchronized Result queryProductsForSubstanceMmiIds(List<Long> substanceMmiIds) {
		return session.run(new Query(
				"MATCH (s:" + SUBSTANCE_LABEL + ")\n" +
						"WHERE s.mmiId IN $mmiIds\n" +
						"MATCH (s)<-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]-(ci:Ingredient)\n" +
						"OPTIONAL MATCH (ci)<-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]-(i:" + MMI_INGREDIENT_LABEL + ")\n" +
						"WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient WHERE ingredient.isActive\n" +
						"MATCH (ingredient)<-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]-(drug:" + DRUG_LABEL + ")<-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]-(p:Product)\n" +
						"RETURN DISTINCT s.mmiId, p.mmiId, p.name",
				parameters("mmiIds", substanceMmiIds)
		));
	}

	@Override
	public String getDescription() {
		return "Resolves substances into products by searching for products which use the substance" +
				" at hand as active ingredient.";
	}
}
