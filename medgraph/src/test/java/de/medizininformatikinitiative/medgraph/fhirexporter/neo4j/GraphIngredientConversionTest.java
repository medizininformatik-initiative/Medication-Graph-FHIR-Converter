package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Markus Budeus
 */
public class GraphIngredientConversionTest extends UnitTest {

	@ParameterizedTest
	@MethodSource("ingredients")
	void testConversion(GraphIngredient ingredient) {
		Ingredient fhirIngredient = ingredient.toFhirIngredient();
		assertNotNull(fhirIngredient);
		assertEquals(fhirIngredient.isActive, ingredient.isActive());
		assertEquals(GraphUtil.toFhirRatio(ingredient.massFrom(), ingredient.massTo(), ingredient.unit()),
				fhirIngredient.strength);
		assertEquals(ingredient.substanceName(), fhirIngredient.itemReference.display);
		assertEquals(Identifier.fromSubstanceMmiId(ingredient.substanceMmiId()),
				fhirIngredient.itemReference.identifier);
	}

	static Stream<GraphIngredient> ingredients() {
		return Catalogue.<GraphIngredient>getAllFields(FhirExportTestFactory.GraphIngredients.class, false).stream();
	}

}