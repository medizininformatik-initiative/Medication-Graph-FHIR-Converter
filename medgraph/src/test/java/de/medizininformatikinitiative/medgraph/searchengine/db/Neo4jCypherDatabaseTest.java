package de.medizininformatikinitiative.medgraph.searchengine.db;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.searchengine.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.Products.DORMICUM_15;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class Neo4jCypherDatabaseTest extends Neo4jTest {

	private Neo4jCypherDatabase sut;

	@BeforeEach
	void setUp() {
		sut = new Neo4jCypherDatabase(session);
	}

	@Test
	public void getDrugDosagesForDormicum15() {
		Set<DbDosagesByProduct> dosageSet = sut.getDrugDosagesByProduct(Set.of(DORMICUM_15.getId()));
		assertEquals(1, dosageSet.size());

		DbDosagesByProduct dosageInfo = dosageSet.iterator().next();

		assertEquals(DORMICUM_15.getId(), dosageInfo.productId);
		assertEquals(1, dosageInfo.drugDosages.size());

		DbDrugDosage drugDosage = dosageInfo.drugDosages.getFirst();
		assertNotNull(drugDosage.amount);
		assertEquals(drugDosage.amount.amount, BigDecimal.valueOf(3));
		assertEquals(drugDosage.amount.unit, "ml");

		assertEquals(Set.of(
				new DbDosage(new BigDecimal("16.68"), null, "mg"),
				new DbDosage(new BigDecimal("15"), null, "mg")
		), new HashSet<>(drugDosage.dosages));
	}

	// TODO Test more

}