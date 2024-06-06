package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Drug;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Markus Budeus
 */
public class PharmaceuticalDoseFormJudgeTest extends UnitTest {

	private PharmaceuticalDoseFormJudge sut;

	@BeforeEach
	void setUp() {
		sut = new PharmaceuticalDoseFormJudge(1.0);
	}

	@ParameterizedTest
	@MethodSource("nonDetailedProducts")
	void notADetailedProduct(Matchable matchable) {
		assertEquals(PharmaceuticalDoseFormJudge.NOT_A_DETAILED_PRODUCT_SCORE,
				sut.judgeInternal(matchable, SAMPLE_SEARCH_QUERY));
	}

	@Test
	void noOverlap() {
		SearchQuery query = new SearchQuery.Builder().withDoseForms(
				List.of(DoseForms.GRANULES, DoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION)).build();
		assertEquals(0, sut.judgeInternal(Products.Detailed.DORMICUM_15, query));
	}

	@Test
	void singleOverlap() {
		SearchQuery query = new SearchQuery.Builder().withDoseForms(
				List.of(DoseForms.SOLUTION_FOR_INJECTION_OR_INFUSION, DoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION)).build();
		assertEquals(PharmaceuticalDoseFormJudge.SCORE_PER_OVERLAP, sut.judgeInternal(Products.Detailed.DORMICUM_15, query));
	}

	@Test
	void partialOverlap() {
		SearchQuery query = new SearchQuery.Builder().withDoseForms(
				List.of(DoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION, DoseForms.GRANULES)).build();
		assertEquals(PharmaceuticalDoseFormJudge.SCORE_PER_OVERLAP,
				sut.judgeInternal(Products.Detailed.PREDNISOLUT, query));
	}

	@Test
	void multiOverlap() {
		SearchQuery query = new SearchQuery.Builder().withDoseForms(
				List.of(DoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION, DoseForms.GRANULES,
						DoseForms.SOLUTION_FOR_INJECTION_OR_INFUSION)).build();
		assertEquals(2 * PharmaceuticalDoseFormJudge.SCORE_PER_OVERLAP,
				sut.judgeInternal(
						new DetailedProduct(17, "Something I made up", List.of(),
								List.of(
										new Drug("powder", DoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION, null,
												List.of()),
										new Drug("granules", DoseForms.GRANULES, null, List.of())
								))
						, query));
	}

	@Test
	void noDrug() {
		SearchQuery query = new SearchQuery.Builder().withDoseForms(
				List.of(DoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION)).build();
		assertEquals(0, sut.judgeInternal(Products.Detailed.ASEPTODERM, query));
	}

	@Test
	void noDoseFormInSearchTerm() {
		SearchQuery query = new SearchQuery.Builder().withDoseForms(List.of()).build();
		assertEquals(0, sut.judgeInternal(Products.Detailed.PREDNISOLUT, query));
	}

	static Stream<Arguments> nonDetailedProducts() {
		return Stream.of(
				arguments(named("Substance", Substances.EPINEPHRINE)),
				arguments(named("Product", Products.DORMICUM_15))
		);
	}

}