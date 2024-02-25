package de.medizininformatikinitiative.medgraph.matcher.resulttransformer;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions;
import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;
import de.medizininformatikinitiative.medgraph.matcher.model.Product;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.Values.parameters;

/**
 * Transformer which transforms substance matches to product matches by resolving all products which use the given
 * substance as active ingredient.
 *
 * @author Markus Budeus
 */
public class SubstanceToProductResolver implements ResultTransformer {

	private final Session session;

	public SubstanceToProductResolver(Session session) {
		this.session = session;
	}

	@Override
	public List<List<MatchingTarget>> batchTransform(List<MatchingTarget> targets, HouselistEntry entry) {
		List<Long> substanceMmiIds = new ArrayList<>(targets.size());
		for (MatchingTarget t : targets) {
			if (t.getType() == MatchingTarget.Type.SUBSTANCE)
				substanceMmiIds.add(t.getMmiId());
		}
		Map<Long, List<MatchingTarget>> remappedSubstances = queryAndParseProductsForSubstanceMmiIds(substanceMmiIds);

		List<List<MatchingTarget>> resultList = new ArrayList<>(targets.size());
		for (MatchingTarget target : targets) {
			if (target.getType() == MatchingTarget.Type.SUBSTANCE) {
				List<MatchingTarget> remaps = remappedSubstances.get(target.getMmiId());
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
	public List<MatchingTarget> transform(MatchingTarget target, HouselistEntry entry) {
		if (target.getType() == MatchingTarget.Type.PRODUCT) return List.of(target);
		return new ArrayList<>(
				queryAndParseProductsForSubstanceMmiIds(List.of(target.getMmiId())).get(target.getMmiId()));
	}

	private Map<Long, List<MatchingTarget>> queryAndParseProductsForSubstanceMmiIds(List<Long> substanceMmiIds) {
		Result result = queryProductsForSubstanceMmiIds(substanceMmiIds);
		Map<Long, List<MatchingTarget>> res = new HashMap<>();
		while (result.hasNext()) {
			Record record = result.next();
			Long id = record.get(0).asLong();
			MatchingTarget target = new Product(record.get(1).asLong(), record.get(2).asString());
			res.computeIfAbsent(id, i -> new ArrayList<>()).add(target);
		}
		return res;
	}

	private synchronized Result queryProductsForSubstanceMmiIds(List<Long> substanceMmiIds) {
		return session.run(new Query(
				"MATCH (s:" + DatabaseDefinitions.SUBSTANCE_LABEL + ")\n" +
						"WHERE s.mmiId IN $mmiIds\n" +
						"MATCH (s)<-[:" + DatabaseDefinitions.INGREDIENT_IS_SUBSTANCE_LABEL + "]-(ci:Ingredient)\n" +
						"OPTIONAL MATCH (ci)<-[:" + DatabaseDefinitions.INGREDIENT_CORRESPONDS_TO_LABEL + "]-(i:" + DatabaseDefinitions.MMI_INGREDIENT_LABEL + ")\n" +
						"WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient WHERE ingredient.isActive\n" +
						"MATCH (ingredient)<-[:" + DatabaseDefinitions.DRUG_CONTAINS_INGREDIENT_LABEL + "]-(drug:" + DatabaseDefinitions.DRUG_LABEL + ")<-[:" + DatabaseDefinitions.PRODUCT_CONTAINS_DRUG_LABEL + "]-(p:Product)\n" +
						"RETURN s.mmiId, p.mmiId, p.name",
				parameters("mmiIds", substanceMmiIds)
		));
	}

}
