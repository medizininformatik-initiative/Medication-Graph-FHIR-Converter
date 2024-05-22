package de.medizininformatikinitiative.medgraph.searchengine.db;

import de.medizininformatikinitiative.medgraph.searchengine.model.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * Implementation of {@link Database} primarily relying on Cypher queries.
 *
 * @author Markus Budeus
 */
public class Neo4jCypherDatabase implements Database {

	private static final Comparator<Drug> drugComparator;

	static {
		Comparator<Drug> comparator = Comparator.comparing(d -> -d.getActiveIngredients().size());
		drugComparator = comparator.thenComparing(drug -> drug.getDoseForm() != null ? drug.getDoseForm() : "");
	}

	private final Session session;

	/**
	 * Creates a new instance.
	 *
	 * @param session the session to use for accessing the database
	 */
	public Neo4jCypherDatabase(Session session) {
		this.session = session;
	}

	@Override
	public Set<DbDosagesByProduct> getDrugDosagesByProduct(Collection<Long> productIds) {
		Result result = session.run(new Query(
				"MATCH (p:" + PRODUCT_LABEL + ")\n" +
						"WHERE p.mmiId IN $mmiIds\n" +
						"MATCH (p)--(d:" + DRUG_LABEL + ")--(i:" + INGREDIENT_LABEL + " {isActive: true})\n" +
						"OPTIONAL MATCH (i)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]-(ic:" + INGREDIENT_LABEL + ")\n" +
						"OPTIONAL MATCH (d)--(du:" + UNIT_LABEL + ")\n" +
						"WITH p, d, du, [i,ic] AS ingredients\n" +
						"UNWIND ingredients as i\n" +
						"WITH p, d, du, i WHERE NOT i IS NULL\n" +
						"MATCH (i)--(u:Unit)\n" +
						"WITH p.mmiId AS productId, d.mmiId AS drugId,\n" +
						"{amount:d.amount, unit:du.print} AS drugAmount, " +
						"collect({amountFrom:i.massFrom,amountTo:i.massTo," +
						"unit:(CASE WHEN u.ucumCs IS NULL THEN u.mmiName ELSE u.ucumCs END)" +
						"}) AS dosage\n" +
						"WITH productId, collect({drugId:drugId, amount:drugAmount, dosage:dosage}) AS drugDosages\n" +
						"RETURN productId, drugDosages",
				parameters("mmiIds", productIds)
		));

		Set<DbDosagesByProduct> resultSet = new HashSet<>();
		result.forEachRemaining(record -> {
			resultSet.add(new DbDosagesByProduct(record));
		});
		return resultSet;
	}

	@Override
	public Set<DetailedProduct> getDetailedProductInfo(Collection<Long> productIds) {
		if (productIds.isEmpty()) return Collections.emptySet();

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
						"WITH p, " +
						"collect(CASE WHEN NOT d IS NULL THEN {doseForm:df.mmiName, edqm:ef.name, amount:d.amount, unit:du.print, ingredients:ingredients} ELSE NULL END) AS drugs " +
						"OPTIONAL MATCH (p)<-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]-(pk:" + PACKAGE_LABEL + ")<-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]-(pzn:" + PZN_LABEL + ") " +
						"RETURN p.mmiId AS productId, p.name as productName, " +
						"drugs, " +
						"collect(pzn.code) AS pzns",
				parameters("productIds", productIds)
		));

		return result.stream().map(this::parseToDetailedProduct).collect(Collectors.toSet());
	}

	private DetailedProduct parseToDetailedProduct(Record record) {
		long productId = record.get("productId").asLong();
		String productName = record.get("productName").asString();
		List<String> pzns = record.get("pzns").asList(Value::asString);
		List<Drug> drugList = new ArrayList<>(record.get("drugs").asList(this::parseToDrug, null));
		drugList.sort(drugComparator);
		return new DetailedProduct(productId, productName, pzns, drugList);
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
