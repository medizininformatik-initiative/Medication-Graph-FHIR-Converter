package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import org.junit.jupiter.api.Test;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class SubstanceOnlyFilterTest {

	@Test
	public void substancePasses() {
		assertTrue(new SubstanceOnlyFilter().passesFilter(SAMPLE_SUBSTANCE_1, null));
	}

	@Test
	public void substancePasses2() {
		assertTrue(new SubstanceOnlyFilter().passesFilter(SAMPLE_SUBSTANCE_2, SAMPLE_SEARCH_QUERY));
	}

	@Test
	public void ProductFails() {
		assertFalse(new SubstanceOnlyFilter().passesFilter(SAMPLE_PRODUCT_2, null));
	}

}