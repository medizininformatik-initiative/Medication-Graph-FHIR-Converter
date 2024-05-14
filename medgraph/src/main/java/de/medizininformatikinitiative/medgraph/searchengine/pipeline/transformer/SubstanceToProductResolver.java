package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * Transformer which transforms substance matches to product matches by resolving all products which use the given
 * substance as active ingredient.
 *
 * @author Markus Budeus
 */
public class SubstanceToProductResolver extends MatchTransformer {

	private final Session session;

	public SubstanceToProductResolver(Session session) {
		this.session = session;
	}

	@Override
	public List<List<Matchable>> batchTransformInternal(List<Matchable> targets, SearchQuery query) {
		List<Long> substanceMmiIds = new ArrayList<>(targets.size());
		for (Matchable t : targets) {
			if (t instanceof Substance s)
				substanceMmiIds.add(s.getId());
		}
		Map<Long, List<Matchable>> remappedSubstances = queryAndParseProductsForSubstanceMmiIds(substanceMmiIds);

		List<List<Matchable>> resultList = new ArrayList<>(targets.size());
		for (Matchable target : targets) {
			if (target instanceof Substance s) {
				List<Matchable> remaps = remappedSubstances.get(s.getId());
				if (remaps == null) {
					remaps = new ArrayList<>();
				}
				resultList.add(remaps);
			} else {
				resultList.add(List.of(target));
			}
		}

		return resultList;
	}

	@Override
	public List<Matchable> transformInternal(Matchable target, SearchQuery query) {
		if (target instanceof Substance s) {
			return new ArrayList<>(queryAndParseProductsForSubstanceMmiIds(List.of(s.getId())).get(s.getId()));
		}
		return List.of(target);
	}

	private Map<Long, List<Matchable>> queryAndParseProductsForSubstanceMmiIds(List<Long> substanceMmiIds) {
		Result result = queryProductsForSubstanceMmiIds(substanceMmiIds);
		Map<Long, List<Matchable>> res = new HashMap<>();
		while (result.hasNext()) {
			Record record = result.next();
			Long id = record.get(0).asLong();
			Matchable target = new Product(record.get(1).asLong(), record.get(2).asString());
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
						"RETURN s.mmiId, p.mmiId, p.name",
				parameters("mmiIds", substanceMmiIds)
		));
	}

	@Override
	public String getDescription() {
		return "Resolves substances into products by searching for products which use the substance" +
				" at hand as active ingredient.";
	}
}
