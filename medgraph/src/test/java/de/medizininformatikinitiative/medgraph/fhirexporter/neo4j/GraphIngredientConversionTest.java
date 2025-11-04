package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.ExtensionWirkstoffRelation;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.ExtensionWirkstoffTyp;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
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
		Medication.MedicationIngredientComponent fhirIngredient = ingredient.toFhirIngredient();
		assertNotNull(fhirIngredient);
		assertBasicsMatch(ingredient, fhirIngredient);
	}

	@ParameterizedTest
	@MethodSource("ingredients")
	void testDetailedConversion(GraphIngredient ingredient) {
		List<Medication.MedicationIngredientComponent> fhirIngredients = ingredient.toFhirIngredientsWithCorrespoindingIngredient(
				1);
		assertNotNull(fhirIngredients);

		Medication.MedicationIngredientComponent precise = fhirIngredients.getFirst();
		assertBasicsMatch(ingredient, precise);
		if (ingredient.getCorrespondingIngredients().isEmpty()) {
			assertEquals(1, fhirIngredients.size());
		} else {
			assertEquals("#ing_1", precise.getId());
			assertWirkstoffTyp("PIN", precise);

			for (int i = 1; i < fhirIngredients.size(); i++) {
				Medication.MedicationIngredientComponent ci = fhirIngredients.get(i);
				assertEquals("#ing_" + (i + 1), ci.getId());
				assertFalse(ci.hasIsActive());
				assertWirkstoffTyp("IN", ci);
				assertWirkstoffRelationTo(precise.getId(), ci);
			}

		}
	}

	@Test
	void testDetailedConversionWithDifferentId() {
		List<Medication.MedicationIngredientComponent> fhirIngredients = FhirExportTestFactory.GraphIngredients.MIDAZOLAM_HYDROCHLORIDE
				.toFhirIngredientsWithCorrespoindingIngredient(3);
		assertNotNull(fhirIngredients);
		assertEquals("#ing_3", fhirIngredients.getFirst().getId());
		assertEquals("#ing_4", fhirIngredients.getLast().getId());
	}

	private void assertWirkstoffTyp(String wirkstofftyp, Medication.MedicationIngredientComponent ingredient) {
		for (Extension e : ingredient.getExtension()) {
			if (ExtensionWirkstoffTyp.URL.equals(e.getUrl())) {
				assertEquals(wirkstofftyp, ((Coding) e.getValue()).getCode());
				return;
			}
		}
		fail("The extension Wirkstofftyp is not present!");
	}

	private void assertWirkstoffRelationTo(String targetIdentifier,
	                                       Medication.MedicationIngredientComponent ingredient) {
		for (Extension e : ingredient.getExtension()) {
			if (ExtensionWirkstoffRelation.URL.equals(e.getUrl())) {
				assertEquals(targetIdentifier, e.getExtension().getFirst().getValueAsPrimitive().getValue());
				return;
			}
		}
		fail("The extension Wirkstofftyp is not present!");
	}

	private void assertBasicsMatch(SimpleGraphIngredient ingredient,
	                               Medication.MedicationIngredientComponent fhirIngredient) {
		if (ingredient instanceof GraphIngredient g) {
			assertEquals(g.isActive(), fhirIngredient.getIsActive());
		}
		assertTrue(GraphUtil.toFhirRatio(ingredient.getMassFrom(), ingredient.getMassTo(), ingredient.getUnit())
		                    .equalsDeep(fhirIngredient.getStrength()));
		assertEquals(ingredient.getSubstanceName(), fhirIngredient.getItemReference().getDisplay());
		assertEquals(IdProvider.fromSubstanceMmiId(ingredient.getSubstanceMmiId()),
				fhirIngredient.getItemReference().getReferenceElement().getIdPart());
	}

	static Stream<Arguments> ingredients() {
		return Catalogue.<GraphIngredient>getAllFields(FhirExportTestFactory.GraphIngredients.class, false)
		                .stream()
		                .map(i -> Arguments.arguments(named(i.substanceName, i)));
	}

}