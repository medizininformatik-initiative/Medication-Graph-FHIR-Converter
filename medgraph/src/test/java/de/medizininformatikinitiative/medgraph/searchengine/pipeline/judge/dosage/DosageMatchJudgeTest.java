package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.db.DbAmount;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDosage;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDrugDosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
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
	private static final List<DbDrugDosage> METHYLPREDNISOLUTE_1000 = List.of(
			new DbDrugDosage(new DbAmount(BigDecimal.ONE, null), List.of(
					new DbDosage(new BigDecimal("1325.94"), "mg"),
					new DbDosage(new BigDecimal(1000), "mg")
			))
	);

	/**
	 * 0.5 milligrams of prodrug, respective 0.423 mg of active ingredient in 1 ml fluid.
	 */
	private static final List<DbDrugDosage> BERBERIL_EYE_DROPS = List.of(
			new DbDrugDosage(new DbAmount(BigDecimal.ONE, "ml"), List.of(
					new DbDosage(new BigDecimal("0.5"), "mg"),
					new DbDosage(new BigDecimal("0.423"), "mg")
			))
	);

	/**
	 * 500 milligrams of active ingredient in 5ml fluid.
	 */
	private static final List<DbDrugDosage> TRANEXAMIC_ACID = List.of(
			new DbDrugDosage(new DbAmount(new BigDecimal(5), "ml"), List.of(
					new DbDosage(new BigDecimal("500"), "mg")
			))
	);


	@BeforeAll
	public static void setupAll() {
		sut = new DosageMatchJudge();
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
	public void testRelativeWithWrongDenominatorUnit() {
		List<Dosage> targetDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "mg"))
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
	@SuppressWarnings("ConstantConditions")
	public void whoeverAssignedTheScoresHasNotSuccumbedToInsanity() {
		assertTrue(PERFECT_RELATIVE_MATCH_SCORE >= NORMALIZED_RELATIVE_MATCH_SCORE);
		assertTrue(PERFECT_RELATIVE_MATCH_SCORE >= ABSOLUTE_MATCH_SCORE);
	}

}