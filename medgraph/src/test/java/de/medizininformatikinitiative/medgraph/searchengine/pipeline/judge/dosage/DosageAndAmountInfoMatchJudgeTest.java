package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.RawAmount;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.*;
import static de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosageAndAmountInfoMatchJudge.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Markus Budeus
 */
public class DosageAndAmountInfoMatchJudgeTest extends UnitTest {

	@Mock
	private Database database;

	private DosageAndAmountInfoMatchJudge sut;

	@BeforeEach
	void setUp() {
		sut = new DosageAndAmountInfoMatchJudge(database, 1.0);
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = { false, true })
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
	@ValueSource(booleans = { false, true })
	public void judgeANonProduct(boolean batchMode) {
		when(database.getDrugDosagesByProduct(any())).thenThrow(
				new IllegalArgumentException("No database access should happen!"));

		SearchQuery query = new SearchQuery(null, "Aspirin",
				List.of(Dosage.of(400, "mg")),
				List.of(new RawAmount(BigDecimal.ONE)));

		if (batchMode) {
			List<ScoredJudgement> judgements = sut.batchJudge(List.of(SAMPLE_SUBSTANCE_1, SAMPLE_SUBSTANCE_2), query);
			for (ScoredJudgement judgement : judgements) {
				assertEquals(NO_PRODUCT_SCORE, judgement.getScore());
			}
		} else {
			assertEquals(NO_PRODUCT_SCORE, sut.judge(SAMPLE_SUBSTANCE_2, query).getScore());
		}
	}

}