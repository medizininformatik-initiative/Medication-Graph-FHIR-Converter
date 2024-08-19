package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Quantity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.FhirExportTestFactory.GraphUnits.FCC_UNITS;
import static de.medizininformatikinitiative.medgraph.FhirExportTestFactory.GraphUnits.MG;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class GraphUtilTest extends UnitTest {

	@Test
	void toBigDecimal() {
		assertEquals(BigDecimal.ONE, GraphUtil.toBigDecimal("1"));
		assertEquals(new BigDecimal("1821"), GraphUtil.toBigDecimal("1821"));
		assertEquals(new BigDecimal("1.124816"), GraphUtil.toBigDecimal("1,124816"));
	}

	@Test
	void toFhirDate() {
		assertEquals("2024-08-13", GraphUtil.toFhirDate(LocalDate.of(2024, 8, 13)));
	}

	@Test
	void toNullQuantity() {
		assertNull(GraphUtil.toFhirQuantity(null, null, null));
	}

	@Test
	void toNullRatio() {
		assertNull(GraphUtil.toFhirRatio(null, null, null));
	}

	@Test
	void invalidQuantity() {
		assertThrows(IllegalArgumentException.class, () -> GraphUtil.toFhirQuantity(null, BigDecimal.ONE, null));
		assertThrows(IllegalArgumentException.class, () -> GraphUtil.toFhirQuantity(null, null, MG));
		assertThrows(IllegalArgumentException.class, () -> GraphUtil.toFhirQuantity(null, BigDecimal.ONE, MG));
	}

	@Test
	void toFhirQuantity() {
		Quantity quantity = GraphUtil.toFhirQuantity(new BigDecimal("10"), null, MG);

		assertNull(quantity.getComparator());
		assertEquals(new BigDecimal(10), quantity.value);
		assertEquals(Quantity.UCUM_URI, quantity.system);
		assertEquals(MG.name(), quantity.code);
		assertEquals(MG.print(), quantity.unit);
	}

	@Test
	void toFhirQuantityUsesUnitNameOrUcumCs() {
		GraphUnit unit = new GraphUnit("yes", "no", "no", "yes", "no", "no");
		Quantity quantity = GraphUtil.toFhirQuantity(new BigDecimal("10.78"), null, unit);

		assertEquals(new BigDecimal("10.78"), quantity.value);
		assertEquals("yes", quantity.code);
	}

	@Test
	void toFhirQuantityUsesGreaterEqual() {
		Quantity quantity = GraphUtil.toFhirQuantity(new BigDecimal("123.45"), new BigDecimal(125), null);


		assertEquals(new BigDecimal("123.45"), quantity.value);
		assertEquals(">=", quantity.getComparator());
		assertNull(quantity.code);
		assertNull(quantity.system);
		assertNull(quantity.unit);
	}

	@Test
	void toFhirQuantityRemovedUnitSystemIfNotUcumUnit() {
		Quantity quantity = GraphUtil.toFhirQuantity(new BigDecimal("123.45"), new BigDecimal(125), FCC_UNITS);

		assertEquals(new BigDecimal("123.45"), quantity.value);
		assertEquals(">=", quantity.getComparator());
		assertNull(quantity.code);
		assertNull(quantity.system);
		assertEquals(FCC_UNITS.print(), quantity.unit);
	}

	@Test
	void toFhirQuantityWithMassToEqual() {
		Quantity quantity = GraphUtil.toFhirQuantity(new BigDecimal("127.1"), new BigDecimal("127.1"), null);
		assertNull(quantity.getComparator());
	}

	@Test
	void toFhirQuantityWithMassToLess() {
		assertThrows(IllegalArgumentException.class,
				() -> GraphUtil.toFhirQuantity(new BigDecimal("127.1"), new BigDecimal("126.1"), null));
	}

	@Test
	void toCodeableConcept() {
		GraphCode code1 = new GraphCode("54165", "system1", null, null);
		GraphCode code2 = new GraphCode("1111", "system2", LocalDate.now(), null);

		CodeableConcept concept = GraphUtil.toCodeableConcept(List.of(
				code1, code2
		));

		assertEquals(2, concept.coding.length);
		assertEquals(code1.toCoding(), concept.coding[0]);
		assertEquals(code2.toCoding(), concept.coding[1]);
	}

	@Test
	void toCodeableConceptWithoutCodes() {
		CodeableConcept concept = GraphUtil.toCodeableConcept(List.of());
		assertEquals(0, concept.coding.length);
		assertNull(concept.text);
	}

}