package de.medizininformatikinitiative.medgraph.matcher;


import de.medizininformatikinitiative.medgraph.matcher.model.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.math.BigDecimal;
import java.util.*;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * This class transforms {@link ProductWithPzn}-instances into {@link FinalMatchingTarget}-instances.
 *
 * @author Markus Budeus
 */
public class FinalResultTransformer {

	private static final Comparator<Drug> drugComparator;

	static {
		Comparator<Drug> comparator = Comparator.comparing(d -> -d.activeIngredients.size());
		drugComparator = comparator.thenComparing(drug -> drug.doseForm != null ? drug.doseForm : "");
	}

	private final Session session;

	public FinalResultTransformer(Session session) {
		this.session = session;
	}

	public ResultSet<FinalMatchingTarget> transform(ResultSet<ProductWithPzn> resultSet) {
		ArrayList<ProductWithPzn> allResults = new ArrayList<>();
		if (resultSet.primaryResult != null) {
			allResults.add(resultSet.primaryResult);
		}
		allResults.addAll(resultSet.secondaryResults);
		allResults.addAll(resultSet.tertiaryResults);

		if (allResults.isEmpty()) {
			return new ResultSet<>(null, List.of(), List.of());
		}
		List<FinalMatchingTarget> resultList = transform(allResults);
		Map<Long, FinalMatchingTarget> targetsByMmiId = new HashMap<>();
		resultList.forEach(t -> targetsByMmiId.put(t.getMmiId(), t));

		FinalMatchingTarget bestResult = null;
		if (resultSet.primaryResult != null) {
			bestResult = targetsByMmiId.get(resultSet.primaryResult.getMmiId());
		}
		List<FinalMatchingTarget> goodResults = resultSet.secondaryResults.stream()
		                                                                  .map(p -> targetsByMmiId.get(p.getMmiId()))
		                                                                  .toList();
		List<FinalMatchingTarget> otherResults = resultSet.tertiaryResults.stream()
		                                                                  .map(p -> targetsByMmiId.get(p.getMmiId()))
		                                                                  .toList();
		return new ResultSet<>(bestResult, goodResults, otherResults);
	}

	public List<FinalMatchingTarget> transform(List<ProductWithPzn> list) {
		if (list.isEmpty()) return Collections.emptyList();
		Result result = session.run(new Query(
				"MATCH (p:" + PRODUCT_LABEL + ") " +
						"WHERE p.mmiId IN $productIds " +
						"OPTIONAL MATCH (p)-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ") " +
						"OPTIONAL MATCH (d)-[:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(df:" + DOSE_FORM_LABEL + ") " +
						"OPTIONAL MATCH (df)-[:" + DOSE_FORM_IS_EDQM + "]->(ef:" + EDQM_LABEL + ") " +
						"OPTIONAL MATCH (d)-[:" + DRUG_HAS_UNIT_LABEL + "]->(du:" + UNIT_LABEL + ") " +
						"OPTIONAL MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:" + MMI_INGREDIENT_LABEL + " {isActive: true})-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(is:" + SUBSTANCE_LABEL + ") " +
						"OPTIONAL MATCH (i)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(ci:" + INGREDIENT_LABEL + ")-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(cs:" + SUBSTANCE_LABEL + ") " +
						"OPTIONAL MATCH (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(iu:" + UNIT_LABEL + ") " +
						"OPTIONAL MATCH (ci)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(cu:" + UNIT_LABEL + ") " +
						"WITH p, d, df, du, ef, collect(CASE WHEN NOT i IS NULL THEN {" +
						"iName:is.name,iMassFrom:i.massFrom,iMassTo:i.massTo,iUnit:iu.print," +
						"cName:cs.name,cMassFrom:ci.massFrom,cMassTo:ci.massTo,cUnit:cu.print} ELSE NULL END) AS ingredients " +
						"RETURN p.mmiId AS productId, collect({doseForm:df.mmiName, edqm:ef.name, amount:d.amount, unit:du.print, ingredients:ingredients}) AS drugs",
				parameters("productIds", list.stream().map(ProductWithPzn::getMmiId).toList())
		));

		Map<Long, List<Drug>> resultMap = new HashMap<>();
		while (result.hasNext()) {
			Record record = result.next();
			resultMap.put(record.get(0).asLong(), parse(record));
		}

		if (resultMap.size() != list.size()) {
			throw new IllegalStateException(
					"Failed to resolve all products! (Got " + list.size() + ", but resolved " + resultMap.size() + ")");
		}

		List<FinalMatchingTarget> resultList = new ArrayList<>(list.size());

		for (ProductWithPzn product : list) {
			resultList.add(new FinalMatchingTarget(product.getMmiId(), product.getName(), product.getPzn(),
					resultMap.get(product.getMmiId())));
		}
		return resultList;
	}

	private List<Drug> parse(Record record) {
		List<Drug> drugList = new ArrayList<>(record.get("drugs").asList(this::parseToDrug));
		drugList.sort(drugComparator);
		return drugList;
	}

	private Drug parseToDrug(Value value) {
		String doseForm = value.get("doseForm").asString(null);
		String edqm = value.get("edqm").asString(null);
		BigDecimal amount = toBigDecimal(value.get("amount").asString(null));
		String unit = value.get("unit").asString(null);
		List<ActiveIngredient> ingredients = value.get("ingredients").asList(this::parseToActiveIngredient);
		return new Drug(doseForm, edqm, new Amount(amount, unit), ingredients);
	}

	private ActiveIngredient parseToActiveIngredient(Value value) {
		String name = value.get("iName").asString(null);
		BigDecimal massFrom = toBigDecimal(value.get("iMassFrom").asString(null));
		BigDecimal massTo = toBigDecimal(value.get("iMassTo").asString(null));
		String unit = value.get("iUnit").asString(null);
		AmountRange amount = new AmountRange(massFrom, massTo, unit);

		String correspondingName = value.get("cName").asString(null);
		if (correspondingName != null) {
			BigDecimal correspondingMassFrom = toBigDecimal(value.get("cMassFrom").asString(null));
			BigDecimal correspondingMassTo = toBigDecimal(value.get("cMassTo").asString(null));
			String correspondingUnit = value.get("cUnit").asString(null);
			AmountRange correspondingAmount = new AmountRange(correspondingMassFrom, correspondingMassTo,
					correspondingUnit);

			return new CorrespondingActiveIngredient(name, amount, correspondingName, correspondingAmount);
		}

		return new ActiveIngredient(name, amount);
	}

	private static BigDecimal toBigDecimal(String germanValue) {
		if (germanValue == null) return null;
		return new BigDecimal(germanValue.replace(',', '.'));
	}

}
