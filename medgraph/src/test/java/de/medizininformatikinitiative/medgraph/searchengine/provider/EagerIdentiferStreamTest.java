package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.medizininformatikinitiative.medgraph.TestFactory.PRODUCTS_AND_SUBSTANCES_PROVIDER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class EagerIdentiferStreamTest {

	@Test
	public void isActuallyEager() {
		AtomicBoolean transformerRan = new AtomicBoolean(false);
		MappedIdentifierStream<String, Matchable> stream = PRODUCTS_AND_SUBSTANCES_PROVIDER
				.withTransformation(m -> {
					transformerRan.set(true);
					return m;
				});

		assertFalse(transformerRan.get());

		stream.eager();

		assertTrue(transformerRan.get());
	}

	@Test
	public void conservesSequentialism() {
		assertFalse(PRODUCTS_AND_SUBSTANCES_PROVIDER.eager().getIdentifiers().isParallel());
	}

	@Test
	public void conservesParallelism() {
		assertTrue(PRODUCTS_AND_SUBSTANCES_PROVIDER.parallel().eager().getIdentifiers().isParallel());
	}

}