package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Drug;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.TestFactory.DoseForms.*;
import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Markus Budeus
 */
public class DoseFormCharacteristicJudgeTest extends UnitTest {

	private DoseFormCharacteristicJudge sut;

	@BeforeEach
	void setUp() {
		sut = new DoseFormCharacteristicJudge();
	}

	@Test
	void noOverlap() {
		SearchQuery query = new SearchQuery.Builder().withDoseFormCharacteristics(
				List.of(Characteristics.GRANULES)).build();
		assertEquals(0, sut.judgeInternal(Products.Detailed.DORMICUM_15, query));
	}

	@Test
	void singleOverlap() {
		SearchQuery query = new SearchQuery.Builder().withDoseFormCharacteristics(
				List.of(Characteristics.SOLUTION)).build();
		assertEquals(DoseFormCharacteristicJudge.SCORE_PER_OVERLAP, sut.judgeInternal(Products.Detailed.DORMICUM_15, query));
	}

	@Test
	void partialOverlap() {
		SearchQuery query = new SearchQuery.Builder().withDoseFormCharacteristics(
				List.of(Characteristics.POWDER, Characteristics.PARENTERAL, Characteristics.ORAL)).build();
		assertEquals(2 * DoseFormCharacteristicJudge.SCORE_PER_OVERLAP,
				sut.judgeInternal(Products.Detailed.PREDNISOLUT, query));
	}

	@Test
	void multiOverlap() {
		SearchQuery query = new SearchQuery.Builder().withDoseFormCharacteristics(
				List.of(Characteristics.POWDER, Characteristics.PARENTERAL, Characteristics.CONVENTIONAL,
						Characteristics.GRANULES, Characteristics.ORAL)).build();
		assertEquals(6 * DoseFormCharacteristicJudge.SCORE_PER_OVERLAP,
				sut.judgeInternal(
						new DetailedProduct(17, "Something I made up", List.of(),
								List.of(
										new Drug("powder", POWDER_FOR_SOLUTION_FOR_INJECTION, null,
												List.of()),
										new Drug("granules", GRANULES, null, List.of())
								))
						, query));
	}

	@Test
	void noDrug() {
		SearchQuery query = new SearchQuery.Builder().withDoseFormCharacteristics(
				List.of(Characteristics.SOLUTION, Characteristics.CONVENTIONAL)).build();
		assertEquals(0, sut.judgeInternal(Products.Detailed.ASEPTODERM, query));
	}

	@Test
	void noDoseFormCharacteristicInSearchTerm() {
		SearchQuery query = new SearchQuery.Builder().withDoseFormCharacteristics(List.of()).build();
		assertEquals(0, sut.judgeInternal(Products.Detailed.PREDNISOLUT, query));
	}

	static Stream<Arguments> nonDetailedProducts() {
		return Stream.of(
				arguments(named("Substance", Substances.EPINEPHRINE)),
				arguments(named("Product", Products.DORMICUM_15))
		);
	}

}