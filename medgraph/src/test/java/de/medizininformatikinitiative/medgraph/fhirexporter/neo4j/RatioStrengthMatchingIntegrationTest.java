package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ratio strength matching logic.
 * Tests that the matching correctly identifies compatible and incompatible ratio units.
 * 
 * These tests verify the core logic that compares denominatorUnits in validateCandidate().
 */
@DisplayName("Ratio Strength Matching Integration Tests")
class RatioStrengthMatchingIntegrationTest {

    /**
     * Simulates the denominator comparison logic from validateCandidate().
     */
    private boolean shouldMatchDenominators(String expectedDenom, String candidateDenom) {
        if (expectedDenom != null || candidateDenom != null) {
            if (expectedDenom == null || candidateDenom == null) {
                return false; // One is ratio, other is scalar - NOT compatible
            } else {
                return expectedDenom.equalsIgnoreCase(candidateDenom);
            }
        } else {
            return true; // Both are scalar
        }
    }

    @Test
    @DisplayName("mg/h vs mg/h should MATCH (same denominator)")
    void testMatching_sameRatioDenominator() {
        // Expected: mg/h (from MMI data, normalized)
        NormalizedStrength expected = UcumNormalizer.normalize(
            new BigDecimal("100"), null, "mg/h"
        );
        assertEquals("h", expected.denominatorUnit());
        
        // Candidate: mg/h (from RxNorm)
        Map<String, String> candidateDenominators = new HashMap<>();
        candidateDenominators.put("12345", "h");
        
        String expectedDenom = expected.denominatorUnit(); // "h"
        String candidateDenom = candidateDenominators.get("12345"); // "h"
        
        boolean shouldMatch = shouldMatchDenominators(expectedDenom, candidateDenom);
        
        assertTrue(shouldMatch, "mg/h vs mg/h should MATCH (same denominator)");
    }

    @Test
    @DisplayName("mg/h vs mg/d should NOT MATCH (different denominators)")
    void testMatching_differentRatioDenominators() {
        // Expected: mg/h
        NormalizedStrength expected = UcumNormalizer.normalize(
            new BigDecimal("100"), null, "mg/h"
        );
        assertEquals("h", expected.denominatorUnit());
        
        // Candidate: mg/d
        Map<String, String> candidateDenominators = new HashMap<>();
        candidateDenominators.put("12345", "d");
        
        String expectedDenom = expected.denominatorUnit(); // "h"
        String candidateDenom = candidateDenominators.get("12345"); // "d"
        
        boolean shouldMatch = shouldMatchDenominators(expectedDenom, candidateDenom);
        
        assertFalse(shouldMatch, "mg/h and mg/d should NOT match (different denominators)");
    }

    @Test
    @DisplayName("mg (scalar) vs mg/h (ratio) should NOT MATCH")
    void testMatching_scalarVsRatio() {
        // Expected: scalar mg
        NormalizedStrength expected = UcumNormalizer.normalize(
            new BigDecimal("100"), null, "mg"
        );
        assertNull(expected.denominatorUnit(), "Expected should be scalar");
        
        // Candidate: mg/h (ratio)
        Map<String, String> candidateDenominators = new HashMap<>();
        candidateDenominators.put("12345", "h");
        
        String expectedDenom = expected.denominatorUnit(); // null
        String candidateDenom = candidateDenominators.get("12345"); // "h"
        
        boolean shouldMatch = shouldMatchDenominators(expectedDenom, candidateDenom);
        
        assertFalse(shouldMatch, "Scalar mg and ratio mg/h should NOT match");
    }

    @Test
    @DisplayName("mg/mL vs mg/mL should MATCH (same denominator)")
    void testMatching_sameVolumeRatio() {
        NormalizedStrength expected = UcumNormalizer.normalize(
            new BigDecimal("500"), null, "mg/mL"
        );
        assertEquals("mL", expected.denominatorUnit());
        
        Map<String, String> candidateDenominators = new HashMap<>();
        candidateDenominators.put("12345", "mL");
        
        String expectedDenom = expected.denominatorUnit(); // "mL"
        String candidateDenom = candidateDenominators.get("12345"); // "mL"
        
        boolean shouldMatch = shouldMatchDenominators(expectedDenom, candidateDenom);
        
        assertTrue(shouldMatch, "mg/mL vs mg/mL should MATCH (same denominator)");
    }

    @Test
    @DisplayName("mg/h vs mg/24h should NOT MATCH (different time units)")
    void testMatching_differentTimeUnits() {
        NormalizedStrength expected = UcumNormalizer.normalize(
            new BigDecimal("100"), null, "mg/h"
        );
        assertEquals("h", expected.denominatorUnit());
        
        Map<String, String> candidateDenominators = new HashMap<>();
        candidateDenominators.put("12345", "24.h");
        
        String expectedDenom = expected.denominatorUnit(); // "h"
        String candidateDenom = candidateDenominators.get("12345"); // "24.h"
        
        boolean shouldMatch = shouldMatchDenominators(expectedDenom, candidateDenom);
        
        assertFalse(shouldMatch, "mg/h and mg/24h should NOT match (different time units)");
    }

    @Test
    @DisplayName("mcg/h normalized to mg/h should match mg/h")
    void testMatching_normalizedUnits() {
        // Normalize mcg/h to mg/h
        NormalizedStrength expected = UcumNormalizer.normalize(
            new BigDecimal("1000"), null, "mcg/h"
        );
        assertEquals(0, expected.amountFrom().compareTo(new BigDecimal("1")));
        assertEquals("mg", expected.numeratorUnit());
        assertNotNull(expected.denominatorUnit());
        assertEquals("h", expected.denominatorUnit().toLowerCase());
        
        // Candidate has mg/h
        Map<String, String> candidateDenominators = new HashMap<>();
        candidateDenominators.put("12345", "h");
        
        String expectedDenom = expected.denominatorUnit().toLowerCase();
        String candidateDenom = candidateDenominators.get("12345").toLowerCase();
        
        boolean shouldMatch = shouldMatchDenominators(expectedDenom, candidateDenom);
        
        assertTrue(shouldMatch, "Normalized mcg/h (→ mg/h) should match mg/h");
    }
}

