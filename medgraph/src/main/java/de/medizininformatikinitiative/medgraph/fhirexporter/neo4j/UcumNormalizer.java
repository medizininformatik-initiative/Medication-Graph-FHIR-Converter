package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Utility to normalize UCUM-based units to canonical scales for matching:
 * - Mass numerator -> mg
 * - Volume denominator -> mL
 * - Scalar amounts become mg
 * - Ratio amounts become mg/mL
 *
 * Only a pragmatic subset of UCUM codes is handled here; unknown units are returned as-is
 * so that the caller can log and track coverage.
 */
public final class UcumNormalizer {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    private UcumNormalizer() {}

    public static NormalizedStrength normalize(BigDecimal from, @Nullable BigDecimal to, @Nullable String ucum) {
        if (from == null || ucum == null) return new NormalizedStrength(from, to, null, null);

        String u = ucum.trim();


        // Ratio units (e.g., mg/mL, g/L, ug/mL, mcg/h)
        int slash = u.indexOf('/');
        if (slash > 0) {
            String num = u.substring(0, slash);
            String den = u.substring(slash + 1);
            
  
            Factor fn = factorToMg(num);
            Factor fd = factorToMg(den);
            if (fn.known && fd.known) {
                // For mass/mass ratios, we keep the value as-is (it's already a ratio)
                // But we normalize both to mg for consistency, so the ratio stays the same
                return new NormalizedStrength(from, to, "mg", "mg");
            }
            
            // Volume denominators (mg/mL, g/L, etc.)
            fn = factorToMg(num);
            Factor fdVol = factorToMilliLiter(den);
            if (fn.known && fdVol.known) {
                BigDecimal f = fn.multiplier.divide(fdVol.multiplier, MC);
                BigDecimal fromN = from.multiply(f, MC);
                BigDecimal toN = to != null ? to.multiply(f, MC) : null;
                return new NormalizedStrength(fromN, toN, "mg", "mL");
            }
            // Volume denominators with molar numerator (e.g., mol/L, mmol/L, umol/mL)
            Factor fmmol = factorToMilliMole(num);
            if (fmmol.known && fd.known) {
                BigDecimal f = fmmol.multiplier.divide(fd.multiplier, MC);
                BigDecimal fromN = from.multiply(f, MC);
                BigDecimal toN = to != null ? to.multiply(f, MC) : null;
                // Canonical: mmol per mL (aligns with volume canonicalization to mL)
                return new NormalizedStrength(fromN, toN, "mmol", "mL");
            }
            // Time denominators (e.g., mcg/h, mg/d): normalize numerator to mg, keep denominator unchanged
            // Note: We only check if denominator is a time unit, but don't normalize it
            Factor ft = factorToHour(den);
            if (fn.known && ft.known) {
                // Only normalize the numerator (mass unit), denominator stays as-is
                BigDecimal fromN = from.multiply(fn.multiplier, MC);
                BigDecimal toN = to != null ? to.multiply(fn.multiplier, MC) : null;
                // Preserve the original time unit (h, d, etc.) as denominator
                return new NormalizedStrength(fromN, toN, "mg", den.toLowerCase(Locale.ROOT));
            }
            // Area denominators (e.g., mg/cm2): normalize numerator to mg, keep denominator unchanged
            Factor fa = factorToCm2(den);
            if (fn.known && fa.known) {
                // Only normalize the numerator (mass unit), denominator stays as-is
                BigDecimal fromN = from.multiply(fn.multiplier, MC);
                BigDecimal toN = to != null ? to.multiply(fn.multiplier, MC) : null;
                return new NormalizedStrength(fromN, toN, "mg", "cm2");
            }
            // Unknown ratio -> return as-is to be handled by caller
            return new NormalizedStrength(from, to, u, null);
        }

        // Scalar mass units -> mg
        Factor fm = factorToMg(u);
        if (fm.known) {
            BigDecimal fromN = from.multiply(fm.multiplier, MC);
            BigDecimal toN = to != null ? to.multiply(fm.multiplier, MC) : null;
            return new NormalizedStrength(fromN, toN, "mg", null);
        }

        // Scalar molar units -> mmol
        Factor fmm = factorToMilliMole(u);
        if (fmm.known) {
            BigDecimal fromN = from.multiply(fmm.multiplier, MC);
            BigDecimal toN = to != null ? to.multiply(fmm.multiplier, MC) : null;
            return new NormalizedStrength(fromN, toN, "mmol", null);
        }

        // Scalar volume units -> mL 
        Factor fv = factorToMilliLiter(u);
        if (fv.known) {
            BigDecimal fromN = from.multiply(fv.multiplier, MC);
            BigDecimal toN = to != null ? to.multiply(fv.multiplier, MC) : null;
            return new NormalizedStrength(fromN, toN, "mL", null);
        }

        // Unknown -> return as-is; 
        return new NormalizedStrength(from, to, u, null);
    }

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

    private static Factor factorToCm2(String ucum) {
        String u = ucum.trim();
        String l = u.toLowerCase(Locale.ROOT);
        if (l.equals("cm2") || l.equals("cm²")) return Factor.one();
        return Factor.unknown();
    }

    /**
     * Converts molar units to mmol scaling.
     * Supported: mol -> 1000, mmol -> 1, umol/µmol -> 0.001, nmol -> 0.000001
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

    private record Factor(boolean known, BigDecimal multiplier) {
        static Factor one() { return new Factor(true, BigDecimal.ONE); }
        static Factor of(double d) { return new Factor(true, new BigDecimal(d, MC)); }
        static Factor unknown() { return new Factor(false, BigDecimal.ONE); }
    }
}


