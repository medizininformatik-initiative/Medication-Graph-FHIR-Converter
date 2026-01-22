package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Canonical representation of a strength value after UCUM normalization.
 * Supports scalar strengths (e.g., mg) and ratio strengths (e.g., mg/mL).
 * @author Lucy Strüfing
 */
public final class NormalizedStrength {

    private final BigDecimal amountFrom;
    private final BigDecimal amountTo;
    private final String numeratorUnit;
    private final String denominatorUnit;

    /**
     * Creates a normalized strength representation.
     *
     * @param amountFrom the lower bound of the strength value (required)
     * @param amountTo the upper bound of the strength value, or null if not a range
     * @param numeratorUnit the numerator unit (e.g., "mg", "g") - required for both scalar and ratio strengths
     * @param denominatorUnit the denominator unit (e.g., "mL", "L") - null for scalar strengths, required for ratios
     */
    public NormalizedStrength(BigDecimal amountFrom, @Nullable BigDecimal amountTo,
                              String numeratorUnit, @Nullable String denominatorUnit) {
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        this.numeratorUnit = numeratorUnit;
        this.denominatorUnit = denominatorUnit;
    }

    /**
     * Returns the lower bound of the strength value.
     */
    public BigDecimal amountFrom() { return amountFrom; }

    /**
     * Returns the upper bound of the strength value, or null if not a range.
     */
    public @Nullable BigDecimal amountTo() { return amountTo; }

    /**
     * Returns the numerator unit (e.g., "mg", "g").
     */
    public String numeratorUnit() { return numeratorUnit; }

    /**
     * Returns the denominator unit (e.g., "mL", "L"), or null for scalar strengths.
     */
    public @Nullable String denominatorUnit() { return denominatorUnit; }

    /**
     * Checks if this strength represents a ratio (e.g., mg/mL) rather than a scalar value (e.g., mg).
     *
     * @return true if denominatorUnit is not null, false otherwise
     */
    public boolean isRatio() { return denominatorUnit != null; }

    /**
     * Returns a string representation of the strength.
     */
    @Override
    public String toString() {
        String unit = isRatio() ? numeratorUnit + "/" + denominatorUnit : numeratorUnit;
        if (amountTo == null) return amountFrom + " " + unit;
        return amountFrom + ".." + amountTo + " " + unit;
    }
}