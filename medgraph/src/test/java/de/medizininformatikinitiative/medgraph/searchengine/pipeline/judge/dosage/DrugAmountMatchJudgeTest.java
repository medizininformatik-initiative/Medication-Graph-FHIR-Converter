package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.db.DbAmount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DrugAmountMatchJudge.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class DrugAmountMatchJudgeTest {

	private DrugAmountMatchJudge sut;

	@BeforeEach
	void setUp() {
		sut = new DrugAmountMatchJudge();
	}

	@Test
	public void nothing() {
		assertEquals(NO_AMOUNTS_SCORE, sut.judge(List.of(), List.of()));
	}

	@Test
	public void noTargetAmounts() {
		List<DbAmount> drugAmounts = List.of(new DbAmount(new BigDecimal(5), "ml"));
		assertEquals(NO_AMOUNTS_SCORE, sut.judge(drugAmounts, List.of()));
	}

	@Test
	public void noDrugAmountsToMatch() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal(5), "ml"));
		assertEquals(0, sut.judge(List.of(), targetAmounts));
	}

	@Test
	public void simpleAmountMatch() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal(5), "ml"));
		List<DbAmount> drugAmounts = List.of(new DbAmount(new BigDecimal(5), "ml"));

		assertEquals(MATCH_SCORE, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void simpleMismatch() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal(10), "ml"));
		List<DbAmount> drugAmounts = List.of(new DbAmount(new BigDecimal(5), "ml"));

		assertEquals(0, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void unitlessMatch() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal(5), null));
		List<DbAmount> drugAmounts = List.of(new DbAmount(new BigDecimal(5), "ml"));

		assertEquals(UNITLESS_MATCH_SCORE, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void unitlessPerfectMatch() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal("2.8"), null));
		List<DbAmount> drugAmounts = List.of(new DbAmount(new BigDecimal("2.8"), null));

		assertEquals(MATCH_SCORE, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void unitMismatch() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal(10), "ml"));
		List<DbAmount> drugAmounts = List.of(new DbAmount(new BigDecimal(10), "g"));

		assertEquals(0, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void nullUnitMismatch() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal(8), "ml"));
		List<DbAmount> drugAmounts = List.of(new DbAmount(new BigDecimal(8), null));

		assertEquals(0, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void matchesOneOfMultiple() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal("7.2"), "ml"));
		List<DbAmount> drugAmounts = List.of(
				new DbAmount(new BigDecimal(8), null),
				new DbAmount(new BigDecimal("7.2"), "mg"),
				new DbAmount(new BigDecimal("7.2"), "ml")
		);

		assertEquals(MATCH_SCORE, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void matchesUnitlessOfMultiple() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal("7.2"), null));
		List<DbAmount> drugAmounts = List.of(
				new DbAmount(new BigDecimal(8), null),
				new DbAmount(new BigDecimal("7.2"), "mg"),
				new DbAmount(new BigDecimal("7.2"), "ml")
		);

		assertEquals(UNITLESS_MATCH_SCORE, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void matchesNoneOfMultiple() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal("7.5"), "ml"));
		List<DbAmount> drugAmounts = List.of(
				new DbAmount(new BigDecimal(8), null),
				new DbAmount(new BigDecimal("7.5"), "mg"),
				new DbAmount(new BigDecimal(10), "ml")
		);

		assertEquals(0, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void selectsBestMatch() {
		List<Amount> targetAmounts = List.of(new Amount(new BigDecimal("20"), null));
		List<DbAmount> drugAmounts = List.of(
				new DbAmount(new BigDecimal(20), "mg"), // Unitless match
				new DbAmount(new BigDecimal(8), null),
				new DbAmount(new BigDecimal(20), null) // Full match
		);

		assertEquals(MATCH_SCORE, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	public void multipleTargetScoresSumUp() {
		List<Amount> targetAmounts = List.of(
				new Amount(new BigDecimal("5"), null),
				new Amount(new BigDecimal("20"), "mg"),
				new Amount(new BigDecimal("20"), null)
		);
		List<DbAmount> drugAmounts = List.of(
				new DbAmount(new BigDecimal(5), "mg"),
				new DbAmount(new BigDecimal(20), "mg"), // Unitless match
				new DbAmount(new BigDecimal(8), null),
				new DbAmount(new BigDecimal(20), null) // Full match
		);

		assertEquals(UNITLESS_MATCH_SCORE + MATCH_SCORE + MATCH_SCORE, sut.judge(drugAmounts, targetAmounts));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void whoeverAssignedTheScoresStillHasNotLostHisSanity() {
		assertTrue(MATCH_SCORE >= UNITLESS_MATCH_SCORE);
	}

}