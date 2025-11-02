package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for isolated logic in RxNormProductMatcher that do not require Neo4j or RxNav.
 */
class RxNormProductMatcherUnitTest {

    @Test
    void selectBestRxcui_prefersPIN_overIN_whenResolverAvailable() throws Exception {
        RxNormProductMatcher matcher = new RxNormProductMatcher();

        // Install a resolver that marks specific RXCUIs as PIN/IN
        RxNormProductMatcher.setRxcuiTermTypeResolver(rxcui -> switch (rxcui) {
            case "111" -> "IN";
            case "222" -> "PIN";
            default -> null;
        });

        // Access private selectBestRxcui via reflection
        Method m = RxNormProductMatcher.class.getDeclaredMethod("selectBestRxcui", List.class);
        m.setAccessible(true);

        @SuppressWarnings("unchecked")
        String selected = (String) m.invoke(matcher, List.of("111", "222"));

        assertEquals("222", selected, "PIN should be preferred over IN");
    }

    @Test
    void selectBestRxcui_fallsBackToFirst_whenResolverUnknown() throws Exception {
        RxNormProductMatcher matcher = new RxNormProductMatcher();

        // No resolver or returns null
        RxNormProductMatcher.setRxcuiTermTypeResolver(rxcui -> null);

        Method m = RxNormProductMatcher.class.getDeclaredMethod("selectBestRxcui", List.class);
        m.setAccessible(true);

        @SuppressWarnings("unchecked")
        String selected = (String) m.invoke(matcher, List.of("999", "888"));

        assertEquals("999", selected, "Should fall back to first RXCUI if TTY unknown");
    }

    @Test
    void withinTolerance_checksRelativeDifference() throws Exception {
        RxNormProductMatcher matcher = new RxNormProductMatcher();

        Method m = RxNormProductMatcher.class.getDeclaredMethod("withinTolerance", BigDecimal.class, BigDecimal.class, double.class);
        m.setAccessible(true);

        boolean ok1 = (boolean) m.invoke(matcher, new BigDecimal("100"), new BigDecimal("105"), 0.1d);
        boolean ok2 = (boolean) m.invoke(matcher, new BigDecimal("100"), new BigDecimal("111"), 0.1d);

        assertTrue(ok1, "+5% should be within 10% tolerance");
        assertFalse(ok2, "+11% should exceed 10% tolerance");
    }
}


