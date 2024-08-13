package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

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
		assertEquals("2024-08-13", GraphUtil.toFhirDate(LocalDate.of(2024,8,13)));
	}

}