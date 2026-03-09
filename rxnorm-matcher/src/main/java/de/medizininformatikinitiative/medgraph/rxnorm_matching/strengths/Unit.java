package de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Markus Budeus
 */
public record Unit(@Nullable String numeratorUnit, @Nullable String denominatorUnit) {

	public static Unit parse(String unit) {
		String[] tokens = unit.split("/", 3);
		if (tokens.length > 2) throw new IllegalArgumentException("Unable to parse unit: " + unit);
		if (tokens.length == 2) return new Unit(tokens[0].trim(), tokens[1].trim());
		return new Unit(unit.trim(), null);
	}

	public Unit(@Nullable String numeratorUnit, @Nullable String denominatorUnit) {
		this.numeratorUnit = numeratorUnit == null ? null : numeratorUnit.isBlank() ? null :
				numeratorUnit.trim().equals("1") ? null : numeratorUnit;
		this.denominatorUnit = denominatorUnit == null ? null : denominatorUnit.isBlank() ? null :
				denominatorUnit.trim().equals("1") ? null : denominatorUnit;
	}

	public Unit multiply(Unit other) {
		String numerator1 = numeratorUnit;
		String numerator2 = other.numeratorUnit;
		String denominator1 = denominatorUnit;
		String denominator2 = other.denominatorUnit;

		if (Objects.equals(numerator1, denominator2)) {
			numerator1 = null;
			denominator2 = null;
		}
		if (Objects.equals(numerator2, denominator1)) {
			numerator2 = null;
			denominator1 = null;
		}

		if ((numerator1 != null && numerator2 != null) || (denominator1 != null && denominator2 != null)) {
			throw new IllegalArgumentException(
					"Incompatible units for multiplication: \"" + this + "\" and \"" + other + "\"");
		}

		return new Unit(
				numerator1 != null ? numerator1 : numerator2,
				denominator1 != null ? denominator1 : denominator2
		);
	}

	public boolean isUnitless() {
		return numeratorUnit == null && denominatorUnit == null;
	}

	public Unit divide(Unit other) {
		return multiply(new Unit(other.denominatorUnit, other.numeratorUnit));
	}

	public boolean isRelative() {
		return denominatorUnit != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Unit u) {
			return Objects.equals(numeratorUnit, u.numeratorUnit) && Objects.equals(denominatorUnit, u.denominatorUnit);
		}
		return false;
	}

	@Override
	public @NotNull String toString() {
		return numeratorUnit == null && denominatorUnit == null ? "" :
				((numeratorUnit != null ? numeratorUnit : "1") +
						(denominatorUnit != null ? "/" + denominatorUnit : ""));
	}
}
