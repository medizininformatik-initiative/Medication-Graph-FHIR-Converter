package de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Utility to normalize UCUM-based units to canonical scales for strength matching.
 * <p>
 * Normalization rules:
 * <ul>
 *   <li>Mass units (numerator) → mg</li>
 *   <li>Volume units (denominator) → mL</li>
 *   <li>Molar units → mmol</li>
 *   <li>Scalar amounts → mg</li>
 *   <li>Ratio amounts → mg/mL (or mmol/mL for molar ratios)</li>
 * </ul>
 * <p>
 * Only a pragmatic subset of UCUM codes is handled. Unknown units are returned as-is
 * so that the caller can log and track coverage. Used by {@link RxNormProductMatcher}
 * to normalize drug strengths for comparison with RxNorm SCD candidates.
 *
 * @author Lucy Strüfing
 */
public final class UcumNormalizer {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    private UcumNormalizer() {}

    /**
     * Normalizes a strength value with UCUM unit to canonical form.
     * <p>
     * Handles both scalar units (e.g., "mg", "g", "mmol") and ratio units (e.g., "mg/mL", "g/L").
     * Supports mass, volume, molar, time, and area units. Unknown units are returned unchanged.
     *
     * @param from the primary strength value (required)
     * @param to optional upper bound for range values (null for single values)
     * @param ucum the UCUM unit string (e.g., "mg", "mg/mL", "mmol/L")
     * @return normalized strength with canonical units, or original values if unit is unknown
     */
    public static NormalizedStrength normalize(BigDecimal from, @Nullable BigDecimal to, @Nullable String ucum) {
        if (from == null || ucum == null) {
            return new NormalizedStrength(from, to, null, null);
        }

        String u = ucum.trim();

        // Ratio units (e.g., mg/mL, g/L, ug/mL, mcg/h)
        int slash = u.indexOf('/');
        if (slash > 0) {
            String num = u.substring(0, slash);
            String den = u.substring(slash + 1);

            // Mass/mass ratios (e.g., mg/mg) -> normalize both to mg
            Factor fn = factorToMg(num);
            Factor fd = factorToMg(den);
            if (fn.known && fd.known) {
                return new NormalizedStrength(from, to, "mg", "mg");
            }

            // Mass/volume ratios (e.g., mg/mL, g/L) -> normalize to mg/mL
            fn = factorToMg(num);
            Factor fdVol = factorToMilliLiter(den);
            if (fn.known && fdVol.known) {
                BigDecimal f = fn.multiplier.divide(fdVol.multiplier, MC);
                BigDecimal fromN = from.multiply(f, MC);
                BigDecimal toN = to != null ? to.multiply(f, MC) : null;
                return new NormalizedStrength(fromN, toN, "mg", "mL");
            }

            // Molar/volume ratios (e.g., mol/L, mmol/L, umol/mL) -> normalize to mmol/mL
            Factor fmmol = factorToMilliMole(num);
            if (fmmol.known && fdVol.known) {
                BigDecimal f = fmmol.multiplier.divide(fdVol.multiplier, MC);
                BigDecimal fromN = from.multiply(f, MC);
                BigDecimal toN = to != null ? to.multiply(f, MC) : null;
                return new NormalizedStrength(fromN, toN, "mmol", "mL");
            }

            // Mass/time ratios (e.g., mcg/h, mg/d) -> normalize numerator to mg, preserve time unit
            Factor ft = factorToHour(den);
            if (fn.known && ft.known) {
                BigDecimal fromN = from.multiply(fn.multiplier, MC);
                BigDecimal toN = to != null ? to.multiply(fn.multiplier, MC) : null;
                return new NormalizedStrength(fromN, toN, "mg", den.toLowerCase(Locale.ROOT));
            }

            // Mass/area ratios (e.g., mg/cm2) -> normalize numerator to mg, preserve area unit
            Factor fa = factorToCm2(den);
            if (fn.known && fa.known) {
                BigDecimal fromN = from.multiply(fn.multiplier, MC);
                BigDecimal toN = to != null ? to.multiply(fn.multiplier, MC) : null;
                return new NormalizedStrength(fromN, toN, "mg", "cm2");
            }

            // Unknown ratio -> return as-is
            return new NormalizedStrength(from, to, u, null);
        }

        // Scalar mass units -> normalize to mg
        Factor fm = factorToMg(u);
        if (fm.known) {
            BigDecimal fromN = from.multiply(fm.multiplier, MC);
            BigDecimal toN = to != null ? to.multiply(fm.multiplier, MC) : null;
            return new NormalizedStrength(fromN, toN, "mg", null);
        }

        // Scalar molar units -> normalize to mmol
        Factor fmm = factorToMilliMole(u);
        if (fmm.known) {
            BigDecimal fromN = from.multiply(fmm.multiplier, MC);
            BigDecimal toN = to != null ? to.multiply(fmm.multiplier, MC) : null;
            return new NormalizedStrength(fromN, toN, "mmol", null);
        }

        // Scalar volume units -> normalize to mL
        Factor fv = factorToMilliLiter(u);
        if (fv.known) {
            BigDecimal fromN = from.multiply(fv.multiplier, MC);
            BigDecimal toN = to != null ? to.multiply(fv.multiplier, MC) : null;
            return new NormalizedStrength(fromN, toN, "mL", null);
        }

        // Unknown unit -> return as-is
        return new NormalizedStrength(from, to, u, null);
    }

    /**
     * Calculates conversion factor from mass unit to milligrams.
     * <p>
     * Supported units: mg, g, kg, ug/µg/mcg, ng
     *
     * @param ucum the UCUM mass unit string
     * @return conversion factor (multiplier to convert to mg), or unknown if unit not recognized
     */
    private static Factor factorToMg(String ucum) {
        String u = ucum.trim();
        String l = u.toLowerCase(Locale.ROOT);
        if (l.equals("mg")) return Factor.one();
        if (l.equals("g")) return Factor.of(1000);
        if (l.equals("kg")) return Factor.of(1_000_000);
        if (l.equals("ug") || l.equals("µg") || l.equals("mcg")) return Factor.of(0.001);
        if (l.equals("ng")) return Factor.of(0.000001);
        return Factor.unknown();
    }

    /**
     * Calculates conversion factor from volume unit to milliliters.
     * <p>
     * Supported units: ml, l, ul/µl/microl, nl/nanol, dl/decil
     *
     * @param ucum the UCUM volume unit string
     * @return conversion factor (multiplier to convert to mL), or unknown if unit not recognized
     */
    private static Factor factorToMilliLiter(String ucum) {
        String u = ucum.trim();
        String l = u.toLowerCase(Locale.ROOT);
        if (l.equals("ml")) return Factor.one();
        if (l.equals("l")) return Factor.of(1000);
        if (l.equals("ul") || l.equals("µl") || l.equals("microl")) return Factor.of(0.001);
        if (l.equals("nl") || l.equals("nanol")) return Factor.of(0.000001);
        if (l.equals("dl") || l.equals("decil")) return Factor.of(100);
        return Factor.unknown();
    }

    /**
     * Calculates conversion factor from time unit to hours.
     * <p>
     * Supported units: h/hr/hour, d/day, 24.h/24.hr/24.hour, min/minute, s/sec/second
     *
     * @param ucum the UCUM time unit string
     * @return conversion factor (multiplier to convert to hours), or unknown if unit not recognized
     */
    private static Factor factorToHour(String ucum) {
        String u = ucum.trim();
        String l = u.toLowerCase(Locale.ROOT);
        if (l.equals("h") || l.equals("hr") || l.equals("hour")) return Factor.one();
        if (l.equals("d") || l.equals("day")) return Factor.of(24.0);
        if (l.equals("24.h") || l.equals("24.hr") || l.equals("24.hour")) return Factor.of(24.0);
        if (l.equals("min") || l.equals("minute")) return Factor.of(1.0/60.0);
        if (l.equals("s") || l.equals("sec") || l.equals("second")) return Factor.of(1.0/3600.0);
        return Factor.unknown();
    }

    /**
     * Calculates conversion factor from area unit to cm².
     * <p>
     * Supported units: cm2, cm²
     *
     * @param ucum the UCUM area unit string
     * @return conversion factor (always 1.0 for recognized units), or unknown if unit not recognized
     */
    private static Factor factorToCm2(String ucum) {
        String u = ucum.trim();
        String l = u.toLowerCase(Locale.ROOT);
        if (l.equals("cm2") || l.equals("cm²")) return Factor.one();
        return Factor.unknown();
    }

    /**
     * Calculates conversion factor from molar unit to millimoles.
     * <p>
     * Supported units: mol, mmol, umol/µmol/μmol, nmol
     *
     * @param ucum the UCUM molar unit string
     * @return conversion factor (multiplier to convert to mmol), or unknown if unit not recognized
     */
    private static Factor factorToMilliMole(String ucum) {
        String u = ucum.trim();
        String l = u.toLowerCase(Locale.ROOT);
        if (l.equals("mmol")) return Factor.one();
        if (l.equals("mol")) return Factor.of(1000);
        if (l.equals("umol") || l.equals("µmol") || l.equals("μmol")) return Factor.of(0.001);
        if (l.equals("nmol")) return Factor.of(0.000001);
        return Factor.unknown();
    }

    /**
     * Represents a unit conversion factor.
     * <p>
     * Used internally to track whether a unit is recognized and what multiplier
     * is needed to convert it to the canonical form.
     *
     * @param known true if the unit is recognized and can be converted
     * @param multiplier the conversion multiplier (multiply original value by this to get canonical value)
     */
    private record Factor(boolean known, BigDecimal multiplier) {
        /**
         * Creates a factor with multiplier 1.0 (no conversion needed).
         */
        static Factor one() {
            return new Factor(true, BigDecimal.ONE);
        }

        /**
         * Creates a factor with the specified multiplier.
         *
         * @param d the conversion multiplier
         */
        static Factor of(double d) {
            return new Factor(true, new BigDecimal(d, MC));
        }

        /**
         * Creates an unknown factor (unit not recognized).
         */
        static Factor unknown() {
            return new Factor(false, BigDecimal.ONE);
        }
    }
}