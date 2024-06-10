package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TransformedIdentifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class TransformerTest extends UnitTest {

	@Test
	public void transformIdentifier() {
		Transformer<List<String>, List<String>> transformer = new TrimSpecialSuffixSymbols();
		OriginalIdentifier<List<String>> original = new OriginalIdentifier<>(List.of("Hello."), OriginalIdentifier.Source.KNOWN_IDENTIFIER);
		TransformedIdentifier<?, List<String>> ti = transformer.apply(original);

		assertNotNull(ti);
		assertSame(transformer, ti.getTransformer());
		assertEquals(original, ti.getOriginal());
		assertEquals(List.of("Hello"), ti.getIdentifier());
	}

}