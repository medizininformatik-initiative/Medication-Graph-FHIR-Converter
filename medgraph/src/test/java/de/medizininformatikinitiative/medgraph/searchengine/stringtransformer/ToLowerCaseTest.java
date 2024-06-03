package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.MultiSubstringUsageStatement;
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
		MultiSubstringUsageStatement usageStatement = new ToLowerCase().reverseTransformUsageStatement(original,
				new MultiSubstringUsageStatement(output, Set.of(new IntRange(6, 10))));

		assertEquals(new MultiSubstringUsageStatement(original,
				Set.of(new IntRange(6, 10))), usageStatement);
	}

	@Test
	public void reverseTransformBlank() {
		String original = "";
		String output = "";
		MultiSubstringUsageStatement usageStatement = new ToLowerCase().reverseTransformUsageStatement(original,
				new MultiSubstringUsageStatement(output, Set.of()));

		assertEquals(new MultiSubstringUsageStatement(original, Set.of()), usageStatement);
	}

}