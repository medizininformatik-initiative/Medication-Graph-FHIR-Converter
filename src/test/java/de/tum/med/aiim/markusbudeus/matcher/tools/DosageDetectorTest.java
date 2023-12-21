package de.tum.med.aiim.markusbudeus.matcher.tools;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DosageDetectorTest {

	@Test
	public void nominatorOnly() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages("Bambec®, 10 mg Tabletten");
		assertSingleMatch(dosages, 9, 5, 10, "mg", null, null, null);
	}

	@Test
	public void nominatorWithComma() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages(
				"Guttaplast 1,39 g Wirkstoffhaltiges Pflaster");
		assertSingleMatch(dosages, 11, 6, new BigDecimal("1.39"), "g", null, null, null);
	}

	@Test
	public void nominatorWithDot() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages(
				"Guttaplast 1.39 g Wirkstoffhaltiges Pflaster");
		assertSingleMatch(dosages, 11, 6, new BigDecimal("1.39"), "g", null, null, null);
	}

	@Test
	public void standAloneNominator() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages("Presinol® 125");
		assertSingleMatch(dosages, 10, 3, 125, null, null, null, null);
	}

	@Test
	public void standAloneNominatorDueToInvalidUnit() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages("Presinol® 120 Äpfel");
		assertSingleMatch(dosages, 10, 3, 120, null, null, null, null);
	}

	@Test
	public void standaloneNominatorTooSmall() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages("Fungata 1 Hartkaps.");
		assertTrue(dosages.isEmpty());
	}

	@Test
	public void nominatorAndDenominator() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages("Endo-Paractol® 0,526 g/100 ml Emulsion");
		assertSingleMatch(dosages, 15, 14, new BigDecimal("0.526"), "g", null, BigDecimal.valueOf(100), "ml");
	}

	@Test
	public void withQualifier() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages("Ferrum Hausmann® 10 mg Eisen/1 ml, Sirup");

		assertSingleMatch(dosages, 17, 16, 10, "mg", "Eisen", 1, "ml");
	}

	@Test
	public void multiMatch() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages(
				"Ciprobay® 200 mg, 200 mg/100 ml, Infusionslösung");

		assertEquals(2, dosages.size());
		assertMatch(dosages.get(0), 10, 6, BigDecimal.valueOf(200), "mg", null, null, null);
		assertMatch(dosages.get(1), 18, 13, BigDecimal.valueOf(200), "mg", null, BigDecimal.valueOf(100), "ml");
	}

	@Test
	public void confusingSeparatorPlacement() {
		List<DosageDetector.Dosage> dosages = DosageDetector.detectDosages(
				"Cotrimoxazol AL forte, Sulfamethoxazol 800 mg/Trimethoprim 160 mg pro Tablette");

		assertEquals(2, dosages.size());
		assertMatch(dosages.get(0), 39, 6, BigDecimal.valueOf(800), "mg", null, null, null);
		assertMatch(dosages.get(1), 59, 6, BigDecimal.valueOf(160), "mg", null, null, null);
}

	public void assertSingleMatch(List<DosageDetector.Dosage> list,
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

	public void assertSingleMatch(List<DosageDetector.Dosage> list,
	                              int startIndex,
	                              int length,
	                              BigDecimal expectedNominator,
	                              String expectedNominatorUnit,
	                              String expectedQualifier,
	                              BigDecimal expectedDenominator,
	                              String expectedDeominatorUnit) {
		assertEquals(1, list.size());
		DosageDetector.Dosage dosage = list.get(0);
		assertMatch(dosage, startIndex, length, expectedNominator, expectedNominatorUnit, expectedQualifier,
				expectedDenominator, expectedDeominatorUnit);
	}

	public void assertMatch(DosageDetector.Dosage dosage,
	                        int startIndex,
	                        int length,
	                        BigDecimal expectedNominator,
	                        String expectedNominatorUnit,
	                        String expectedQualifier,
	                        BigDecimal expectedDenominator,
	                        String expectedDeominatorUnit) {
		assertEquals(startIndex, dosage.startIndex);
		assertEquals(length, dosage.length);
		assertEquals(expectedNominator, dosage.amountNominator.number);
		assertEquals(expectedNominatorUnit, dosage.amountNominator.unit);
		assertEquals(expectedQualifier, dosage.nominatorQualifier);
		if (expectedDenominator == null && expectedDeominatorUnit == null) {
			assertNull(dosage.amountDemoninator);
		} else {
			assertEquals(dosage.amountDemoninator.number, expectedDenominator);
			assertEquals(dosage.amountDemoninator.unit, expectedDeominatorUnit);
		}
	}

}