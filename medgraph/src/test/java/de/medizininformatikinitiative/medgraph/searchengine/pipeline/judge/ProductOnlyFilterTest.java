package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import org.junit.jupiter.api.Test;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ProductOnlyFilterTest {


	@Test
	public void productPasses() {
		assertTrue(new ProductOnlyFilter().passesFilter(SAMPLE_PRODUCT_1, null));
	}

	@Test
	public void productPasses2() {
		assertTrue(new ProductOnlyFilter().passesFilter(SAMPLE_PRODUCT_2, SAMPLE_SEARCH_QUERY));
	}

	@Test
	public void substanceFails() {
		assertFalse(new ProductOnlyFilter().passesFilter(SAMPLE_SUBSTANCE_2, null));
	}

}