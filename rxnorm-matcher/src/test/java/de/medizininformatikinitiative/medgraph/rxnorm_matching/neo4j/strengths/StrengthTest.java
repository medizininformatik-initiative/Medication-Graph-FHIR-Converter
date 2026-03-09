package de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.strengths;

import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Amount;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.AmountRange;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Strength;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
class StrengthTest {


	@Test
	public void absoluteMustBeAbsolute() {
		assertThrows(IllegalArgumentException.class, () -> {
			absoluteStrength("10", "mg/ml");
		});
	}

	@Test
	public void relativeMustBeRelative() {
		assertThrows(IllegalArgumentException.class, () -> {
			relativeStrength("10", "ml");
		});
	}

	@Test
	public void basicAbsolute() {
		Strength strength1 = absoluteStrength("10", "mg");
		Strength strength2 = absoluteStrength("10", "mg");
		Strength strength3 = absoluteStrength("11", "mg");
		Strength strength4 = absoluteStrength("10", "ml");

		assertTrue(strength1.matchesAbsoluteOrRelative(strength2));
		assertFalse(strength1.matchesAbsoluteOrRelative(strength3));
		assertFalse(strength1.matchesAbsoluteOrRelative(strength4));
	}

	@Test
	public void basicRelative() {
		Strength strength1 = relativeStrength("20", "mg/ml");
		Strength strength2 = relativeStrength("20", "mg/ml");
		Strength strength3 = relativeStrength("10", "mg/ml");
		Strength strength4 = relativeStrength("20", "g/g");
		assertTrue(strength1.matchesAbsoluteOrRelative(strength2));
		assertFalse(strength1.matchesAbsoluteOrRelative(strength3));
		assertFalse(strength1.matchesAbsoluteOrRelative(strength4));
	}

	@Test
	public void relativeVsAbsolute() {
		Strength strength1 = absoluteStrength("100", "mg");
		Strength strength2 = new Strength(true,
				amount("100", "mg"),
				amount("10", "mg/ml")
		);
		Strength strength3 = new Strength(false,
				amount("100", "mg"),
				amount("10", "mg/ml")
		);
		Strength strength4 = relativeStrength("10", "mg/ml");

		assertTrue(strength1.matchesAbsoluteOrRelative(strength2));
		assertTrue(strength1.matchesAbsoluteOrRelative(strength3));
		assertFalse(strength1.matchesAbsoluteOrRelative(strength4));

		assertTrue(strength2.matchesAbsoluteOrRelative(strength1));
		assertTrue(strength2.matchesAbsoluteOrRelative(strength2));
		assertTrue(strength2.matchesAbsoluteOrRelative(strength3));
		assertTrue(strength2.matchesAbsoluteOrRelative(strength4));

		assertTrue(strength3.matchesAbsoluteOrRelative(strength1));
		assertTrue(strength3.matchesAbsoluteOrRelative(strength2));
		assertTrue(strength3.matchesAbsoluteOrRelative(strength3));
		assertTrue(strength3.matchesAbsoluteOrRelative(strength4));

		assertFalse(strength4.matchesAbsoluteOrRelative(strength1));
		assertTrue(strength4.matchesAbsoluteOrRelative(strength2));
		assertTrue(strength4.matchesAbsoluteOrRelative(strength3));
	}

	@Test
	public void bothMustMatch() {
		Strength strength1 = new Strength(true,
				amount("100", "mg"),
				amount("5", "mg/ml")
		);
		Strength strength2 = new Strength(true,
				amount("100", "mg"),
				amount("10", "mg/ml")
		);

		assertFalse(strength1.matchesAbsoluteOrRelative(strength2));
	}

	@Test
	public void bothMustMatch2() {
		Strength strength1 = new Strength(false,
				amount("100", "mg"),
				amount("10", "mg/ml")
		);
		Strength strength2 = new Strength(false,
				amount("10", "mg"),
				amount("10", "mg/ml")
		);

		assertFalse(strength1.matchesAbsoluteOrRelative(strength2));
	}

	@Test
	public void amountRange() {
		Strength strength1 = new Strength(true,
				range("100", "110", "mg"),
				null
		);
		Strength strength2 = new Strength(true,
				amount("105", "mg"),
				null
		);
		Strength strength3 = new Strength(true,
				amount("100", "mg"),
				null
		);
		Strength strength4 = new Strength(true,
				amount("110", "mg"),
				null
		);

		assertTrue(strength1.matchesAbsoluteOrRelative(strength2));
		assertTrue(strength2.matchesAbsoluteOrRelative(strength1));

		assertTrue(strength1.matchesAbsoluteOrRelative(strength3));
		assertTrue(strength3.matchesAbsoluteOrRelative(strength1));

		assertTrue(strength1.matchesAbsoluteOrRelative(strength4));
		assertTrue(strength4.matchesAbsoluteOrRelative(strength1));
	}

	@Test
	public void delta() {
		Strength strength1 = absoluteStrength("5.5", "mg");
		Strength strength2 = absoluteStrength("5", "mg");

		assertFalse(strength1.matchesAbsoluteOrRelative(strength2));
		assertFalse(strength1.matchesAbsoluteOrRelative(strength2, new BigDecimal("0.05")));

		assertTrue(strength1.matchesAbsoluteOrRelative(strength2, new BigDecimal("0.1")));
		assertTrue(strength2.matchesAbsoluteOrRelative(strength1, new BigDecimal("0.1")));
	}

	private Strength absoluteStrength(String amount, String unit) {
		return new Strength(true, amount(amount, unit), null);
	}

	private Strength relativeStrength(String amount, String unit) {
		return new Strength(false, null, amount(amount, unit));
	}

	private Amount amount(String amount, String unit) {
		return new Amount(new BigDecimal(amount), Unit.parse(unit));
	}

	private AmountRange range(String from, String to, String unit) {
		return new AmountRange(new BigDecimal(from), new BigDecimal(to), Unit.parse(unit));
	}

}