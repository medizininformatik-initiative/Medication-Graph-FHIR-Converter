package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Markus Budeus
 */
public class SubstringUsageStatementTest {

	@Test
	void rangeExceedsLowerBound() {
		assertThrows(IllegalArgumentException.class, () -> {
			new SubstringUsageStatement("What is this?", new IntRange(-1, 5));
		});
	}

	@Test
	void rangeExceedsUpperBound() {
		assertThrows(IllegalArgumentException.class, () -> {
			new SubstringUsageStatement("What is this?", new IntRange(10, 14));
		});
	}

	@Test
	void sample1() {
		SubstringUsageStatement sut = new SubstringUsageStatement("A great string!", new IntRange(2, 8));
		assertEquals("great ", sut.getUsedParts());
		assertEquals("A string!", sut.getUnusedParts());
	}

	@Test
	void sample2() {
		SubstringUsageStatement sut = new SubstringUsageStatement("Wh0lesome", new IntRange(0, 5));
		assertEquals("Wh0le", sut.getUsedParts());
		assertEquals("some", sut.getUnusedParts());
	}

	@Test
	void emptyRange() {
		SubstringUsageStatement sut = new SubstringUsageStatement("Waterfall", new IntRange(2, 2));
		assertEquals("", sut.getUsedParts());
		assertEquals("Waterfall", sut.getUnusedParts());
	}

	@Test
	void fullRange() {
		SubstringUsageStatement sut = new SubstringUsageStatement("Waterfall", new IntRange(0, 9));
		assertEquals("Waterfall", sut.getUsedParts());
		assertEquals("", sut.getUnusedParts());
	}

	@Test
	void emptyString() {
		SubstringUsageStatement sut = new SubstringUsageStatement("", new IntRange(0, 0));
		assertEquals("", sut.getUsedParts());
		assertEquals("", sut.getUnusedParts());
	}

}