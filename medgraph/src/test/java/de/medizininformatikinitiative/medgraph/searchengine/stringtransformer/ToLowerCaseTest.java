package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ToLowerCaseTest {

	@Test
	public void mixed() {
		assertEquals("hallo welt,", new ToLowerCase().apply("Hallo Welt,"));
	}

	@Test
	public void lowerCase() {
		assertEquals("ich gebe nicht auf!", new ToLowerCase().apply("ich gebe nicht auf!"));
	}

	@Test
	public void upperCase() {
		assertEquals("caps lock gilt als schreien", new ToLowerCase().apply("CAPS LOCK GILT ALS SCHREIEN"));
	}

	@Test
	public void blank() {
		assertEquals("", new ToLowerCase().apply(""));
	}

	@Test
	public void reverseTransform() {
		String original = "Hallo welt,";
		String output = "hallo welt,";
		DistinctMultiSubstringUsageStatement usageStatement = new ToLowerCase().reverseTransformUsageStatement(original,
				new DistinctMultiSubstringUsageStatement(output, Set.of(new IntRange(6, 10))));

		assertEquals(new DistinctMultiSubstringUsageStatement(original,
				Set.of(new IntRange(6, 10))), usageStatement);
	}

	@Test
	public void reverseTransformBlank() {
		String original = "";
		String output = "";
		DistinctMultiSubstringUsageStatement usageStatement = new ToLowerCase().reverseTransformUsageStatement(original,
				new DistinctMultiSubstringUsageStatement(output, Set.of()));

		assertEquals(new DistinctMultiSubstringUsageStatement(original, Set.of()), usageStatement);
	}

}