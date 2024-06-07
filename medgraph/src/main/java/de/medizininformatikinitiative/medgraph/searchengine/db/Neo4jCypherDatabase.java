package de.medizininformatikinitiative.medgraph.searchengine.db;

import de.medizininformatikinitiative.medgraph.common.EDQM;
import de.medizininformatikinitiative.medgraph.searchengine.model.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmPharmaceuticalDoseForm;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

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
	public Set<DetailedProduct> getDetailedProductInfo(Collection<Long> productIds) {
		if (productIds.isEmpty()) return Collections.emptySet();

		Result result = session.run(new Query(
				// Don't ask questions. Yes this query is complicated, but doing all of this via a single query
				// allows the database engine to make lotsa nice query performance enhancements.
				"MATCH (p:"+PRODUCT_LABEL+") WHERE p.mmiId IN $productIds\n" +
						"OPTIONAL MATCH (p)-[:"+PRODUCT_CONTAINS_DRUG_LABEL+"]->(d:"+DRUG_LABEL+")\n" +
						"CALL {\n" +
						"    WITH d\n" +
						"    OPTIONAL MATCH (d)-[:"+DRUG_HAS_DOSE_FORM_LABEL+"]->(df:"+MMI_DOSE_FORM_LABEL+")\n" +
						"    OPTIONAL MATCH (df)-[:"+DOSE_FORM_IS_EDQM+"]->(ef:"+EDQM_LABEL+")\n" +
						"    OPTIONAL MATCH (ef)-[:"+EDQM_HAS_CHARACTERISTIC_LABEL+"]->(ec:"+ EDQM_LABEL+")\n" +
						"    RETURN df, ef, collect(CASE WHEN ec IS NULL THEN NULL ELSE {name:ec.name,type:ec.type,code:ec.code} END) AS characteristics\n" +
						"}\n" +
						"CALL {\n" +
						"    WITH d\n" +
						"    OPTIONAL MATCH (d)-[:"+DRUG_CONTAINS_INGREDIENT_LABEL+"]->(i:"+MMI_INGREDIENT_LABEL+" {isActive: true})-[:"+INGREDIENT_IS_SUBSTANCE_LABEL+"]->(is:"+SUBSTANCE_LABEL+")\n" +
						"    OPTIONAL MATCH (i)-[:"+INGREDIENT_CORRESPONDS_TO_LABEL+"]->(ci:"+INGREDIENT_LABEL+")-[:"+INGREDIENT_IS_SUBSTANCE_LABEL+"]->(cs:"+SUBSTANCE_LABEL+")\n" +
						"    OPTIONAL MATCH (i)-[:"+INGREDIENT_HAS_UNIT_LABEL+"]->(iu:"+UNIT_LABEL+")\n" +
						"    OPTIONAL MATCH (ci)-[:"+INGREDIENT_HAS_UNIT_LABEL+"]->(cu:"+UNIT_LABEL+")\n" +
						"    RETURN collect(CASE WHEN NOT i IS NULL THEN {iName:is.name,iMassFrom:i.massFrom,iMassTo:i.massTo,iUnit:iu.name,cName:cs.name,cMassFrom:ci.massFrom,cMassTo:ci.massTo,cUnit:cu.name} ELSE NULL END) AS ingredients\n" +
						"}\n" +
						"OPTIONAL MATCH (d)-[:"+DRUG_HAS_UNIT_LABEL+"]->(du:"+UNIT_LABEL+")\n" +
						"WITH p, collect(CASE WHEN NOT d IS NULL THEN {mmiDoseForm:df.mmiName, edqmDoseForm:(CASE WHEN ef IS NULL THEN NULL ELSE {name:ef.name,code:ef.code,characteristics:characteristics} END), amount:d.amount, unit:du.name, ingredients:ingredients} ELSE NULL END) AS drugs\n" +
						"OPTIONAL MATCH (p)<-[:"+PACKAGE_BELONGS_TO_PRODUCT_LABEL+"]-(pk:"+PACKAGE_LABEL+")<-[:"+CODE_REFERENCE_RELATIONSHIP_NAME+"]-(pzn:"+PZN_LABEL+")\n" +
						"RETURN p.mmiId AS productId, p.name as productName, drugs, collect(pzn.code) AS pzns",
				parameters("productIds", productIds)
		));

		return result.stream().map(this::parseToDetailedProduct).collect(Collectors.toSet());
	}

	private DetailedProduct parseToDetailedProduct(Record record) {
		long productId = record.get("productId").asLong();
		String productName = record.get("productName").asString();
		List<String> pzns = new ArrayList<>(record.get("pzns").asList(Value::asString));
		pzns.sort(Comparator.naturalOrder());
		List<Drug> drugList = new ArrayList<>(record.get("drugs").asList(this::parseToDrug, null));
		drugList.sort(drugComparator);
		return new DetailedProduct(productId, productName, pzns, drugList);
	}

	private Drug parseToDrug(Value value) {
		String doseForm = value.get("mmiDoseForm").asString(null);
		EdqmPharmaceuticalDoseForm edqm = parseToPdf(value.get("edqmDoseForm"));
		BigDecimal amountValue = toBigDecimal(value.get("amount").asString(null));
		String unit = value.get("unit").asString(null);
		Amount amount = amountValue != null ? new Amount(amountValue, unit) : null;
		List<ActiveIngredient> ingredients = value.get("ingredients").asList(this::parseToActiveIngredient);
		return new Drug(doseForm, edqm, amount, ingredients);
	}

	private EdqmPharmaceuticalDoseForm parseToPdf(Value value) {
		if (value.isNull()) return null;
		return new EdqmPharmaceuticalDoseForm(
				value.get("code").asString(),
				value.get("name").asString(),
				value.get("characteristics").asList(this::parseToConcept)
		);
	}

	private EdqmConcept parseToConcept(Value value) {
		return new EdqmConcept(
				value.get("code").asString(),
				value.get("name").asString(),
				Objects.requireNonNull(EDQM.fromTypeFullName(value.get("type").asString()))
		);
	}

	private ActiveIngredient parseToActiveIngredient(Value value) {
		String name = value.get("iName").asString(null);
		BigDecimal massFrom = toBigDecimal(value.get("iMassFrom").asString(null));
		BigDecimal massTo = toBigDecimal(value.get("iMassTo").asString(null));
		String unit = value.get("iUnit").asString(null);
		AmountOrRange amount;
		if (massFrom == null) {
			amount = null;
		} else {
			amount = AmountRange.ofNullableUpperEnd(massFrom, massTo, unit);
		}

		String correspondingName = value.get("cName").asString(null);
		if (correspondingName != null) {
			BigDecimal correspondingMassFrom = toBigDecimal(value.get("cMassFrom").asString(null));
			AmountOrRange correspondingAmount;
			if (correspondingMassFrom == null) {
				correspondingAmount = null;
			} else {
				BigDecimal correspondingMassTo = toBigDecimal(value.get("cMassTo").asString(null));
				String correspondingUnit = value.get("cUnit").asString(null);
				correspondingAmount = AmountRange.ofNullableUpperEnd(correspondingMassFrom, correspondingMassTo,
						correspondingUnit);
			}

			return new CorrespondingActiveIngredient(name, amount, correspondingName, correspondingAmount);
		}

		return new ActiveIngredient(name, amount);
	}

	private static BigDecimal toBigDecimal(String germanValue) {
		if (germanValue == null) return null;
		return new BigDecimal(germanValue.replace(',', '.'));
	}

}
