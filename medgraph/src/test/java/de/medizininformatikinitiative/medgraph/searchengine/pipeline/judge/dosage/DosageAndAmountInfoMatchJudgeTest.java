package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosageAndAmountInfoMatchJudge.NO_DETAILED_PRODUCT_SCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Markus Budeus
 */
public class DosageAndAmountInfoMatchJudgeTest extends UnitTest {

	@Mock
	private DosageMatchJudge dosageMatchJudge;
	@Mock
	private DrugAmountMatchJudge drugAmountMatchJudge;

	private DosageAndAmountInfoMatchJudge sut;

	@BeforeEach
	void setUp() {
		sut = new DosageAndAmountInfoMatchJudge(1.0, dosageMatchJudge, drugAmountMatchJudge);
	}

	@Test
	public void judgeANonProduct() {
		SearchQuery query = new SearchQuery.Builder()
				.withSubstances(List.of(SAMPLE_SUBSTANCE_2))
				.withActiveIngredientDosages(List.of(Dosage.of(400, "mg")))
				.withDrugAmounts(List.of(new Amount(BigDecimal.ONE, null)))
				.build();

		assertEquals(NO_DETAILED_PRODUCT_SCORE, sut.judge(SAMPLE_SUBSTANCE_2, query).getScore());
	}

	@Test
	public void judgeANonDetailedProduct() {
		SearchQuery query = new SearchQuery.Builder()
				.withSubstances(List.of(SAMPLE_SUBSTANCE_2))
				.withActiveIngredientDosages(List.of(Dosage.of(400, "mg")))
				.withDrugAmounts(List.of(new Amount(BigDecimal.ONE, null)))
				.build();

		assertEquals(NO_DETAILED_PRODUCT_SCORE, sut.judge(SAMPLE_PRODUCT_3, query).getScore());
	}

	@Test
	public void correctValuesPassedToDosageMatchJudge() {
		List<Dosage> queryDosages = List.of(Dosage.of(10, "mg"));

		SearchQuery query = new SearchQuery.Builder()
				.withSubstances(List.of(SAMPLE_SUBSTANCE_1))
				.withActiveIngredientDosages(queryDosages)
				.build();

		List<Drug> drugs = List.of(
				buildDrug(
						SAMPLE_AMOUNT_1,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_4))
				)
		);
		DetailedProduct detailedProduct = buildDetailedProduct(SAMPLE_PRODUCT_1.getId(), drugs);

		sut.judge(detailedProduct, query);

		verify(dosageMatchJudge).judge(eq(drugs), eq(queryDosages));
	}

	@Test
	public void correctValuesPassedToAmountJudge() {
		List<Amount> queryAmounts = List.of(new Amount(BigDecimal.ONE, "ml"));

		SearchQuery query = new SearchQuery.Builder()
				.withSubstances(List.of(SAMPLE_SUBSTANCE_1))
				.withDrugAmounts(queryAmounts)
				.build();

		List<Drug> drugs = List.of(
				buildDrug(
						SAMPLE_AMOUNT_1,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_4))
				),
				buildDrug(
						SAMPLE_AMOUNT_2,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_4), new ActiveIngredient("B", SAMPLE_AMOUNT_5))
				)
		);
		DetailedProduct detailedProduct = buildDetailedProduct(SAMPLE_PRODUCT_1.getId(), drugs);

		sut.judge(detailedProduct, query);

		verify(drugAmountMatchJudge).judge(eq(List.of(SAMPLE_AMOUNT_1, SAMPLE_AMOUNT_2)), eq(queryAmounts));
	}

	@Test
	public void judgeAProduct() {
		List<Dosage> queryDosages = List.of(Dosage.of(10, "mg"));
		List<Amount> queryAmounts = List.of(new Amount(BigDecimal.ONE, "ml"));

		SearchQuery query = new SearchQuery.Builder()
				.withSubstances(List.of(SAMPLE_SUBSTANCE_1))
				.withActiveIngredientDosages(queryDosages)
				.withDrugAmounts(queryAmounts)
				.build();

		List<Drug> drugs = List.of(
				buildDrug(
						SAMPLE_AMOUNT_1,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_4))
				)
		);
		DetailedProduct detailedProduct = buildDetailedProduct(SAMPLE_PRODUCT_1.getId(), drugs);

		when(dosageMatchJudge.judge(drugs, queryDosages)).thenReturn(1.7);
		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_AMOUNT_1)),
				eq(queryAmounts))).thenReturn(2.2);


		assertEquals(3.9, sut.judge(detailedProduct, query).getScore(), 0.01);
	}

	@Test
	public void batchJudgeProducts() {
		List<Dosage> queryDosages = List.of(Dosage.of(10, "mg"));
		List<Amount> queryAmounts = List.of(new Amount(BigDecimal.ONE, "ml"));

		SearchQuery query = new SearchQuery.Builder()
				.withSubstances(List.of(SAMPLE_SUBSTANCE_1))
				.withActiveIngredientDosages(queryDosages)
				.withDrugAmounts(queryAmounts)
				.build();

		List<Drug> drugs1 = List.of(
				buildDrug(
						SAMPLE_AMOUNT_1,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_4))
				),
				buildDrug(
						SAMPLE_AMOUNT_2,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_5),
								new ActiveIngredient("B", SAMPLE_AMOUNT_4))
				)
		);
		List<Drug> drugs2 = List.of(
				buildDrug(
						SAMPLE_AMOUNT_3,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_RANGE))
				)
		);
		List<Drug> drugs3 = List.of(
				buildDrug(
						SAMPLE_AMOUNT_1,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_RANGE),
								new ActiveIngredient("B", SAMPLE_AMOUNT_5))
				)
		);
		List<Drug> unrelatedDosageInfo = List.of(
				buildDrug(
						SAMPLE_AMOUNT_2,
						List.of(new ActiveIngredient("A", SAMPLE_AMOUNT_5))
				)
		);
		DetailedProduct detailedProduct1 = buildDetailedProduct(SAMPLE_PRODUCT_1.getId(), drugs1);
		DetailedProduct detailedProduct2 = buildDetailedProduct(SAMPLE_PRODUCT_2.getId(), drugs2);
		DetailedProduct detailedProduct3 = buildDetailedProduct(SAMPLE_PRODUCT_3.getId(), drugs3);

		when(dosageMatchJudge.judge(eq(drugs1), eq(queryDosages))).thenReturn(0.8);
		when(dosageMatchJudge.judge(eq(drugs2), eq(queryDosages))).thenReturn(1.2);
		when(dosageMatchJudge.judge(eq(drugs3), eq(queryDosages))).thenReturn(0.0);
		when(dosageMatchJudge.judge(eq(unrelatedDosageInfo), eq(queryDosages))).thenReturn(5.0);

		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_AMOUNT_1, SAMPLE_AMOUNT_2)),
				eq(queryAmounts))).thenReturn(0.5);
		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_AMOUNT_3)), eq(queryAmounts))).thenReturn(0.6);
		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_AMOUNT_1)), eq(queryAmounts))).thenReturn(0.0);
		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_AMOUNT_2)), eq(queryAmounts))).thenReturn(5.0);

		List<ScoredJudgement> judgements = sut.batchJudge(
				List.of(detailedProduct1, new Product(1685454L, "Unknown"), detailedProduct2, detailedProduct3),
				query);

		assertEquals(1.3, judgements.get(0).getScore(), 0.01);
		assertEquals(NO_DETAILED_PRODUCT_SCORE, judgements.get(1).getScore());
		assertEquals(1.8, judgements.get(2).getScore(), 0.01);
		assertEquals(0, judgements.get(3).getScore());
	}

}