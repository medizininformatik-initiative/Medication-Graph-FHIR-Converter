package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.TestFactory.EDQM_PROVIDER;
import static de.medizininformatikinitiative.medgraph.TestFactory.SUBSTANCES_PROVIDER;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class NewQueryRefinerImplTest extends UnitTest {

	private NewQueryRefinerImpl sut;

	@BeforeEach
	void setUp() {
		sut = new NewQueryRefinerImpl(
				new NewDosageQueryRefiner(),
				new NewDoseFormQueryRefiner(EDQM_PROVIDER),
				new NewSubstanceQueryRefiner(SUBSTANCES_PROVIDER)
		);
	}

	@Test
	void simpleExample() {
		RawQuery rawQuery = new RawQuery("Midazolam solution for injection 1mg/ml 5ml");
		NewRefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.toSearchQuery();

		assertEquals(List.of("Midazolam"), searchQuery.getProductNameKeywords());
		assertEquals(List.of(
						Dosage.of(1, "mg", 1, "ml"),
						Dosage.of(5, "ml")
				),
				searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(
				new Amount(new BigDecimal(5), "ml")
		), searchQuery.getDrugAmounts());
		assertEquals(List.of(TestFactory.DoseForms.SOLUTION_FOR_INJECTION), searchQuery.getDoseForms());

		assertNotNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNotNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNotNull(refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDosageUsageStatement());
		assertNull(refinedQuery.getDoseFormUsageStatement());
		assertNull(refinedQuery.getSubstanceUsageStatement());
		assertUsedParts(List.of("Midazolam"), refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertEquals("solution for injection", refinedQuery.getDoseFormGeneralSearchTermUsageStatement()
				.getUsedParts().trim());
		assertUsedParts(List.of("1mg/ml", "5ml"), refinedQuery.getDosageGeneralSearchTermUsageStatement());
	}

	@Test
	void queryDoseFormOnly() {
		RawQuery rawQuery = new RawQuery("", "", "", "", "Granules");
		NewRefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.toSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertTrue(searchQuery.getSubstances().isEmpty());
		assertTrue(searchQuery.getActiveIngredientDosages().isEmpty());
		assertTrue(searchQuery.getDrugAmounts().isEmpty());
		assertEquals(List.of(TestFactory.DoseForms.GRANULES), searchQuery.getDoseForms());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceUsageStatement());
		assertNull(refinedQuery.getDosageUsageStatement());
		assertNotNull(refinedQuery.getDoseFormUsageStatement());
		assertEquals("Granules", refinedQuery.getDoseFormUsageStatement().getUsedParts());
	}

	@Test
	void queryDosageOnly() {
		RawQuery rawQuery = new RawQuery("", "", "", "100 ml", "");
		NewRefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.toSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertTrue(searchQuery.getSubstances().isEmpty());
		assertEquals(List.of(Dosage.of(100, "ml")), searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(new Amount(new BigDecimal(100), "ml")), searchQuery.getDrugAmounts());
		assertTrue(searchQuery.getDoseForms().isEmpty());
		assertTrue(searchQuery.getDoseFormCharacteristics().isEmpty());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceUsageStatement());
		assertNotNull(refinedQuery.getDosageUsageStatement());
		assertEquals("100 ml", refinedQuery.getDosageUsageStatement().getUsedParts());
		assertNull(refinedQuery.getDoseFormUsageStatement());
	}

	@Test
	void querySubstanceOnly() {
		RawQuery rawQuery = new RawQuery("", "", "Epinephrin X", "", "");
		NewRefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.toSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertEquals(List.of(TestFactory.Substances.EPINEPHRINE), searchQuery.getSubstances());
		assertTrue(searchQuery.getActiveIngredientDosages().isEmpty());
		assertTrue(searchQuery.getDrugAmounts().isEmpty());
		assertTrue(searchQuery.getDoseForms().isEmpty());
		assertTrue(searchQuery.getDoseFormCharacteristics().isEmpty());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertNotNull(refinedQuery.getSubstanceUsageStatement());
		assertUsedParts(List.of("Epinephrin"), refinedQuery.getSubstanceUsageStatement());
		assertNull(refinedQuery.getDosageUsageStatement());
		assertNull(refinedQuery.getDoseFormUsageStatement());
	}

	@Test
	void misplacedDoseForm() {
		RawQuery rawQuery = new RawQuery("", "", "", "solution for injection", "");
		NewRefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.toSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertTrue(searchQuery.getSubstances().isEmpty());
		assertTrue(searchQuery.getActiveIngredientDosages().isEmpty());
		assertTrue(searchQuery.getDrugAmounts().isEmpty());
		assertTrue(searchQuery.getDoseForms().isEmpty());
		assertTrue(searchQuery.getDoseFormCharacteristics().isEmpty());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceUsageStatement());
		assertNotNull(refinedQuery.getDosageUsageStatement());
		assertEquals("", refinedQuery.getDosageUsageStatement().getUsedParts());
		assertNull(refinedQuery.getDoseFormUsageStatement());
	}

	@Test
	void misplacedDosage() {
		RawQuery rawQuery = new RawQuery("", "", "", "", "100 mg");
		NewRefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.toSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertTrue(searchQuery.getSubstances().isEmpty());
		assertTrue(searchQuery.getActiveIngredientDosages().isEmpty());
		assertTrue(searchQuery.getDrugAmounts().isEmpty());
		assertTrue(searchQuery.getDoseForms().isEmpty());
		assertTrue(searchQuery.getDoseFormCharacteristics().isEmpty());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceUsageStatement());
		assertNull(refinedQuery.getDosageUsageStatement());
		assertNotNull(refinedQuery.getDoseFormUsageStatement());
		assertEquals("100 mg", refinedQuery.getDoseFormUsageStatement().getUnusedParts());
	}

	@Test
	void complexExample() {
		RawQuery rawQuery = new RawQuery(
				"Dormicum 5mg/ml solution",
				"Bayer",
				"Midazolam",
				"3 ml",
				""
		);

		NewRefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.toSearchQuery();


		assertEqualsIgnoreOrder(List.of("Dormicum", "Bayer"), searchQuery.getProductNameKeywords());
		assertEqualsIgnoreOrder(
				List.of(TestFactory.Substances.MIDAZOLAM_HYDROCHLORIDE, TestFactory.Substances.MIDAZOLAM),
				searchQuery.getSubstances());
		assertEqualsIgnoreOrder(List.of(
				Dosage.of(5, "mg", 1, "ml"),
				Dosage.of(3, "ml")
		), searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(new Amount(new BigDecimal(3), "ml")), searchQuery.getDrugAmounts());
		assertEquals(List.of(), searchQuery.getDoseForms());
		assertEquals(List.of(TestFactory.DoseForms.Characteristics.SOLUTION), searchQuery.getDoseFormCharacteristics());

		assertUsedParts(List.of("5mg/ml"), refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertUsedParts(List.of("solution"), refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertUsedParts(List.of(), refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertUsedParts(List.of("Midazolam"), refinedQuery.getSubstanceUsageStatement());
		assertUsedParts(List.of("3 ml"), refinedQuery.getDosageUsageStatement());
		assertNull(refinedQuery.getDoseFormUsageStatement());
	}

	@Test
	void complexExample2() {
		RawQuery rawQuery = new RawQuery(
				"Prednisolon granules",
				"Prednisolut",
				"",
				"",
				""
		);

		NewRefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.toSearchQuery();

		assertEqualsIgnoreOrder(List.of("Prednisolon", "Prednisolut"), searchQuery.getProductNameKeywords());
		assertEqualsIgnoreOrder(
				List.of(TestFactory.Substances.PREDNISOLONE, TestFactory.Substances.PREDNISOLONE_HYDROGENSUCCINATE),
				searchQuery.getSubstances());
		assertEquals(List.of(), searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(), searchQuery.getDrugAmounts());
		assertEquals(List.of(TestFactory.DoseForms.GRANULES), searchQuery.getDoseForms());
		// Characteristics left out, because supplying the Granules characteristic may make sense, but
		// it could also be left out because we have the corresponding dose form.

		assertUsedParts(List.of(), refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertUsedParts(List.of("granules"), refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertUsedParts(List.of("Prednisolon"), refinedQuery.getSubstanceGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getSubstanceUsageStatement());
		assertNull(refinedQuery.getDosageUsageStatement());
		assertNull(refinedQuery.getDoseFormUsageStatement());
	}
}