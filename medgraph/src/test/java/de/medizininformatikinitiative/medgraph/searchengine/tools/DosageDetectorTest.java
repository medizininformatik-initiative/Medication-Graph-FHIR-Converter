package de.medizininformatikinitiative.medgraph.searchengine.tools;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DosageDetectorTest {

	@Test
	public void nominatorOnly() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Bambec®, 10 mg Tabletten");
		assertSingleMatch(dosages, 9, 5, 10, "mg", null, null, null);
	}

	@Test
	public void nominatorWithComma() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Guttaplast 1,39 g Wirkstoffhaltiges Pflaster");
		assertSingleMatch(dosages, 11, 6, new BigDecimal("1.39"), "g", null, null, null);
	}

	@Test
	public void nominatorWithDot() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Guttaplast 1.39 g Wirkstoffhaltiges Pflaster");
		assertSingleMatch(dosages, 11, 6, new BigDecimal("1.39"), "g", null, null, null);
	}

	@Test
	public void standAloneNominator() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Presinol® 125");
		assertSingleMatch(dosages, 10, 3, 125, null, null, null, null);
	}

	@Test
	public void standAloneNominatorDueToInvalidUnit() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Presinol® 120 Äpfel");
		assertSingleMatch(dosages, 10, 3, 120, null, null, null, null);
	}

	@Test
	public void standaloneNominatorTooSmall() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Fungata 1 Hartkaps.");
		assertTrue(dosages.isEmpty());
	}

	@Test
	public void nominatorAndDenominator() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Endo-Paractol® 0,526 g/100 ml Emulsion");
		assertSingleMatch(dosages, 15, 14, new BigDecimal("0.526"), "g", null, BigDecimal.valueOf(100), "ml");
	}

	@Test
	public void withQualifier() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Ferrum Hausmann® 10 mg Eisen/1 ml, Sirup");

		assertSingleMatch(dosages, 17, 16, 10, "mg", "Eisen", 1, "ml");
	}

	@Test
	public void withSpaceAfterQualifier() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Lanicor® Ampullen 0,25 mg Digoxin / ml Injektionslösung");
		assertSingleMatch(dosages, 18, 20, new BigDecimal("0.25"), "mg", "Digoxin", BigDecimal.ONE, "ml");
	}

	@Test
	public void multiMatch() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Ciprobay® 200 mg, 200 mg/100 ml, Infusionslösung");

		assertEquals(2, dosages.size());
		assertMatch(dosages.get(0), 10, 6, BigDecimal.valueOf(200), "mg", null, null, null);
		assertMatch(dosages.get(1), 18, 13, BigDecimal.valueOf(200), "mg", null, BigDecimal.valueOf(100), "ml");
	}

	@Test
	public void confusingSeparatorPlacement() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Cotrimoxazol AL forte, Sulfamethoxazol 800 mg/Trimethoprim 160 mg pro Tablette");

		assertEquals(2, dosages.size());
		assertMatch(dosages.get(0), 39, 6, BigDecimal.valueOf(800), "mg", null, null, null);
		assertMatch(dosages.get(1), 59, 6, BigDecimal.valueOf(160), "mg", null, null, null);
	}

	@Test
	public void otherUnit() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Berotec® N 100 µg Dosier-Aerosol, Druckgasinhalation, Lösung");
		assertSingleMatch(dosages, 11, 6, 100, "μg", null, null, null);
	}

	@Test
	public void denominatorWithImplicitOne() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Lefax® Pump-Liquid, Suspension mit 41,2 mg/ml Simeticon");
		assertSingleMatch(dosages, 35, 10, new BigDecimal("41.2"), "mg", null, BigDecimal.ONE, "ml");
	}

	@Test
	public void unitNotSeparatedBySpace() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Bambec®, 10mg Tabletten");
		assertSingleMatch(dosages, 9, 4, 10, "mg", null, null, null);
	}

	@Test
	public void badUnitSpacing() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Medication I invented 100 µg/ ml oof");
		assertSingleMatch(dosages, 22, 10, 100, "μg", null, 1, "ml");
	}

	@Test
	public void moreBadUnitSpacing() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Medication I invented 100µg/10 ml oof");
		assertSingleMatch(dosages, 22, 11, 100, "μg", null, 10, "ml");
	}

	@Test
	public void nonUcumDenominatorUnits() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Profact® nasal, 0,105 mg/Sprühstoß Nasenspray, Lösung");
		assertSingleMatch(dosages, 16, 18, new BigDecimal("0.105"), "mg", null, BigDecimal.ONE, "Sprühstoß");
	}

	@Test
	public void lotsOfSpacing() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"talvosilen forte 500 mg / 30 ml Hartkapseln");
		assertSingleMatch(dosages, 17, 14, 500, "mg", null, 30, "ml");
	}

	@Test
	public void multiDosageTablets() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("talvosilen forte 500 mg / 30 mg Hartkapseln");
		assertEquals(2, dosages.size());
		assertMatch(dosages.get(0), 17, 6, BigDecimal.valueOf(500), "mg", null, null, null);
		assertMatch(dosages.get(1), 26, 5, BigDecimal.valueOf(30), "mg", null, null, null);
	}

	@Test
	public void thosandsSeparator() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Medication 100.000");
		assertSingleMatch(dosages, 11, 7, 100000, null, null, null, null);
	}

	@Test
	public void thosandsSeparator2() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Medication 100,000");
		assertSingleMatch(dosages, 11, 7, 100000, null, null, null, null);
	}


	@Test
	public void decimalSeparator() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Medication 100.50");
		assertSingleMatch(dosages, 11, 6, new BigDecimal("100.50"), null, null, null, null);
	}

	@Test
	public void decimalSeparator2() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Medication 100,50");
		assertSingleMatch(dosages, 11, 6, new BigDecimal("100.50"), null, null, null, null);
	}

	@Test
	public void decimalAndThousandsSeparator() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Medication 107,256,550.37");
		assertSingleMatch(dosages, 11, 14, new BigDecimal("107256550.37"), null, null, null, null);
	}

	@Test
	public void decimalAndThousandsSeparatorInverse() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Medication 107.256.550,37");
		assertSingleMatch(dosages, 11, 14, new BigDecimal("107256550.37"), null, null, null, null);
	}


	@Test
	public void internationalUnitAndDotIsNotAComma() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"D3-Vicotrat®, 100.000 I.E./1 ml Injektionslösung");
		assertSingleMatch(dosages, 14, 17, 100000, "I.E.", null, 1, "ml");
	}

	@Test
	public void threeDosages() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Polyspectran® Tropfen, 7500 I.E./ml 3500 I.E./ml 20 µg/ml Augen- und Ohrentropfen, Lösung");

		assertEquals(3, dosages.size());
		assertMatch(dosages.get(0), 23, 12, BigDecimal.valueOf(7500), "I.E.", null, BigDecimal.ONE, "ml");
		assertMatch(dosages.get(1), 36, 12, BigDecimal.valueOf(3500), "I.E.", null, BigDecimal.ONE, "ml");
		assertMatch(dosages.get(2), 49, 8, BigDecimal.valueOf(20), "μg", null, BigDecimal.ONE, "ml");
	}

	@Test
	public void negativeTest() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"ISOPTO-MAX®, Augentropfensuspension");
		assertTrue(dosages.isEmpty());
	}

	@Test
	public void lmIgnored() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Kalium chloricum LM 90");
		assertTrue(dosages.isEmpty());
	}

	@Test
	public void confusingComma() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Imeron® 350, 350 mg Iod/ml, Injektionslösung, Infusionslösung, 200 ml");
		assertEquals(3, dosages.size());
		assertMatch(dosages.get(0), 8, 3, BigDecimal.valueOf(350), null, null, null, null);
		assertMatch(dosages.get(1), 13, 13, BigDecimal.valueOf(350), "mg", "Iod", BigDecimal.ONE, "ml");
		assertMatch(dosages.get(2), 63, 6, BigDecimal.valueOf(200), "ml", null, null, null);
	}

	@Test
	public void denominatorOnlyUnit() {
		// "Tablette" is not allowed as nominator unit!
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages("Aspirin 100 Tablette");
		assertSingleMatch(dosages, 8, 3, BigDecimal.valueOf(100), null, null, null, null);
	}

	@Test
	public void overloadedSlash() {
		// Why would you use the slash both as a ratio separator and as a separator between different amounts?
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"dispadex® comp. 2,45 mg/1 mg/ml Augentropfen, Lösung");
		assertEquals(2, dosages.size());
		assertMatch(dosages.get(0), 16, 7, new BigDecimal("2.45"), "mg", null, null, null);
		assertMatch(dosages.get(1), 24, 7, BigDecimal.valueOf(1), "mg", null, BigDecimal.ONE, "ml");
	}

	@Test
	public void qualifierLikeNewUnit() {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(
				"Aspirin 500 mg 10mg/ml");
		assertEquals(2, dosages.size());
		assertMatch(dosages.get(0), 8, 6, new BigDecimal(500), "mg", null, null, null);
		assertMatch(dosages.get(1), 15, 7, BigDecimal.TEN, "mg", null, BigDecimal.ONE, "ml");
	}

	public void assertSingleMatch(List<DosageDetector.DetectedDosage> list,
	                              int startIndex,
	                              int length,
	                              int expectedNominator,
	                              String expectedNominatorUnit,
	                              String expectedQualifier,
	                              Integer expectedDenominator,
	                              String expectedDeominatorUnit) {
		assertSingleMatch(list,
				startIndex,
				length,
				BigDecimal.valueOf(expectedNominator),
				expectedNominatorUnit,
				expectedQualifier,
				expectedDenominator == null ? null : BigDecimal.valueOf(expectedDenominator),
				expectedDeominatorUnit);
	}

	public void assertSingleMatch(List<DosageDetector.DetectedDosage> list,
	                              int startIndex,
	                              int length,
	                              BigDecimal expectedNominator,
	                              String expectedNominatorUnit,
	                              String expectedQualifier,
	                              BigDecimal expectedDenominator,
	                              String expectedDeominatorUnit) {
		assertEquals(1, list.size());
		DosageDetector.DetectedDosage dosage = list.getFirst();
		assertMatch(dosage, startIndex, length, expectedNominator, expectedNominatorUnit, expectedQualifier,
				expectedDenominator, expectedDeominatorUnit);
	}

	public void assertMatch(DosageDetector.DetectedDosage dosage,
	                        int startIndex,
	                        int length,
	                        BigDecimal expectedNominator,
	                        String expectedNominatorUnit,
	                        String expectedQualifier,
	                        BigDecimal expectedDenominator,
	                        String expectedDeominatorUnit) {
		assertEquals(startIndex, dosage.startIndex);
		assertEquals(length, dosage.length);
		assertEquals(expectedNominator, dosage.getDosage().amountNominator.getNumber());
		assertEquals(expectedNominatorUnit, dosage.getDosage().amountNominator.getUnit());
		assertEquals(expectedQualifier, dosage.getDosage().nominatorQualifier);
		if (expectedDenominator == null && expectedDeominatorUnit == null) {
			assertNull(dosage.getDosage().amountDenominator);
		} else {
			assertNotNull(dosage.getDosage().amountDenominator);
			assertEquals(dosage.getDosage().amountDenominator.getNumber(), expectedDenominator);
			assertEquals(dosage.getDosage().amountDenominator.getUnit(), expectedDeominatorUnit);
		}
	}

}