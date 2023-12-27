package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModifiedJaccardMatcherTest {

	@Test
	void perfectMatch() {
		assertEquals(1, ModifiedJaccardMatcher.getJaccardCoefficient(
				Set.of("A", "C", "Apfel"),
				Set.of("C", "Apfel", "A")
		));
	}

	@Test
	void noMatch() {
		assertEquals(0, ModifiedJaccardMatcher.getJaccardCoefficient(
				Set.of("A", "C", "Apfel"),
				Set.of("Haus", "Eier")
		));
	}

	@Test
	void someMatch() {
		assertEquals(0.25, ModifiedJaccardMatcher.getJaccardCoefficient(
				Set.of("A", "C", "Apfel"),
				Set.of("C", "Haus")
		), 0.01);
	}

}