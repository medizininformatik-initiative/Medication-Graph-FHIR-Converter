package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosageMatchJudge.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class DosageMatchJudgeTest {

	private static DosageMatchJudge sut;

	/**
	 * 1325.94 milligrams of prodrug, respective 1000 mg of active ingredient.
	 */
	private static final List<Drug> METHYLPREDNISOLUTE_1000 = List.of(
			new Drug(null, null, new Amount(BigDecimal.ONE, null), List.of(
					new ActiveIngredient("A", new Amount(new BigDecimal("1325.94"), "mg")),
					new ActiveIngredient("A", new Amount(new BigDecimal(1000), "mg"))
			))
	);

	/**
	 * 0.5 milligrams of prodrug, respective 0.423 mg of active ingredient in 1 ml fluid.
	 */
	private static final List<Drug> BERBERIL_EYE_DROPS = List.of(
			new Drug(null, null, new Amount(BigDecimal.ONE, "ml"), List.of(
					new ActiveIngredient("A", new Amount(new BigDecimal("0.5"), "mg")),
					new ActiveIngredient("A", new Amount(new BigDecimal("0.423"), "mg"))
			))
	);

	/**
	 * 500 milligrams of active ingredient in 5ml fluid.
	 */
	private static final List<Drug> TRANEXAMIC_ACID = List.of(
			new Drug(null, null, new Amount(new BigDecimal(5), "ml"), List.of(
					new ActiveIngredient("A", new Amount(new BigDecimal("500"), "mg"))
			))
	);

	/**
	 * 450-550 milligrams of active ingredient in 5ml fluid.
	 */
	private static final List<Drug> INACCURATE_DOSAGE = List.of(
			new Drug(null, null, new Amount(new BigDecimal(5), "ml"), List.of(
					new ActiveIngredient("A", new AmountRange(new BigDecimal(450), new BigDecimal(550), "mg"))
			))
	);

	/**
	 * 500 milligrams of active ingredient in 1 tablet (no amount unit).
	 */
	private static final List<Drug> ASPIRIN_500_TABLET = List.of(
			new Drug(null, null, new Amount(BigDecimal.ONE, null), List.of(
					new ActiveIngredient("A", new Amount(new BigDecimal("500"), "mg"))
			))
	);

	/**
	 * 100 milligrams of active ingredient with no specified drug amount.
	 */
	private static final List<Drug> GENERIC_MEDICATION = List.of(
			new Drug(null, null, null, List.of(
					new ActiveIngredient("A", new Amount(new BigDecimal("100"), "mg"))
			))
	);


	@BeforeAll
	public static void setupAll() {
		sut = new DosageMatchJudge();
	}

	@Test
	public void testWithoutTargetDosage() {
		assertEquals(DOSAGELESS_SCORE, sut.judge(METHYLPREDNISOLUTE_1000, List.of()));
	}

	@Test
	public void testWithoutTargetSubstanceData() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal(1000), "mg"), null, null)
		);
		assertEquals(0, sut.judge(List.of(), targetDosages));
	}

	@Test
	public void testWithNothing() {
		assertEquals(DOSAGELESS_SCORE, sut.judge(List.of(), List.of()));
	}

	@Test
	public void testAbsolute() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal(1000), "mg"), null, null)
		);
		assertEquals(ABSOLUTE_MATCH_SCORE, sut.judge(METHYLPREDNISOLUTE_1000, targetDosages));
	}

	@Test
	public void testAbsoluteWithAmountOff() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal(900), "mg"), null, null)
		);
		assertEquals(0, sut.judge(METHYLPREDNISOLUTE_1000, targetDosages));
	}

	@Test
	public void testAbsoluteWithoutTargetUnit() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal(1000), null), null, null)
		);
		// Even without unit on the search, this is considered to be a match
		assertEquals(ABSOLUTE_MATCH_SCORE, sut.judge(METHYLPREDNISOLUTE_1000, targetDosages));
	}

	@Test
	public void testAbsoluteWithWrongUnit() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal(1000), "ml"), null, null)
		);
		assertEquals(0, sut.judge(METHYLPREDNISOLUTE_1000, targetDosages));
	}

	@Test
	public void testPerfectRelative() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "ml"))
		);
		assertEquals(PERFECT_RELATIVE_MATCH_SCORE, sut.judge(BERBERIL_EYE_DROPS, targetDosages));
	}

	@Test
	public void testRelativeWithWrongDenominator() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.TEN, "ml"))
		);
		assertEquals(0, sut.judge(BERBERIL_EYE_DROPS, targetDosages));
	}

	@Test
	public void testRelativeWithWrongNominatorUnit() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "ug"), null, new Amount(BigDecimal.ONE, "ml"))
		);
		assertEquals(0, sut.judge(BERBERIL_EYE_DROPS, targetDosages));
	}

	@Test
	public void testRelativeWithWrongDenominatorUnit() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "mg"))
		);
		assertEquals(0, sut.judge(BERBERIL_EYE_DROPS, targetDosages));
	}


	@Test
	public void testRelativeWithMissingDenominatorUnit() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, null))
		);
		assertEquals(0, sut.judge(BERBERIL_EYE_DROPS, targetDosages));
	}

	@Test
	public void testPerfectRelative2() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("500"), "mg"), null, new Amount(new BigDecimal(5), "ml"))
		);
		assertEquals(PERFECT_RELATIVE_MATCH_SCORE, sut.judge(TRANEXAMIC_ACID, targetDosages));
	}

	@Test
	public void testNormalizedRelative() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("100"), "mg"), null, new Amount(BigDecimal.ONE, "ml"))
		);
		assertEquals(NORMALIZED_RELATIVE_MATCH_SCORE, sut.judge(TRANEXAMIC_ACID, targetDosages));
	}

	@Test
	public void matchesDrugAmount() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("5"), "ml"), null, null)
		);
		assertEquals(0, sut.judge(TRANEXAMIC_ACID, targetDosages));
	}

	@Test
	public void correctDosageButWrongAmount() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("500"), "mg"), null, new Amount(new BigDecimal(10), "ml"))
		);
		assertEquals(0, sut.judge(TRANEXAMIC_ACID, targetDosages));
	}

	@Test
	public void matchAbsoluteInaccurateDosage() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("475"), "mg"), null, null)
		);
		assertEquals(ABSOLUTE_MATCH_SCORE, sut.judge(INACCURATE_DOSAGE, targetDosages));
	}

	@Test
	public void perfectlyMatchInaccurateDosage() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("525"), "mg"), null, new Amount(new BigDecimal("5"), "ml"))
		);
		assertEquals(PERFECT_RELATIVE_MATCH_SCORE, sut.judge(INACCURATE_DOSAGE, targetDosages));
	}

	@Test
	public void relativelyMatchInaccurateDosage() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("95"), "mg"), null, new Amount(new BigDecimal("1"), "ml"))
		);
		assertEquals(NORMALIZED_RELATIVE_MATCH_SCORE, sut.judge(INACCURATE_DOSAGE, targetDosages));
	}

	@Test
	public void borderMatchInaccurateDosage() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("550"), "mg"), null, null)
		);
		assertEquals(ABSOLUTE_MATCH_SCORE, sut.judge(INACCURATE_DOSAGE, targetDosages));
	}

	@Test
	public void mismatchInaccurateDosage2() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("560"), "mg"), null, null)
		);
		assertEquals(0, sut.judge(INACCURATE_DOSAGE, targetDosages));
	}

	@Test
	public void mismatchInaccurateDosage() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("430"), "mg"), null, null)
		);
		assertEquals(0, sut.judge(INACCURATE_DOSAGE, targetDosages));
	}

	@Test
	public void allMatch() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "ml")),
				new Dosage(new Amount(new BigDecimal("0.423"), "mg"), null, null)
		);

		assertEquals(PERFECT_RELATIVE_MATCH_SCORE + ABSOLUTE_MATCH_SCORE, sut.judge(BERBERIL_EYE_DROPS, targetDosages));
	}

	@Test
	public void someMatch() {

		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "ml")),
				new Dosage(new Amount(new BigDecimal("0.8"), "mg"), null, null)
		);

		assertEquals(PERFECT_RELATIVE_MATCH_SCORE, sut.judge(BERBERIL_EYE_DROPS, targetDosages));
	}

	@Test
	public void drugDosageWithoutAmountUnit() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("500"), "mg"), null, new Amount(BigDecimal.ONE, null))
		);
		assertEquals(PERFECT_RELATIVE_MATCH_SCORE, sut.judge(ASPIRIN_500_TABLET, targetDosages));
	}

	@Test
	public void drugDosageWithTargetAmountUnitMismatch() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("500"), "mg"), null, new Amount(BigDecimal.ONE, "ml"))
		);
		assertEquals(0, sut.judge(ASPIRIN_500_TABLET, targetDosages));
	}

	@Test
	public void relativeMatchWithNoDrugAmount() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("100"), "mg"), null, new Amount(BigDecimal.ONE, "ml"))
		);
		assertEquals(0, sut.judge(GENERIC_MEDICATION, targetDosages));
	}

	@Test
	public void relativeMatchWithNoDrugAmount2() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("100"), "mg"), null, new Amount(BigDecimal.ONE, null))
		);
		assertEquals(0, sut.judge(GENERIC_MEDICATION, targetDosages));
	}

	@Test
	public void absoluteMatchWithNoDrugAmount() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("100"), "mg"), null, null)
		);
		assertEquals(ABSOLUTE_MATCH_SCORE, sut.judge(GENERIC_MEDICATION, targetDosages));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void whoeverAssignedTheScoresHasNotSuccumbedToInsanity() {
		assertTrue(PERFECT_RELATIVE_MATCH_SCORE >= NORMALIZED_RELATIVE_MATCH_SCORE);
		assertTrue(PERFECT_RELATIVE_MATCH_SCORE >= ABSOLUTE_MATCH_SCORE);
	}

}