package de.medizininformatikinitiative.medgraph.searchengine.db;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.Products.*;
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

		DbDosagesByProduct expected = constructExpectedDormicum15Instance();
		assertEquals(expected, dosageInfo);
	}

	@Test
	public void getDrugDosagesForDormicum5() {
		Set<DbDosagesByProduct> dosageSet = sut.getDrugDosagesByProduct(Set.of(DORMICUM_5.getId()));
		assertEquals(1, dosageSet.size());

		DbDosagesByProduct dosageInfo = dosageSet.iterator().next();

		DbDosagesByProduct expected = constructExpectedDormicum5Instance();
		assertEquals(expected, dosageInfo);
	}

	@Test
	public void getDrugDosagesForAspirin() {
		Set<DbDosagesByProduct> dosageSet = sut.getDrugDosagesByProduct(Set.of(ASPIRIN.getId()));
		assertEquals(1, dosageSet.size());

		DbDosagesByProduct dosageInfo = dosageSet.iterator().next();

		DbDosagesByProduct expected = constructExpectedAspirinInstance();
		assertEquals(expected, dosageInfo);
	}

	@Test
	public void getDrugDosagesForAnapen() {
		Set<DbDosagesByProduct> dosageSet = sut.getDrugDosagesByProduct(Set.of(ANAPEN.getId()));
		assertEquals(1, dosageSet.size());

		DbDosagesByProduct dosageInfo = dosageSet.iterator().next();

		DbDosagesByProduct expected = constructExpectedAnapenInstance();
		assertEquals(expected, dosageInfo);
	}

	@Test
	public void notFound() {
		assertTrue(sut.getDrugDosagesByProduct(Set.of(16841546814L, 56846816L)).isEmpty());
	}

	@Test
	public void batchLoad() {
		Set<DbDosagesByProduct> dosageSet = sut.getDrugDosagesByProduct(Set.of(
				ASPIRIN.getId(),
				ANAPEN.getId(),
				DORMICUM_15.getId(),
				-18541435L
		));

		assertEquals(3, dosageSet.size());

		Map<Long, DbDosagesByProduct> dosagesByProductId = new HashMap<>();
		dosageSet.forEach(s -> dosagesByProductId.put(s.productId, s));

		assertEquals(constructExpectedAspirinInstance(), dosagesByProductId.get(ASPIRIN.getId()));
		assertEquals(constructExpectedAnapenInstance(), dosagesByProductId.get(ANAPEN.getId()));
		assertEquals(constructExpectedDormicum15Instance(), dosagesByProductId.get(DORMICUM_15.getId()));
	}

	private DbDosagesByProduct constructExpectedDormicum5Instance() {
		return new DbDosagesByProduct(DORMICUM_5.getId(),
				List.of(new DbDrugDosage(
						new DbAmount(BigDecimal.valueOf(3), "ml"),
						List.of(
								new DbDosage(new BigDecimal("5.5"), new BigDecimal("5.7"), "mg"),
								new DbDosage(new BigDecimal("5"), null, "mg")
						)
				))
		);
	}

	private DbDosagesByProduct constructExpectedDormicum15Instance() {
		return new DbDosagesByProduct(DORMICUM_15.getId(),
				List.of(new DbDrugDosage(
						new DbAmount(BigDecimal.valueOf(3), "ml"),
						List.of(
								new DbDosage(new BigDecimal("16.68"), null, "mg"),
								new DbDosage(new BigDecimal("15"), null, "mg")
						)
				))
		);
	}

	private DbDosagesByProduct constructExpectedAspirinInstance() {
		return new DbDosagesByProduct(ASPIRIN.getId(),
				List.of(new DbDrugDosage(
						new DbAmount(BigDecimal.valueOf(1), null),
						List.of(
								new DbDosage(new BigDecimal("500"), null, "mg")
						)
				))
		);
	}

	private DbDosagesByProduct constructExpectedAnapenInstance() {
		return new DbDosagesByProduct(ANAPEN.getId(),
				List.of(new DbDrugDosage(
						new DbAmount(new BigDecimal("0.3"), "ml"),
						List.of(
								// This one ensures UCUM cs notation is used, so unit is "ug"
								new DbDosage(new BigDecimal("300"), null, "ug")
						)
				))
		);
	}

}