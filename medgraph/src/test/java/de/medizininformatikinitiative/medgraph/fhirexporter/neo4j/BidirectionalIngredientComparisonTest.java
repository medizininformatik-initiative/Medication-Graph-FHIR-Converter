package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for bidirectional ingredient comparison logic.
 * Tests the new bidirectional comparison that ensures MMI and RxNorm ingredients are identical.
 */
class BidirectionalIngredientComparisonTest {

    private RxNormProductMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new RxNormProductMatcher();
    }

    @Test
    void areIngredientsCompatible_identicalSets_returnsTrue() throws Exception {
        // Test with identical sets
        Set<String> expected = Set.of("123", "456", "789");
        Set<String> candidate = Set.of("123", "456", "789");
        
        Method method = RxNormProductMatcher.class.getDeclaredMethod("areIngredientsCompatible", Set.class, Set.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(matcher, expected, candidate);
        
        assertTrue(result, "Identical ingredient sets should be compatible");
    }

    @Test
    void areIngredientsCompatible_differentSets_returnsFalse() throws Exception {
        // Test with different sets
        Set<String> expected = Set.of("123", "456");
        Set<String> candidate = Set.of("123", "789");
        
        Method method = RxNormProductMatcher.class.getDeclaredMethod("areIngredientsCompatible", Set.class, Set.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(matcher, expected, candidate);
        
        assertFalse(result, "Different ingredient sets should not be compatible");
    }

    @Test
    void areIngredientsCompatible_expectedSubset_returnsFalse() throws Exception {
        // Test bidirectional check: expected is subset of candidate (should fail)
        Set<String> expected = Set.of("123", "456");
        Set<String> candidate = Set.of("123", "456", "789");
        
        Method method = RxNormProductMatcher.class.getDeclaredMethod("areIngredientsCompatible", Set.class, Set.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(matcher, expected, candidate);
        
        assertFalse(result, "Expected subset should fail bidirectional check");
    }

    @Test
    void areIngredientsCompatible_candidateSubset_returnsFalse() throws Exception {
        // Test bidirectional check: candidate is subset of expected (should fail)
        Set<String> expected = Set.of("123", "456", "789");
        Set<String> candidate = Set.of("123", "456");
        
        Method method = RxNormProductMatcher.class.getDeclaredMethod("areIngredientsCompatible", Set.class, Set.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(matcher, expected, candidate);
        
        assertFalse(result, "Candidate subset should fail bidirectional check");
    }

    @Test
    void areIngredientsCompatible_emptyCandidate_returnsTrue() throws Exception {
        // Test with empty candidate (relaxed validation)
        Set<String> expected = Set.of("123", "456");
        Set<String> candidate = new HashSet<>();
        
        Method method = RxNormProductMatcher.class.getDeclaredMethod("areIngredientsCompatible", Set.class, Set.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(matcher, expected, candidate);
        
        assertTrue(result, "Empty candidate should be compatible (relaxed validation)");
    }

    @Test
    void areIngredientsCompatible_emptyExpected_returnsFalse() throws Exception {
        // Test with empty expected - should fail bidirectional check
        Set<String> expected = new HashSet<>();
        Set<String> candidate = Set.of("123", "456");
        
        Method method = RxNormProductMatcher.class.getDeclaredMethod("areIngredientsCompatible", Set.class, Set.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(matcher, expected, candidate);
        
        assertFalse(result, "Empty expected should fail bidirectional check when candidate has ingredients");
    }
}
