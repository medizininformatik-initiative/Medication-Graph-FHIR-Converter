package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class GraphIngredientConversionTest extends UnitTest {

	@Test
	void sample1() {
		GraphIngredient graphIngredient = new GraphIngredient(
				17L,
				"Prednisolon",
				true,
				new BigDecimal("250"),
				null,
				TestFactory.GraphUnits.MG
		);

		Ingredient ingredient = graphIngredient.toFhirIngredient();
		assertNotNull(ingredient);
		assertTrue(ingredient.isActive);
		assertEquals(GraphUtil.toFhirRatio(new BigDecimal(250), null, TestFactory.GraphUnits.MG), ingredient.strength);
		assertEquals("Prednisolon", ingredient.itemReference.display);
	}

	@Test
	void sample2() {
		GraphIngredient graphIngredient = new GraphIngredient(
				224L,
				"Wasser",
				false,
				new BigDecimal("4"),
				new BigDecimal("8"),
				null
		);

		Ingredient ingredient = graphIngredient.toFhirIngredient();
		assertNotNull(ingredient);
		assertFalse(ingredient.isActive);
		assertEquals(GraphUtil.toFhirRatio(new BigDecimal(4), new BigDecimal(8), null), ingredient.strength);
		assertEquals("Wasser", ingredient.itemReference.display);
		assertEquals(Identifier.fromSubstanceMmiId(224L), ingredient.itemReference.identifier);
	}

}