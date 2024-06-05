package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class QueryRefinerImplTest extends UnitTest {

	private QueryRefinerImpl sut;

	@BeforeEach
	void setUp() {
		sut = new QueryRefinerImpl(
				new DosageQueryRefiner(),
				new DoseFormQueryRefiner(EDQM_PROVIDER),
				new SubstanceQueryRefiner(SUBSTANCES_PROVIDER)
		);
	}

	@Test
	void simpleExample() {
		RawQuery rawQuery = new RawQuery("Midazolam solution for injection 1mg/ml 5ml");
		RefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.getSearchQuery();

		assertEquals(List.of("Midazolam"), searchQuery.getProductNameKeywords());
		assertEquals(List.of(
						Dosage.of(1, "mg", 1, "ml"),
						Dosage.of(5, "ml")
				),
				searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(
				new Amount(new BigDecimal(5), "ml")
		), searchQuery.getDrugAmounts());
		assertEquals(List.of(DoseForms.SOLUTION_FOR_INJECTION), searchQuery.getDoseForms());

		assertNotNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNotNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDosageUsageStatement());
		assertNull(refinedQuery.getDoseFormUsageStatement());
		assertEquals("solution for injection",
				refinedQuery.getDoseFormGeneralSearchTermUsageStatement().getUsedParts().trim());
		assertUsedParts(List.of("1mg/ml", "5ml"), refinedQuery.getDosageGeneralSearchTermUsageStatement());
	}

	@Test
	void queryDoseFormOnly() {
		RawQuery rawQuery = new RawQuery("", "", "", "", "Granules");
		RefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.getSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertTrue(searchQuery.getSubstances().isEmpty());
		assertTrue(searchQuery.getActiveIngredientDosages().isEmpty());
		assertTrue(searchQuery.getDrugAmounts().isEmpty());
		assertEquals(List.of(DoseForms.GRANULES), searchQuery.getDoseForms());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDosageUsageStatement());
		assertNotNull(refinedQuery.getDoseFormUsageStatement());
		assertEquals("Granules", refinedQuery.getDoseFormUsageStatement().getUsedParts());
	}

	@Test
	void queryDosageOnly() {
		RawQuery rawQuery = new RawQuery("", "", "", "100 ml", "");
		RefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.getSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertTrue(searchQuery.getSubstances().isEmpty());
		assertEquals(List.of(Dosage.of(100, "ml")), searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(new Amount(new BigDecimal(100), "ml")), searchQuery.getDrugAmounts());
		assertTrue(searchQuery.getDoseForms().isEmpty());
		assertTrue(searchQuery.getDoseFormCharacteristics().isEmpty());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNotNull(refinedQuery.getDosageUsageStatement());
		assertEquals("100 ml", refinedQuery.getDosageUsageStatement().getUsedParts());
		assertNull(refinedQuery.getDoseFormUsageStatement());
	}

	@Test
	void misplacedDoseForm() {
		RawQuery rawQuery = new RawQuery("", "", "", "solution for injection", "");
		RefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.getSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertTrue(searchQuery.getSubstances().isEmpty());
		assertTrue(searchQuery.getActiveIngredientDosages().isEmpty());
		assertTrue(searchQuery.getDrugAmounts().isEmpty());
		assertTrue(searchQuery.getDoseForms().isEmpty());
		assertTrue(searchQuery.getDoseFormCharacteristics().isEmpty());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNotNull(refinedQuery.getDosageUsageStatement());
		assertEquals("", refinedQuery.getDosageUsageStatement().getUsedParts());
		assertNull(refinedQuery.getDoseFormUsageStatement());
	}

	@Test
	void misplacedDosage() {
		RawQuery rawQuery = new RawQuery("", "", "", "", "100 mg");
		RefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.getSearchQuery();

		assertTrue(searchQuery.getProductNameKeywords().isEmpty());
		assertTrue(searchQuery.getSubstances().isEmpty());
		assertTrue(searchQuery.getActiveIngredientDosages().isEmpty());
		assertTrue(searchQuery.getDrugAmounts().isEmpty());
		assertTrue(searchQuery.getDoseForms().isEmpty());
		assertTrue(searchQuery.getDoseFormCharacteristics().isEmpty());

		assertNull(refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
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

		RefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.getSearchQuery();


		assertEqualsIgnoreOrder(List.of("Dormicum", "Bayer"), searchQuery.getProductNameKeywords());
		assertEqualsIgnoreOrder(List.of(Substances.MIDAZOLAM_HYDROCHLORIDE, Substances.MIDAZOLAM), searchQuery.getSubstances());
		assertEqualsIgnoreOrder(List.of(
				Dosage.of(5, "mg", 1, "ml"),
				Dosage.of(3, "ml")
		), searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(new Amount(new BigDecimal(3), "ml")), searchQuery.getDrugAmounts());
		assertEquals(List.of(), searchQuery.getDoseForms());
		assertEquals(List.of(DoseForms.Characteristics.SOLUTION), searchQuery.getDoseFormCharacteristics());

		assertUsedParts(List.of("5mg/ml"), refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertUsedParts(List.of("solution"), refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
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

		RefinedQuery refinedQuery = sut.refine(rawQuery);
		SearchQuery searchQuery = refinedQuery.getSearchQuery();

		assertEqualsIgnoreOrder(List.of("Prednisolon", "Prednisolut"), searchQuery.getProductNameKeywords());
		assertEqualsIgnoreOrder(List.of(Substances.PREDNISOLONE, Substances.PREDNISOLONE_HYDROGENSUCCINATE), searchQuery.getSubstances());
		assertEquals(List.of(), searchQuery.getActiveIngredientDosages());
		assertEquals(List.of(), searchQuery.getDrugAmounts());
		assertEquals(List.of(DoseForms.GRANULES), searchQuery.getDoseForms());
		// Characteristics left out, because supplying the Granules characteristic may make sense, but
		// it could also be left out because we have the corresponding dose form.

		assertUsedParts(List.of(), refinedQuery.getDosageGeneralSearchTermUsageStatement());
		assertUsedParts(List.of("granules"), refinedQuery.getDoseFormGeneralSearchTermUsageStatement());
		assertNull(refinedQuery.getDosageUsageStatement());
		assertNull(refinedQuery.getDoseFormUsageStatement());
	}

}