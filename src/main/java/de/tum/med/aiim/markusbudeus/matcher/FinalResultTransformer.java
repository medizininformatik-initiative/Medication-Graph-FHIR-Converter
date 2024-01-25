package de.tum.med.aiim.markusbudeus.matcher;


import de.tum.med.aiim.markusbudeus.matcher.model.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

public class FinalResultTransformer {

	private final Session session;

	public FinalResultTransformer(Session session) {
		this.session = session;
	}

	public List<FinalMatchingTarget> transform(List<ProductWithPzn> list) {
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
						"WITH p, d, df, du, ef, collect({" +
						"iName:is.name,iMassFrom:i.massFrom,iMassTo:i.massTo,iUnit:iu.print," +
						"cName:cs.name,cMassFrom:ci.massFrom,cMassTo:ci.massTo,cUnit:cu.print}) AS ingredients " +
						"RETURN p.mmiId AS productId, collect({doseForm:df.mmiName, edqm:ef.name, amount:d.amount, unit:du.print, ingredients:ingredients}) AS drugs",
				parameters("productIds", list.stream().map(ProductWithPzn::getMmiId).toList())
		));

		Map<Long, List<Drug>> resultMap = new HashMap<>();
		while (result.hasNext()) {
			Record record = result.next();
			resultMap.put(record.get(0).asLong(), parse(record));
		}

		if (resultMap.size() != list.size()) {
			throw new IllegalStateException("Failed to resolve all products! (Got "+list.size()+", but resolved "+resultMap.size()+")");
		}

		List<FinalMatchingTarget> resultList = new ArrayList<>(list.size());

		for (ProductWithPzn product : list) {
			resultList.add(new FinalMatchingTarget(product.getMmiId(), product.getName(), product.getPzn(),
					resultMap.get(product.getMmiId())));
		}
		return resultList;
	}

	private List<Drug> parse(Record record) {
		return record.get("drugs").asList(this::parseToDrug);
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
