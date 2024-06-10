package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.TestFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class TransformedIdentifierStreamTest {

	@Test
	public void transformsInput() {
		List<MappedIdentifier<String>> identifiers = TestFactory.PRODUCTS_AND_SUBSTANCES_PROVIDER
				.withTransformation(name -> name + "A113")
				.getIdentifiers()
				.toList();
		for (MappedIdentifier<String> mi: identifiers) {
			assertTrue(mi.identifier.getIdentifier().endsWith("A113"));
		}
	}

	@Test
	public void isNotEager() {
		AtomicBoolean transformationRan = new AtomicBoolean(false);
		TestFactory.PRODUCTS_AND_SUBSTANCES_PROVIDER
				.withTransformation(name -> {
					transformationRan.set(true);
					return name;
				})
				.getIdentifiers();
		assertFalse(transformationRan.get());
	}

}