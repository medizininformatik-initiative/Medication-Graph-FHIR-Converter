package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.FhirExportTestFactory.GraphUnits.*;
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
		assertEquals(new BigDecimal(10), quantity.getValue());
		assertEquals("http://unitsofmeasure.org", quantity.getSystem());
		assertEquals(MG.name(), quantity.getCode());
		assertEquals(MG.print(), quantity.getUnit());
	}

	@Test
	void toFhirQuantityUsesUnitNameOrUcumCs() {
		GraphUnit unit = new GraphUnit("yes", "no", "no", "yes", "no", "no");
		Quantity quantity = GraphUtil.toFhirQuantity(new BigDecimal("10.78"), null, unit);

		assertEquals(new BigDecimal("10.78"), quantity.getValue());
		assertEquals("yes", quantity.getCode());
	}

	@Test
	void toFhirQuantityUsesGreaterEqual() {
		Quantity quantity = GraphUtil.toFhirQuantity(new BigDecimal("123.45"), new BigDecimal(125), null);


		assertEquals(new BigDecimal("123.45"), quantity.getValue());
		assertEquals(Quantity.QuantityComparator.GREATER_OR_EQUAL, quantity.getComparator());
		assertNull(quantity.getCode());
		assertNull(quantity.getSystem());
		assertNull(quantity.getUnit());
	}

	@Test
	void toFhirQuantityRemovedUnitSystemIfNotUcumUnit() {
		Quantity quantity = GraphUtil.toFhirQuantity(new BigDecimal("123.45"), new BigDecimal(125), FCC_UNITS);

		assertEquals(new BigDecimal("123.45"), quantity.getValue());
		assertEquals(Quantity.QuantityComparator.GREATER_OR_EQUAL, quantity.getComparator());
		assertNull(quantity.getCode());
		assertNull(quantity.getSystem());
		assertEquals(FCC_UNITS.print(), quantity.getUnit());
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
	void toFhirRatio() {
		Ratio ratio = GraphUtil.toFhirRatio(new BigDecimal("123.7"), null, ML);

		assertTrue(new Quantity(null, 123.7D, "http://unitsofmeasure.org", "ml", "ml").equalsDeep(ratio.getNumerator()));
		assertTrue(new Quantity(1).equalsDeep(ratio.getDenominator()));
		assertEquals("ml", ratio.getNumerator().getUnit());
	}

	@Test
	void toFhirRatioWithComparator() {
		Ratio ratio = GraphUtil.toFhirRatio(new BigDecimal("123.7"), new BigDecimal("123.8"), ML);

		assertTrue(new Quantity(
				Quantity.QuantityComparator.GREATER_OR_EQUAL, 123.7D, "http://unitsofmeasure.org", "ml", "ml").equalsDeep(ratio.getNumerator()));
		assertTrue(new Quantity(1).equalsDeep(ratio.getDenominator()));
		assertEquals("ml", ratio.getNumerator().getUnit());
	}

	@Test
	void toCodeableConcept() {
		GraphCode code1 = new GraphCode("54165", "system1", null, null);
		GraphCode code2 = new GraphCode("1111", "system2", LocalDate.now(), null);

		CodeableConcept concept = GraphUtil.toCodeableConcept(List.of(
				code1, code2
		));

		assertEquals(2, concept.getCoding().size());
		assertTrue(code1.toCoding().equalsDeep(concept.getCoding().get(0)));
		assertTrue(code2.toCoding().equalsDeep(concept.getCoding().get(1)));
	}

	@Test
	void toCodeableConceptWithoutCodes() {
		CodeableConcept concept = GraphUtil.toCodeableConcept(List.of());
		assertEquals(0, concept.getCoding().size());
		assertNull(concept.getText());
	}

	@Test
	void toCodeableConceptNull() {
		assertNull(GraphUtil.toCodeableConcept(null));
	}

	@Test
	void toCodeableConceptWithOneCode() {
		GraphAtc code = new GraphAtc("54165", "system1", LocalDate.now(), null, "Alcohol");

		CodeableConcept concept = GraphUtil.toCodeableConcept(List.of(
				code
		));

		assertEquals(1, concept.getCoding().size());
		assertTrue(code.toCoding().equalsDeep(concept.getCoding().getFirst()));
		assertEquals("Alcohol", concept.getText());
	}

}