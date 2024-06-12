package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class NewRefinedQueryTest extends UnitTest {

	private final Identifier<List<String>> SAMPLE_PRODUCT_KEYWORDS = new OriginalIdentifier<>(
			List.of("Sodium", "chloride"), OriginalIdentifier.Source.RAW_QUERY);

	@Test
	void productIdentifierRequired() {
		assertThrows(IllegalStateException.class, () -> {
			new NewRefinedQuery.Builder()
					.withDosage(new OriginalMatch<>(TestFactory.SAMPLE_DOSAGE_1))
					.withDoseForm(new OriginalMatch<>(TestFactory.DoseForms.SOLUTION_FOR_INJECTION))
					.withDoseFormCharacteristic(new OriginalMatch<>(TestFactory.DoseForms.Characteristics.CONVENTIONAL))
					.withSubstance(new OriginalMatch<>(TestFactory.SAMPLE_SUBSTANCE_1))
					.withDrugAmount(new OriginalMatch<>(TestFactory.SAMPLE_AMOUNT_1))
					.build();
		});
	}

	@Test
	void outputCorrect() {
		OriginalMatch<Substance> substance = new OriginalMatch<>(TestFactory.SAMPLE_SUBSTANCE_1);
		OriginalMatch<Amount> drugAmount = new OriginalMatch<>(TestFactory.SAMPLE_AMOUNT_1);
		OriginalMatch<Dosage> dosage = new OriginalMatch<>(TestFactory.SAMPLE_DOSAGE_1);
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm = new OriginalMatch<>(
				TestFactory.DoseForms.SOLUTION_FOR_INJECTION);
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm1 = new OriginalMatch<>(TestFactory.DoseForms.GRANULES);
		OriginalMatch<EdqmConcept> doseFormCharacteristic = new OriginalMatch<>(
				TestFactory.DoseForms.Characteristics.CONVENTIONAL);
		OriginalMatch<EdqmConcept> doseFormCharacteristic1 = new OriginalMatch<>(
				TestFactory.DoseForms.Characteristics.CONVENTIONAL);

		NewRefinedQuery refinedQuery = new NewRefinedQuery.Builder()
				.withProductNameKeywords(SAMPLE_PRODUCT_KEYWORDS)
				.withDosage(dosage)
				.withDoseForm(doseForm)
				.withDoseForm(doseForm1)
				.withDoseFormCharacteristic(doseFormCharacteristic)
				.withDoseFormCharacteristic(doseFormCharacteristic1)
				.withSubstance(substance)
				.withDrugAmount(drugAmount)
				.build();

		assertSame(substance, refinedQuery.getSubstances().getFirst());
		assertEquals(List.of(drugAmount), refinedQuery.getDrugAmounts());
		assertEquals(List.of(dosage), refinedQuery.getDosages());
		assertEquals(List.of(doseForm, doseForm1), refinedQuery.getDoseForms());
		assertEquals(List.of(new Merge<>(List.of(doseFormCharacteristic, doseFormCharacteristic1))),
				refinedQuery.getDoseFormCharacteristics());
		assertEquals(List.of(substance), refinedQuery.getSubstances());
		assertEquals(SAMPLE_PRODUCT_KEYWORDS, refinedQuery.getProductNameKeywords());

		SearchQuery searchQuery = refinedQuery.toSearchQuery();
		assertEquals(SAMPLE_PRODUCT_KEYWORDS.getIdentifier(), searchQuery.getProductNameKeywords());
		assertEquals(List.of(TestFactory.SAMPLE_SUBSTANCE_1), searchQuery.getSubstances());
		assertEquals(List.of(TestFactory.SAMPLE_AMOUNT_1), searchQuery.getDrugAmounts());
		assertEquals(List.of(TestFactory.SAMPLE_DOSAGE_1), searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(TestFactory.DoseForms.SOLUTION_FOR_INJECTION, TestFactory.DoseForms.GRANULES),
				searchQuery.getDoseForms());
		assertEquals(List.of(TestFactory.DoseForms.Characteristics.CONVENTIONAL),
				searchQuery.getDoseFormCharacteristics());
	}

	@Test
	void substanceDuplicatesMerged() {
		OriginalMatch<Substance> substance1 = new OriginalMatch<>(TestFactory.SAMPLE_SUBSTANCE_1);
		OriginalMatch<Substance> substance2 = new OriginalMatch<>(TestFactory.SAMPLE_SUBSTANCE_2);
		OriginalMatch<Substance> substance3 = new OriginalMatch<>(TestFactory.SAMPLE_SUBSTANCE_1);
		OriginalMatch<Substance> substance4 = new OriginalMatch<>(TestFactory.SAMPLE_SUBSTANCE_3);

		NewRefinedQuery refinedQuery = new NewRefinedQuery.Builder()
				.withProductNameKeywords(SAMPLE_PRODUCT_KEYWORDS)
				.withSubstance(substance1)
				.withSubstance(substance2)
				.withSubstance(substance3)
				.withSubstance(substance4)
				.build();

		assertEquals(
				List.of(
						new Merge<>(List.of(substance1, substance3)),
						substance2,
						substance4
				),
				refinedQuery.getSubstances()
		);
	}

	@Test
	void dosageDuplicatesMerged() {
		OriginalMatch<Dosage> dosage1 = new OriginalMatch<>(TestFactory.SAMPLE_DOSAGE_1);
		OriginalMatch<Dosage> dosage2 = new OriginalMatch<>(TestFactory.SAMPLE_DOSAGE_2);
		OriginalMatch<Dosage> dosage3 = new OriginalMatch<>(TestFactory.SAMPLE_DOSAGE_2);

		NewRefinedQuery refinedQuery = new NewRefinedQuery.Builder()
				.withProductNameKeywords(SAMPLE_PRODUCT_KEYWORDS)
				.withDosage(dosage1)
				.withDosage(dosage2)
				.withDosage(dosage3)
				.build();

		assertEquals(
				List.of(
						dosage1,
						new Merge<>(List.of(dosage2, dosage3))
				),
				refinedQuery.getDosages()
		);
	}

	@Test
	void drugAmountDuplicatesMerged() {
		OriginalMatch<Dosage> dosage1 = new OriginalMatch<>(TestFactory.SAMPLE_DOSAGE_1);
		OriginalMatch<Dosage> dosage2 = new OriginalMatch<>(TestFactory.SAMPLE_DOSAGE_2);
		OriginalMatch<Dosage> dosage3 = new OriginalMatch<>(TestFactory.SAMPLE_DOSAGE_2);

		NewRefinedQuery refinedQuery = new NewRefinedQuery.Builder()
				.withProductNameKeywords(SAMPLE_PRODUCT_KEYWORDS)
				.withDosage(dosage1)
				.withDosage(dosage2)
				.withDosage(dosage3)
				.build();

		assertEquals(
				List.of(
						dosage1,
						new Merge<>(List.of(dosage2, dosage3))
				),
				refinedQuery.getDosages()
		);
	}

	@Test
	void doseFormDuplicatesMerged() {
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm1 = new OriginalMatch<>(
				TestFactory.DoseForms.SOLUTION_FOR_INJECTION);
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm2 = new OriginalMatch<>(TestFactory.DoseForms.GRANULES);
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm3 = new OriginalMatch<>(
				TestFactory.DoseForms.SOLUTION_FOR_INJECTION_OR_INFUSION);
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm4 = new OriginalMatch<>(TestFactory.DoseForms.GRANULES);
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm5 = new OriginalMatch<>(
				TestFactory.DoseForms.SOLUTION_FOR_INJECTION_OR_INFUSION);
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm6 = new OriginalMatch<>(
				TestFactory.DoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION);
		OriginalMatch<EdqmPharmaceuticalDoseForm> doseForm7 = new OriginalMatch<>(
				TestFactory.DoseForms.SOLUTION_FOR_INJECTION_OR_INFUSION);

		NewRefinedQuery refinedQuery = new NewRefinedQuery.Builder()
				.withProductNameKeywords(SAMPLE_PRODUCT_KEYWORDS)
				.withDoseForm(doseForm1)
				.withDoseForm(doseForm2)
				.withDoseForm(doseForm3)
				.withDoseForm(doseForm4)
				.withDoseForm(doseForm5)
				.withDoseForm(doseForm6)
				.withDoseForm(doseForm7)
				.build();

		assertEquals(
				List.of(
						doseForm1,
						new Merge<>(List.of(doseForm2, doseForm4)),
						new Merge<>(List.of(doseForm3, doseForm5, doseForm7)),
						doseForm6
				),
				refinedQuery.getDoseForms()
		);
	}

	@Test
	void doseFormCharacteristicDuplicatesMerged() {
		OriginalMatch<EdqmConcept> doseForm1 = new OriginalMatch<>(TestFactory.DoseForms.Characteristics.SOLUTION);
		OriginalMatch<EdqmConcept> doseForm2 = new OriginalMatch<>(TestFactory.DoseForms.Characteristics.GRANULES);
		OriginalMatch<EdqmConcept> doseForm3 = new OriginalMatch<>(TestFactory.DoseForms.Characteristics.ORAL);
		OriginalMatch<EdqmConcept> doseForm4 = new OriginalMatch<>(TestFactory.DoseForms.Characteristics.CONVENTIONAL);
		OriginalMatch<EdqmConcept> doseForm5 = new OriginalMatch<>(TestFactory.DoseForms.Characteristics.CONVENTIONAL);
		OriginalMatch<EdqmConcept> doseForm6 = new OriginalMatch<>(TestFactory.DoseForms.Characteristics.POWDER);
		OriginalMatch<EdqmConcept> doseForm7 = new OriginalMatch<>(TestFactory.DoseForms.Characteristics.SOLUTION);

		NewRefinedQuery refinedQuery = new NewRefinedQuery.Builder()
				.withProductNameKeywords(SAMPLE_PRODUCT_KEYWORDS)
				.withDoseFormCharacteristic(doseForm1)
				.withDoseFormCharacteristic(doseForm2)
				.withDoseFormCharacteristic(doseForm3)
				.withDoseFormCharacteristic(doseForm4)
				.withDoseFormCharacteristic(doseForm5)
				.withDoseFormCharacteristic(doseForm6)
				.withDoseFormCharacteristic(doseForm7)
				.build();

		assertEquals(
				List.of(
						new Merge<>(List.of(doseForm1, doseForm7)),
						doseForm2,
						doseForm3,
						new Merge<>(List.of(doseForm4, doseForm5)),
						doseForm6
				),
				refinedQuery.getDoseFormCharacteristics()
		);
	}

}