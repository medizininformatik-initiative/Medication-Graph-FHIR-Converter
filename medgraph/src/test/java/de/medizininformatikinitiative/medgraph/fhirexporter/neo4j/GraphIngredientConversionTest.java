package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Ratio;
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
		assertTrue(ingredient.isActive);
		// TODO Continue test, remaining conversion tests
	}

}