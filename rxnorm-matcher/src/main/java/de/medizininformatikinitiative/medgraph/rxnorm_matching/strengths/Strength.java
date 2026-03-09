package de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Represents the strength of an ingredient in two versions. Once as absolute strength (e.g. 100 MG) and once as
 * relative strength (e.g. 10mg/ml).
 *
 * @author Markus Budeus
 */
public class Strength {

	private static final Logger logger = LogManager.getLogger(Strength.class);

	/**
	 * Which one is the canonical value. If true, the absolute value is considered canonical. Otherwise, the relative
	 * value is considered canonical.
	 */
	private final boolean canonicalAbsolute;

	/**
	 * e.g. 100mg
	 */
	@Nullable
	private final AmountOrRange absoluteStrength;
	/**
	 * e.g. 10mg/ml
	 */
	@Nullable
	private final AmountOrRange relativeStrength;

	/**
	 * Constructs a {@link Strength} from a normalized ingredient strength without a drug amount. This means the
	 * resulting strength will only contain an absolute OR relative strength, depnending on what the normalized
	 * strength is.
	 */
	public static Strength fromNormalizedStrength(NormalizedStrength strength) {
		AmountOrRange amount = AmountRange.ofNullableUpperEnd(strength.amountFrom(), strength.amountTo(),
				new Unit(strength.numeratorUnit(), strength.denominatorUnit()));

		if (amount.getUnit().isRelative()) {
			return new Strength(false, null, amount);
		} else {
			return new Strength(true, amount, null);
		}
	}

	/**
	 * Constructs a {@link Strength} from a normalized ingredient strength and the respective normalized drug amount.
	 */
	public static Strength fromNormalizedIngredientAndDrugStrength(NormalizedStrength ingredientStrength,
	                                                               NormalizedStrength drugStrength) {
		AmountOrRange ingredientAmount = AmountRange.ofNullableUpperEnd(ingredientStrength.amountFrom(),
				ingredientStrength.amountTo(),
				new Unit(ingredientStrength.numeratorUnit(), ingredientStrength.denominatorUnit()));
		if (drugStrength.amountTo() != null) {
			throw new IllegalArgumentException("A drug reference strength may not be a range!");
		}
		Amount drugAmount = new Amount(
				drugStrength.amountFrom(),
				new Unit(drugStrength.numeratorUnit(), drugStrength.denominatorUnit())
		);
		return fromIngredientAndDrugStrength(ingredientAmount, drugAmount);
	}

	/**
	 * Constructs a {@link Strength} from a normalized ingredient strength and the respective drug amount.
	 */
	public static Strength fromIngredientAndDrugStrength(AmountOrRange ingredientStrength, Amount drugAmount) {
		if (drugAmount.getUnit().isUnitless()) {
			// If the drug amount is unitless (e.g. 1 Tablet), then multiplying/dividing is useless.
			if (ingredientStrength.getUnit().isRelative()) {
				return new Strength(false, null, ingredientStrength);
			} else {
				return new Strength(true, ingredientStrength, null);
			}
		}
		if (ingredientStrength.getUnit().isRelative()) {
			AmountOrRange absolute = tryMultiply(ingredientStrength, drugAmount);
			// If the drug amount is not the denominator of the ingredient strength, the multiplication will fail
			// or not yield an absolute amount.
			if (absolute != null && absolute.getUnit().isRelative()) absolute = null;
			return new Strength(false, absolute, ingredientStrength);
		} else {
			AmountOrRange relativeStrength = tryDivide(ingredientStrength, drugAmount);
			if (relativeStrength != null) {
				if (relativeStrength.getUnit().isUnitless()) {
					// Unitless result! Happens for things like mg/mg, where the units cancel each other out.
					// We assign the unit manually.
					relativeStrength = relativeStrength.setUnit(
							new Unit(ingredientStrength.getUnit().numeratorUnit(), drugAmount.getUnit().numeratorUnit())
					);
				}
				if (!relativeStrength.getUnit().isRelative()) {
					logger.log(Level.WARN, "Found drug with ingredient strength "+ingredientStrength+
							" and drug amount "+drugAmount+". Cannot establish relative strength.");
					relativeStrength = null;
				}
			}

			return new Strength(true, ingredientStrength, relativeStrength);
		}
	}

	/**
	 * Attempts to multiply the two amounts, but returns null if this fails due to incompatible units.
	 */
	private static AmountOrRange tryMultiply(AmountOrRange amount1, Amount amount2) {
		try {
			return amount1.multiply(amount2);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	/**
	 * Attempts to divideu the two amounts, but returns null if this fails due to incompatible units.
	 */
	private static AmountOrRange tryDivide(AmountOrRange amount1, Amount amount2) {
		try {
			return amount1.divide(amount2);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	public Strength(boolean canonicalAbsolute, @Nullable AmountOrRange absoluteStrength,
	                @Nullable AmountOrRange relativeStrength) {
		this.canonicalAbsolute = canonicalAbsolute;
		if (canonicalAbsolute) {
			if (absoluteStrength == null) {
				throw new IllegalArgumentException("If the absolute strength is canonical, it may not be null!");
			}
		} else {
			if (relativeStrength == null) {
				throw new IllegalArgumentException("If the relative strength is canonical, it may not be null!");
			}
		}
		if (absoluteStrength != null && absoluteStrength.getUnit().isRelative()) {
			throw new IllegalArgumentException(
					"Expected an absolute strength, but got a relative one: " + absoluteStrength.getUnit());
		}
		if (relativeStrength != null && !relativeStrength.getUnit().isRelative()) {
			throw new IllegalArgumentException(
					"Expected a relative strength, but got an absolute one: " + relativeStrength.getUnit());
		}
		this.absoluteStrength = absoluteStrength;
		this.relativeStrength = relativeStrength;
	}

	public boolean matchesAbsoluteOrRelative(Strength other) {
		return matchesAbsoluteOrRelative(other, BigDecimal.ZERO);
	}

	/**
	 * Tests whether this strength matches the given other strength within a certain threshold.
	 *
	 * @param relativeDelta The relative(!) allowed delta. If this is 0.1, this means that the values may be off by up
	 *                      to 10%.
	 */
	public boolean matchesAbsoluteOrRelative(Strength other, BigDecimal relativeDelta) {
		boolean tested = false;
		if (absoluteStrength != null && other.absoluteStrength != null) {
			if (!absoluteStrength.equalsWithRelativeDelta(other.absoluteStrength, relativeDelta)) return false;
			tested = true;
		}
		if (relativeStrength != null && other.relativeStrength != null) {
			if (!relativeStrength.equalsWithRelativeDelta(other.relativeStrength, relativeDelta)) return false;
			tested = true;
		}
		return tested;
	}

	@Override
	public String toString() {
		if (canonicalAbsolute) {
			return absoluteStrength + (relativeStrength == null ? "" :
					"[resp. " + relativeStrength + "]");
		} else {
			return relativeStrength + (absoluteStrength == null ? "" :
					"[resp. " + absoluteStrength + "]");
		}
	}
}
