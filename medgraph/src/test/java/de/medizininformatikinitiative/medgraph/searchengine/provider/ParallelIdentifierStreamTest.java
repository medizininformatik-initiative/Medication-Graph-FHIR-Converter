package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.TestFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ParallelIdentifierStreamTest {

	@Test
	public void introducesParallelism() {
		assertTrue(TestFactory.PRODUCTS_AND_SUBSTANCES_PROVIDER.parallel().getIdentifiers().isParallel());
	}

}