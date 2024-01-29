package de.tum.med.aiim.markusbudeus.matcher.judge;

import de.tum.med.aiim.markusbudeus.FakeSession;
import de.tum.med.aiim.markusbudeus.matcher.TestWithSession;
import de.tum.med.aiim.markusbudeus.matcher.model.Amount;
import de.tum.med.aiim.markusbudeus.matcher.model.Dosage;
import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DosageMatchJudgeTest extends TestWithSession {
	private static BaseProvider<String> provider;

	private static DosageMatchJudge sut;

	@BeforeAll
	public static void setupAll() {
		provider = BaseProvider.ofDatabaseSynonymes(session);
		sut = new DosageMatchJudge(session);
	}

	@Test
	public void testAbsolute() {
		MatchingTarget target = getProductByName(
				"Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(1000), "mg"), null, null));
		assertEquals(DosageMatchJudge.ABSOLUTE_MATCH_SCORE, sut.judge(target, sampleEntry));
	}

	@Test
	public void testAbsolute2() {
		MatchingTarget target = getProductByName(
				"Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(900), "mg"), null, null));
		assertEquals(0, sut.judge(target, sampleEntry));
	}

	@Test
	public void testAbsolute3() {
		MatchingTarget target = getProductByName(
				"Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(1000), "ml"), null, null));
		assertEquals(0, sut.judge(target, sampleEntry));
	}

	@Test
	public void testRelative() {
		MatchingTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "ml")));
		assertEquals(DosageMatchJudge.PERFECT_RELATIVE_MATCH_SCORE, sut.judge(target, sampleEntry));
	}

	@Test
	public void testRelative2() {
		MatchingTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.TEN, "ml")));
		assertEquals(0, sut.judge(target, sampleEntry));
	}

	@Test
	public void testRelative3() {
		MatchingTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "mg")));
		assertEquals(0, sut.judge(target, sampleEntry));
	}

	@Test
	public void testRelative4() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 5 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("500"), "mg"), null, new Amount(new BigDecimal("5"), "ml")));
		assertEquals(DosageMatchJudge.PERFECT_RELATIVE_MATCH_SCORE, sut.judge(target, sampleEntry));
	}

	@Test
	public void testRelative5() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 5 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("100"), "mg"), null, new Amount(BigDecimal.ONE, "ml")));
		assertEquals(DosageMatchJudge.NORMALIZED_RELATIVE_MATCH_SCORE, sut.judge(target, sampleEntry));
	}

	@Test
	public void matchesDrugAmount() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 10 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("10"), "ml"), null, null)
		);
		assertEquals(DosageMatchJudge.DRUG_AMOUNT_MATCH_SCORE, sut.judge(target, sampleEntry));
	}

	@Test
	public void allMatch() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 10 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("1000"), "mg"), null, new Amount(BigDecimal.TEN, "ml")),
				new Dosage(new Amount(new BigDecimal("10"), "ml"), null, null)
		);
		assertEquals(DosageMatchJudge.PERFECT_RELATIVE_MATCH_SCORE + DosageMatchJudge.DRUG_AMOUNT_MATCH_SCORE,
				sut.judge(target, sampleEntry), 0.01);
	}

	@Test
	public void someMatch() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 5 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("500"), "mg"), null, new Amount(new BigDecimal(5), "ml")),
				new Dosage(new Amount(new BigDecimal("10"), "ml"), null, null)
		);
		assertEquals(DosageMatchJudge.PERFECT_RELATIVE_MATCH_SCORE, sut.judge(target, sampleEntry));
	}

	@Test
	public void noDatabaseAccessRequiredSingle() {
		DosageMatchJudge localSut = new DosageMatchJudge(new FakeSession()); // Cannot access DB!
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 5 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		// No active ingredient dosages set!
		assertEquals(DosageMatchJudge.DOSAGELESS_SCORE, localSut.judge(target, sampleEntry));
	}

	@Test
	public void noDatabaseAccessRequiredMulti() {
		DosageMatchJudge localSut = new DosageMatchJudge(new FakeSession()); // Cannot access DB!
		MatchingTarget target1 = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 5 ml");
		MatchingTarget target2 = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		// No active ingredient dosages set!
		assertEquals(List.of(
						DosageMatchJudge.DOSAGELESS_SCORE,
						DosageMatchJudge.DOSAGELESS_SCORE),
				localSut.batchJudge(List.of(target1, target2), sampleEntry));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void minScoreIsValid() {
		assertTrue(DosageMatchJudge.DOSAGELESS_SCORE >= DosageMatchJudge.MIN_SCORE_ON_MATCH);
		assertTrue(DosageMatchJudge.PERFECT_RELATIVE_MATCH_SCORE >= DosageMatchJudge.MIN_SCORE_ON_MATCH);
		assertTrue(DosageMatchJudge.NORMALIZED_RELATIVE_MATCH_SCORE >= DosageMatchJudge.MIN_SCORE_ON_MATCH);
		assertTrue(DosageMatchJudge.ABSOLUTE_MATCH_SCORE >= DosageMatchJudge.MIN_SCORE_ON_MATCH);
		assertTrue(DosageMatchJudge.DRUG_AMOUNT_MATCH_SCORE >= DosageMatchJudge.MIN_SCORE_ON_MATCH);
		assertTrue(DosageMatchJudge.MIN_SCORE_ON_MATCH > 0);
	}

	private MatchingTarget getProductByName(String name) {

		List<MappedIdentifier<String>> results = provider.identifiers
				.stream().filter(i -> i.identifier.equals(name)).toList();

		if (results.size() != 1) throw new NoSuchElementException("Product name is not unique!");
		return results.get(0).target;
	}

}