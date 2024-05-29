package de.medizininformatikinitiative.medgraph.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Markus Budeus
 */
public class EDQMTest {

	@Test
	public void correctCode() {
		assertEquals("PDF-00010001", EDQM.PHARMACEUTICAL_DOSE_FORM.validateAndCorrectCode("PDF-00010001"));
		assertEquals("BDF-9876", EDQM.BASIC_DOSE_FORM.validateAndCorrectCode("BDF-9876"));
	}

	@Test
	public void correctShortenedCode() {
		assertEquals("PDF-12345678", EDQM.PHARMACEUTICAL_DOSE_FORM.validateAndCorrectCode("12345678"));
		assertEquals("ISI-4444", EDQM.INTENDED_SITE.validateAndCorrectCode("4444"));
	}

	@Test
	public void incorrectNumberOfDigits() {
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.PHARMACEUTICAL_DOSE_FORM.validateAndCorrectCode("PDF-1533");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.BASIC_DOSE_FORM.validateAndCorrectCode("BDF-15855533");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.INTENDED_SITE.validateAndCorrectCode("BDF-186425");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.RELEASE_CHARACTERISTIC.validateAndCorrectCode("21112425");
		});
	}

	@Test
	public void nonDigitCode() {
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.PHARMACEUTICAL_DOSE_FORM.validateAndCorrectCode("12345T7A");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.BASIC_DOSE_FORM.validateAndCorrectCode("BDF-A113");
		});
	}

	@Test
	public void wrongPrefix() {
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.PHARMACEUTICAL_DOSE_FORM.validateAndCorrectCode("-11551144");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.INTENDED_SITE.validateAndCorrectCode("LOL-1101");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			EDQM.RELEASE_CHARACTERISTIC.validateAndCorrectCode("ISI-1144");
		});
	}

}