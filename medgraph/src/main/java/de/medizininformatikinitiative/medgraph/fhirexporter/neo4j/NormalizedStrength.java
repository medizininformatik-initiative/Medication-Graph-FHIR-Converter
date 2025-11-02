package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Canonical representation of a strength value after UCUM normalization.
 * Supports scalar strengths (e.g., mg) and ratio strengths (e.g., mg/mL).
 */
public final class NormalizedStrength {

    private final BigDecimal amountFrom;
    private final BigDecimal amountTo; // may be null
    private final String numeratorUnit; // e.g., "mg" for scalar and ratio
    private final String denominatorUnit; // null for scalar; e.g., "mL" for mg/mL

    public NormalizedStrength(BigDecimal amountFrom, @Nullable BigDecimal amountTo,
                              String numeratorUnit, @Nullable String denominatorUnit) {
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        this.numeratorUnit = numeratorUnit;
        this.denominatorUnit = denominatorUnit;
    }

    public BigDecimal amountFrom() { return amountFrom; }
    public @Nullable BigDecimal amountTo() { return amountTo; }
    public String numeratorUnit() { return numeratorUnit; }
    public @Nullable String denominatorUnit() { return denominatorUnit; }

    public boolean isRatio() { return denominatorUnit != null; }

    @Override
    public String toString() {
        String unit = isRatio() ? numeratorUnit + "/" + denominatorUnit : numeratorUnit;
        if (amountTo == null) return amountFrom + " " + unit;
        return amountFrom + ".." + amountTo + " " + unit;
    }
}


