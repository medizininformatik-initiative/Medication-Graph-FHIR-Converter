package de.medizininformatikinitiative.medgraph.searchengine.db;

import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.HashSet;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.UNIT_LABEL;
import static org.neo4j.driver.Values.parameters;

/**
 * Implementation of {@link Database} primarily relying on Cypher queries.
 *
 * @author Markus Budeus
 */
public class Neo4jCypherDatabase implements Database {

	private final Session session;

	/**
	 * Creates a new instance.
	 * @param session the session to use for accessing the database
	 */
	public Neo4jCypherDatabase(Session session) {
		this.session = session;
	}

	@Override
	public Set<DbDosagesByProduct> getDrugDosagesByProduct(Set<Long> productIds) {
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
						"collect({amountFrom:i.massFrom,amountTo:i.massTo,unit:u.print}) AS dosage\n" +
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

}
