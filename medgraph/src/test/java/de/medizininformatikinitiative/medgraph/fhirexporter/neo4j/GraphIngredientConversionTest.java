package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Extension;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.ExtensionWirkstoffRelation;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.ExtensionWirkstoffTyp;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;

/**
 * @author Markus Budeus
 */
public class GraphIngredientConversionTest extends UnitTest {

	@ParameterizedTest
	@MethodSource("ingredients")
	void testConversion(GraphIngredient ingredient) {
		Ingredient fhirIngredient = ingredient.toFhirIngredient();
		assertNotNull(fhirIngredient);
		assertBasicsMatch(ingredient, fhirIngredient);
	}

	@ParameterizedTest
	@MethodSource("ingredients")
	void testDetailedConversion(GraphIngredient ingredient) {
		List<Ingredient> fhirIngredients = ingredient.toFhirIngredientsWithCorrespoindingIngredient(1);
		assertNotNull(fhirIngredients);

		Ingredient precise = fhirIngredients.getFirst();
		assertBasicsMatch(ingredient, precise);
		if (ingredient.getCorrespondingIngredients().isEmpty()) {
			assertEquals(1, fhirIngredients.size());
		} else {
			assertEquals("#ing_1", precise.id);
			assertWirkstoffTyp("PIN", precise);

			for (int i = 1; i < fhirIngredients.size(); i++) {
				Ingredient ci = fhirIngredients.get(i);
				assertEquals("#ing_"+(i + 1), ci.id);
				assertNull(ci.isActive);
				assertWirkstoffTyp("IN", ci);
				assertWirkstoffRelationTo(precise.id, ci);
			}

		}
	}

	@Test
	void testDetailedConversionWithDifferentId() {
		List<Ingredient> fhirIngredients = FhirExportTestFactory.GraphIngredients.MIDAZOLAM_HYDROCHLORIDE
				.toFhirIngredientsWithCorrespoindingIngredient(3);
		assertNotNull(fhirIngredients);
		assertEquals("#ing_3", fhirIngredients.getFirst().id);
		assertEquals("#ing_4", fhirIngredients.getLast().id);
	}

	private void assertWirkstoffTyp(String wirkstofftyp, Ingredient ingredient) {
		for (Extension e: ingredient.extension) {
			if (e instanceof ExtensionWirkstoffTyp ex) {
				assertEquals(wirkstofftyp, ex.valueCoding.code);
				return;
			}
		}
		fail("The extension Wirkstofftyp is not present!");
	}

	private void assertWirkstoffRelationTo(String targetIdentifier, Ingredient ingredient) {
		for (Extension e: ingredient.extension) {
			if (e instanceof ExtensionWirkstoffRelation ex) {
				assertEquals(targetIdentifier, ex.extension[0].valueUri);
				return;
			}
		}
		fail("The extension Wirkstofftyp is not present!");
	}

	private void assertBasicsMatch(SimpleGraphIngredient ingredient, Ingredient fhirIngredient) {
		if (ingredient instanceof GraphIngredient g) {
			assertEquals(g.isActive(), fhirIngredient.isActive);
		}
		assertEquals(GraphUtil.toFhirRatio(ingredient.getMassFrom(), ingredient.getMassTo(), ingredient.getUnit()),
				fhirIngredient.strength);
		assertEquals(ingredient.getSubstanceName(), fhirIngredient.itemReference.display);
		assertEquals(Identifier.fromSubstanceMmiId(ingredient.getSubstanceMmiId()),
				fhirIngredient.itemReference.identifier);
	}

	static Stream<Arguments> ingredients() {
		return Catalogue.<GraphIngredient>getAllFields(FhirExportTestFactory.GraphIngredients.class, false)
		                .stream()
				.map(i -> Arguments.arguments(named(i.substanceName, i)));
	}

}