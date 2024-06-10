package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TransformedIdentifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Markus Budeus
 */
public class CompoundTransformerTest extends UnitTest {

	@Test
	void compoundTransform() {
		Transformer<String, List<String>> compound = new ToLowerCase().and(new WhitespaceTokenizer());

		assertEquals(List.of("water", "flame"), compound.apply("Water Flame"));
	}

	@SuppressWarnings("unchecked")
	@Test
	void compoundTransformIdentifier() {
		Transformer<String, List<String>> compound = new ToLowerCase().and(new WhitespaceTokenizer());

		OriginalIdentifier<String> original = new OriginalIdentifier<>("Roses Are Red", OriginalIdentifier.Source.RAW_QUERY);
		TransformedIdentifier<?, List<String>> ti = compound.apply(original);
		TransformedIdentifier<String, String> intermediate = (TransformedIdentifier<String, String>) ti.getOriginal();

		assertEquals(List.of("roses", "are", "red"), ti.getIdentifier());
		assertEquals("roses are red", intermediate.getIdentifier());
		assertSame(original, intermediate.getOriginal());
	}

}