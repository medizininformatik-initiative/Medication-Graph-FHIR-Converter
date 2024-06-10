package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class DosagesInProductNameJudgeTest extends UnitTest {

	private DosagesInProductNameJudge sut;

	@BeforeEach
	void setUp() {
		sut = new DosagesInProductNameJudge(null);
	}

	@Test
	void basicExample() {
		SearchQuery query = new SearchQuery.Builder()
				.withActiveIngredientDosages(List.of(Dosage.of(500, "mg")))
				.build();
		double judgement = sut.judgeInternal(new Product(0, "Aspirin 500mg"), query);
		assertEquals(DosagesInProductNameJudge.SCORE_PER_MATCH, judgement);
	}

	@Test
	void notAProduct() {
		SearchQuery query = new SearchQuery.Builder()
				.withActiveIngredientDosages(List.of(Dosage.of(500, "mg")))
				.build();
		double judgement = sut.judgeInternal(TestFactory.SAMPLE_SUBSTANCE_1, query);
		assertEquals(DosagesInProductNameJudge.NOT_A_PRODUCT_SCORE, judgement);
	}

	@Test
	void multipleMatches() {
		SearchQuery query = new SearchQuery.Builder()
				.withActiveIngredientDosages(List.of(
						Dosage.of(500, "mg"),
						Dosage.of(10, "mg", 1, "ml"),
						Dosage.of(50, "ml")
				))
				.build();
		double judgement = sut.judgeInternal(new Product(132, "Inventionium 10mg/ml 50ml"), query);
		assertEquals(2 * DosagesInProductNameJudge.SCORE_PER_MATCH, judgement);
	}

	@Test
	void dosagesInNameWhichAreNotSearched() {
		SearchQuery query = new SearchQuery.Builder()
				.withActiveIngredientDosages(List.of(
						Dosage.of(50, "ml")
				))
				.build();
		double judgement = sut.judgeInternal(new Product(132, "Inventionium 50mg"), query);
		assertEquals(0, judgement);
	}

	@Test
	void noDosagesSearched() {
		SearchQuery query = new SearchQuery.Builder()
				.build();
		double judgement = sut.judgeInternal(new Product(132, "Triptan 1000mg"), query);
		assertEquals(0, judgement);
	}

	@Test
	void searchedDrugAmountsNotApplied() {
		SearchQuery query = new SearchQuery.Builder()
				.withDrugAmounts(List.of(new Amount(new BigDecimal(500), "mg")))
				.build();
		double judgement = sut.judgeInternal(new Product(0, "Aspirin 500mg"), query);
		assertEquals(0, judgement);
	}

}