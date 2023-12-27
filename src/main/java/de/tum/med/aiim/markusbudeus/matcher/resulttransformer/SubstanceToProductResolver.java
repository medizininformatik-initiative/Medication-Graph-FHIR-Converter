package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.*;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

public class SubstanceToProductResolver implements ResultTransformer {

	private final Session session;

	public SubstanceToProductResolver(Session session) {
		this.session = session;
	}

	@Override
	public List<Set<IdentifierTarget>> batchTransform(List<IdentifierTarget> targets, HouselistEntry entry) {
		List<Long> substanceMmiIds = new ArrayList<>(targets.size());
		for (IdentifierTarget t : targets) {
			if (t.type == IdentifierTarget.Type.SUBSTANCE)
				substanceMmiIds.add(t.mmiId);
		}
		Map<Long, Set<IdentifierTarget>> remappedSubstances = queryAndParseProductsForSubstanceMmiIds(substanceMmiIds);

		List<Set<IdentifierTarget>> resultList = new ArrayList<>(targets.size());
		for (IdentifierTarget target : targets) {
			if (target.type == IdentifierTarget.Type.SUBSTANCE) {
				Set<IdentifierTarget> remaps = remappedSubstances.get(target.mmiId);
				if (remaps == null) {
					remaps = new HashSet<>();
				}
				resultList.add(remaps);
			} else {
				resultList.add(Set.of(target));
			}
		}

		return resultList;
	}

	@Override
	public Set<IdentifierTarget> transform(IdentifierTarget target, HouselistEntry entry) {
		if (target.type == IdentifierTarget.Type.PRODUCT) return Set.of(target);
		return new HashSet<>(queryAndParseProductsForSubstanceMmiIds(List.of(target.mmiId)).get(target.mmiId));
	}

	private Map<Long, Set<IdentifierTarget>> queryAndParseProductsForSubstanceMmiIds(List<Long> substanceMmiIds) {
		Result result = queryProductsForSubstanceMmiIds(substanceMmiIds);
		Map<Long, Set<IdentifierTarget>> res = new HashMap<>();
		while (result.hasNext()) {
			Record record = result.next();
			Long id = record.get(0).asLong();
			IdentifierTarget target = new IdentifierTarget(record.get(1).asLong(), record.get(2).asString(),
					IdentifierTarget.Type.PRODUCT);
			target.flags.add(IdentifierTarget.Flag.RESOLVED_VIA_SUBSTANCE);
			res.computeIfAbsent(id, i -> new HashSet<>()).add(target);
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
						"MATCH (ingredient)<-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]-(drug:Drug)<-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]-(p:Product)\n" +
						"RETURN s.mmiId, p.mmiId, p.name",
				parameters("mmiIds", substanceMmiIds)
		));
	}

}
