package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDosagesByProduct;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDrugDosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.*;
import static de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosageAndAmountInfoMatchJudge.NO_DOSAGE_AND_AMOUNT_SCORE;
import static de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosageAndAmountInfoMatchJudge.NO_PRODUCT_SCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Markus Budeus
 */
public class DosageAndAmountInfoMatchJudgeTest extends UnitTest {

	@Mock
	private Database database;
	@Mock
	private DosageMatchJudge dosageMatchJudge;
	@Mock
	private DrugAmountMatchJudge drugAmountMatchJudge;

	private DosageAndAmountInfoMatchJudge sut;

	@BeforeEach
	void setUp() {
		sut = new DosageAndAmountInfoMatchJudge(database, 1.0, dosageMatchJudge, drugAmountMatchJudge);
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void noDatabaseAccessRequiredSingle(boolean batchMode) {
		when(database.getDrugDosagesByProduct(any())).thenThrow(
				new IllegalArgumentException("No database access should happen!"));

		// No active ingredient dosages or drug amounts in query!
		SearchQuery query = new SearchQuery(null, "Aspirin", Collections.emptyList(), Collections.emptyList());

		if (batchMode) {
			List<ScoredJudgement> judgements = sut.batchJudge(List.of(SAMPLE_PRODUCT_1, SAMPLE_PRODUCT_2), query);
			for (ScoredJudgement judgement : judgements) {
				assertEquals(NO_DOSAGE_AND_AMOUNT_SCORE, judgement.getScore());
			}
		} else {
			assertEquals(NO_DOSAGE_AND_AMOUNT_SCORE, sut.judge(SAMPLE_PRODUCT_1, query).getScore());
		}
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void judgeANonProduct(boolean batchMode) {
		when(database.getDrugDosagesByProduct(any())).thenThrow(
				new IllegalArgumentException("No database access should happen!"));

		SearchQuery query = new SearchQuery(null, "Aspirin",
				List.of(Dosage.of(400, "mg")),
				List.of(new Amount(BigDecimal.ONE, null)));

		if (batchMode) {
			List<ScoredJudgement> judgements = sut.batchJudge(List.of(SAMPLE_SUBSTANCE_1, SAMPLE_SUBSTANCE_2), query);
			for (ScoredJudgement judgement : judgements) {
				assertEquals(NO_PRODUCT_SCORE, judgement.getScore());
			}
		} else {
			assertEquals(NO_PRODUCT_SCORE, sut.judge(SAMPLE_SUBSTANCE_2, query).getScore());
		}
	}

	@Test
	public void correctValuesPassedToDosageMatchJudge() {
		List<Dosage> queryDosages = List.of(Dosage.of(10, "mg"));
		SearchQuery query = new SearchQuery("", "Aspirin", queryDosages, Collections.emptyList());

		List<DbDrugDosage> drugDosages = List.of(
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_1,
						List.of(SAMPLE_DB_DOSAGE_1)
				)
		);
		DbDosagesByProduct resultDosageInfo = new DbDosagesByProduct(SAMPLE_PRODUCT_1.getId(), drugDosages);
		when(database.getDrugDosagesByProduct(any())).thenReturn(Set.of(resultDosageInfo));

		sut.judge(SAMPLE_PRODUCT_1, query);

		verify(dosageMatchJudge).judge(eq(drugDosages), eq(queryDosages));
	}

	@Test
	public void correctValuesPassedToAmountJudge() {
		List<Amount> queryAmounts = List.of(new Amount(BigDecimal.ONE, "ml"));
		SearchQuery query = new SearchQuery("", "Aspirin", Collections.emptyList(), queryAmounts);

		List<DbDrugDosage> drugDosages = List.of(
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_1,
						List.of(SAMPLE_DB_DOSAGE_1)
				),
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_2,
						List.of(SAMPLE_DB_DOSAGE_1, SAMPLE_DB_DOSAGE_3)
				)
		);
		DbDosagesByProduct resultDosageInfo = new DbDosagesByProduct(SAMPLE_PRODUCT_1.getId(), drugDosages);
		when(database.getDrugDosagesByProduct(any())).thenReturn(Set.of(resultDosageInfo));

		sut.judge(SAMPLE_PRODUCT_1, query);

		verify(drugAmountMatchJudge).judge(eq(List.of(SAMPLE_DB_AMOUNT_1, SAMPLE_DB_AMOUNT_2)), eq(queryAmounts));
	}

	@Test
	public void judgeAProduct() {
		List<Dosage> queryDosages = List.of(Dosage.of(10, "mg"));
		List<Amount> queryAmounts = List.of(new Amount(BigDecimal.ONE, "ml"));
		SearchQuery query = new SearchQuery("", "Aspirin", queryDosages, queryAmounts);

		List<DbDrugDosage> drugDosages = List.of(
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_1,
						List.of(SAMPLE_DB_DOSAGE_1)
				)
		);
		DbDosagesByProduct resultDosageInfo = new DbDosagesByProduct(SAMPLE_PRODUCT_1.getId(), drugDosages);

		when(database.getDrugDosagesByProduct(any())).thenReturn(Set.of(resultDosageInfo));
		when(dosageMatchJudge.judge(drugDosages, queryDosages)).thenReturn(1.7);
		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_DB_AMOUNT_1)),
				eq(queryAmounts))).thenReturn(2.2);


		assertEquals(3.9, sut.judge(SAMPLE_PRODUCT_1, query).getScore(), 0.01);
	}

	@Test
	public void dosageInformationUnavailable() {
		when(database.getDrugDosagesByProduct(any())).thenReturn(Set.of());

		assertEquals(0, sut.judge(SAMPLE_PRODUCT_1, SAMPLE_SEARCH_QUERY).getScore());
	}

	@Test
	public void databaseReportsDataForWrongProduct() {
		List<Dosage> queryDosages = List.of(Dosage.of(10, "mg"));
		List<Amount> queryAmounts = List.of(new Amount(BigDecimal.ONE, "ml"));
		SearchQuery query = new SearchQuery("", "Aspirin", queryDosages, queryAmounts);

		List<DbDrugDosage> drugDosages1 = List.of(
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_1,
						List.of(SAMPLE_DB_DOSAGE_1)
				)
		);

		// Database reports for sample 1, but the SUT requests data for sample 2!
		when(database.getDrugDosagesByProduct(any())).thenReturn(
				Set.of(new DbDosagesByProduct(SAMPLE_PRODUCT_1.getId(), drugDosages1)));

		when(dosageMatchJudge.judge(any(), any())).thenReturn(1.0);
		when(drugAmountMatchJudge.judge(any(), any())).thenReturn(1.0);

		assertEquals(0, sut.judge(SAMPLE_PRODUCT_2, query).getScore());
	}

	@Test
	public void batchJudgeProducts() {
		List<Dosage> queryDosages = List.of(Dosage.of(10, "mg"));
		List<Amount> queryAmounts = List.of(new Amount(BigDecimal.ONE, "ml"));
		SearchQuery query = new SearchQuery("", "Aspirin", queryDosages, queryAmounts);

		List<DbDrugDosage> drugDosages1 = List.of(
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_1,
						List.of(SAMPLE_DB_DOSAGE_1)
				),
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_2,
						List.of(SAMPLE_DB_DOSAGE_3, SAMPLE_DB_DOSAGE_1)
				)
		);
		List<DbDrugDosage> drugDosages2 = List.of(
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_3,
						List.of(SAMPLE_DB_DOSAGE_2)
				)
		);
		List<DbDrugDosage> drugDosages3 = List.of(
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_1,
						List.of(SAMPLE_DB_DOSAGE_2, SAMPLE_DB_DOSAGE_3)
				)
		);
		List<DbDrugDosage> unrelatedDosageInfo = List.of(
				new DbDrugDosage(
						SAMPLE_DB_AMOUNT_2,
						List.of(SAMPLE_DB_DOSAGE_3)
				)
		);
		DbDosagesByProduct resultDosageInfo1 = new DbDosagesByProduct(SAMPLE_PRODUCT_1.getId(), drugDosages1);
		DbDosagesByProduct resultDosageInfo2 = new DbDosagesByProduct(SAMPLE_PRODUCT_2.getId(), drugDosages2);
		DbDosagesByProduct resultDosageInfo3 = new DbDosagesByProduct(SAMPLE_PRODUCT_3.getId(), drugDosages3);
		DbDosagesByProduct resultDosageInfo4 = new DbDosagesByProduct(65419845L, drugDosages3);

		when(database.getDrugDosagesByProduct(any())).thenReturn(
				Set.of(resultDosageInfo1, resultDosageInfo2, resultDosageInfo3, resultDosageInfo4));

		when(dosageMatchJudge.judge(eq(drugDosages1), eq(queryDosages))).thenReturn(0.8);
		when(dosageMatchJudge.judge(eq(drugDosages2), eq(queryDosages))).thenReturn(1.2);
		when(dosageMatchJudge.judge(eq(drugDosages3), eq(queryDosages))).thenReturn(0.0);
		when(dosageMatchJudge.judge(eq(unrelatedDosageInfo), eq(queryDosages))).thenReturn(5.0);

		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_DB_AMOUNT_1, SAMPLE_DB_AMOUNT_2)),
				eq(queryAmounts))).thenReturn(0.5);
		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_DB_AMOUNT_3)), eq(queryAmounts))).thenReturn(0.6);
		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_DB_AMOUNT_1)), eq(queryAmounts))).thenReturn(0.0);
		when(drugAmountMatchJudge.judge(eq(List.of(SAMPLE_DB_AMOUNT_2)), eq(queryAmounts))).thenReturn(5.0);

		List<ScoredJudgement> judgements = sut.batchJudge(
				List.of(SAMPLE_PRODUCT_1, new Product(1685454L, "Unknown"), SAMPLE_PRODUCT_2, SAMPLE_PRODUCT_3),
				query);

		assertEquals(1.3, judgements.get(0).getScore(), 0.01);
		assertEquals(0, judgements.get(1).getScore());
		assertEquals(1.8, judgements.get(2).getScore(), 0.01);
		assertEquals(0, judgements.get(3).getScore());
	}

}