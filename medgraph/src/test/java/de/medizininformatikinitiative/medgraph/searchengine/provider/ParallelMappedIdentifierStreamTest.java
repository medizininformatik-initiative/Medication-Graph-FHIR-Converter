package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.TestFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ParallelMappedIdentifierStreamTest {

	@Test
	public void introducesParallelism() {
		assertTrue(TestFactory.PRODUCTS_AND_SUBSTANCES_PROVIDER.parallel().getIdentifiers().isParallel());
	}

}