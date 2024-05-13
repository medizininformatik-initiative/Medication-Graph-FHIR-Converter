package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class SubstringPresenceMatcherTest {

	@Test
	public void noOverlap() {
		assertEquals(0, SubstringPresenceMatcher.getScore(
				Set.of("Rock", "Paper", "Scissors"),
				"Mince"
		));
	}

	@Test
	public void someOverlap() {
		assertEquals(2, SubstringPresenceMatcher.getScore(
				Set.of("Water", "Flame", "lame", "tame"),
				"Waterflame"
		));
	}

	@Test
	public void perfectOverlap() {
		assertEquals(3, SubstringPresenceMatcher.getScore(
				Set.of("Waterflame", "flame", "terf"),
				"Waterflame"
		));
	}

	@Test
	public void emptySet() {
		assertEquals(0, SubstringPresenceMatcher.getScore(
				Set.of(),
				"Waterflame"
		));
	}

}