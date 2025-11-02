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
        // Case-insensitive variant support
        String l = u.toLowerCase(Locale.ROOT);

        // Ratio units (e.g., mg/mL, g/L, ug/mL, mcg/h)
        int slash = u.indexOf('/');
        if (slash > 0) {
            String num = u.substring(0, slash);
            String den = u.substring(slash + 1);
            Factor fn = factorToMg(num);
            Factor fd = factorToMilliLiter(den);
            if (fn.known && fd.known) {
                BigDecimal f = fn.multiplier.divide(fd.multiplier, MC);
                BigDecimal fromN = from.multiply(f, MC);
                BigDecimal toN = to != null ? to.multiply(f, MC) : null;
                return new NormalizedStrength(fromN, toN, "mg", "mL");
            }
            // Time denominators (e.g., mcg/h): normalize numerator to mg, keep denominator
            Factor ft = factorToHour(den);
            if (fn.known && ft.known) {
                BigDecimal f = fn.multiplier.divide(ft.multiplier, MC);
                BigDecimal fromN = from.multiply(f, MC);
                BigDecimal toN = to != null ? to.multiply(f, MC) : null;
                return new NormalizedStrength(fromN, toN, "mg", "h");
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
        return Factor.unknown();
    }

    private static Factor factorToHour(String ucum) {
        String u = ucum.trim();
        String l = u.toLowerCase(Locale.ROOT);
        if (l.equals("h") || l.equals("hr")) return Factor.one();
        if (l.equals("min")) return Factor.of(1.0/60.0);
        if (l.equals("s") || l.equals("sec")) return Factor.of(1.0/3600.0);
        return Factor.unknown();
    }

    private record Factor(boolean known, BigDecimal multiplier) {
        static Factor one() { return new Factor(true, BigDecimal.ONE); }
        static Factor of(double d) { return new Factor(true, new BigDecimal(d, MC)); }
        static Factor unknown() { return new Factor(false, BigDecimal.ONE); }
    }
}


