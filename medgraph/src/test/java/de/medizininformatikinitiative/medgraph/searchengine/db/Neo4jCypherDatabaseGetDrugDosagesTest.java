package de.medizininformatikinitiative.medgraph.searchengine.db;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.ActiveIngredient;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.AmountRange;
import de.medizininformatikinitiative.medgraph.searchengine.model.CorrespondingActiveIngredient;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.TestFactory.Products.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class Neo4jCypherDatabaseGetDrugDosagesTest extends Neo4jTest {

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

		DbDosagesByProduct expected = ofDetailedInfo(Detailed.DORMICUM_15);
		assertEquals(expected, dosageInfo);
	}

	@Test
	public void getDrugDosagesForDormicum5() {
		Set<DbDosagesByProduct> dosageSet = sut.getDrugDosagesByProduct(Set.of(DORMICUM_5.getId()));
		assertEquals(1, dosageSet.size());

		DbDosagesByProduct dosageInfo = dosageSet.iterator().next();

		DbDosagesByProduct expected = ofDetailedInfo(Detailed.DORMICUM_5);
		assertEquals(expected, dosageInfo);
	}

	@Test
	public void getDrugDosagesForAspirin() {
		Set<DbDosagesByProduct> dosageSet = sut.getDrugDosagesByProduct(Set.of(ASPIRIN.getId()));
		assertEquals(1, dosageSet.size());

		DbDosagesByProduct dosageInfo = dosageSet.iterator().next();

		DbDosagesByProduct expected = ofDetailedInfo(Detailed.ASPIRIN);
		assertEquals(expected, dosageInfo);
	}

	@Test
	public void getDrugDosagesForAnapen() {
		Set<DbDosagesByProduct> dosageSet = sut.getDrugDosagesByProduct(Set.of(ANAPEN.getId()));
		assertEquals(1, dosageSet.size());

		DbDosagesByProduct dosageInfo = dosageSet.iterator().next();

		DbDosagesByProduct expected = ofDetailedInfo(Detailed.ANAPEN);
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

		assertEquals(ofDetailedInfo(Detailed.ASPIRIN), dosagesByProductId.get(ASPIRIN.getId()));
		assertEquals(ofDetailedInfo(Detailed.ANAPEN), dosagesByProductId.get(ANAPEN.getId()));
		assertEquals(ofDetailedInfo(Detailed.DORMICUM_15), dosagesByProductId.get(DORMICUM_15.getId()));
	}

	private DbDosagesByProduct ofDetailedInfo(DetailedProduct product) {
		return new DbDosagesByProduct(product.getId(),
				product.getDrugs().stream().map(drug ->
						new DbDrugDosage(
								drug.getAmount() == null ? null : new DbAmount(drug.getAmount().getNumber(),
										drug.getAmount().getUnit()),
								drug.getActiveIngredients().stream().flatMap(this::convertIngredient).toList()
						)
				).toList()
		);
	}

	private Stream<DbDosage> convertIngredient(ActiveIngredient ingredient) {
		DbDosage baseDosage = convertToDbDosage(ingredient.getAmount());

		if (ingredient instanceof CorrespondingActiveIngredient ci) {
			return Stream.of(baseDosage, convertToDbDosage(ci.getCorrespondingSubstanceAmount()));
		} else return Stream.of(baseDosage);
	}

	@SuppressWarnings("ConstantConditions")
	private DbDosage convertToDbDosage(Amount amount) {
		if (amount instanceof AmountRange r) {
			return new DbDosage(r.getFrom(), r.getTo(), r.getUnit());
		} else {
			return new DbDosage(amount.getNumber(), amount.getUnit());
		}
	}

}