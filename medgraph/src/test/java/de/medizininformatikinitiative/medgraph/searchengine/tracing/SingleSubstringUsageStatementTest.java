package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Markus Budeus
 */
public class SingleSubstringUsageStatementTest {

	@Test
	void rangeExceedsLowerBound() {
		assertThrows(IllegalArgumentException.class, () -> {
			new SingleSubstringUsageStatement("What is this?", new IntRange(-1, 5));
		});
	}

	@Test
	void rangeExceedsUpperBound() {
		assertThrows(IllegalArgumentException.class, () -> {
			new SingleSubstringUsageStatement("What is this?", new IntRange(10, 14));
		});
	}

	@Test
	void sample1() {
		SingleSubstringUsageStatement sut = new SingleSubstringUsageStatement("A great string!", new IntRange(2, 8));
		assertEquals("great ", sut.getUsedParts());
		assertEquals("A string!", sut.getUnusedParts());
	}

	@Test
	void sample2() {
		SingleSubstringUsageStatement sut = new SingleSubstringUsageStatement("Wh0lesome", new IntRange(0, 5));
		assertEquals("Wh0le", sut.getUsedParts());
		assertEquals("some", sut.getUnusedParts());
	}

	@Test
	void emptyRange() {
		SingleSubstringUsageStatement sut = new SingleSubstringUsageStatement("Waterfall", new IntRange(2, 2));
		assertEquals("", sut.getUsedParts());
		assertEquals("Waterfall", sut.getUnusedParts());
	}

	@Test
	void fullRange() {
		SingleSubstringUsageStatement sut = new SingleSubstringUsageStatement("Waterfall", new IntRange(0, 9));
		assertEquals("Waterfall", sut.getUsedParts());
		assertEquals("", sut.getUnusedParts());
	}

	@Test
	void emptyString() {
		SingleSubstringUsageStatement sut = new SingleSubstringUsageStatement("", new IntRange(0, 0));
		assertEquals("", sut.getUsedParts());
		assertEquals("", sut.getUnusedParts());
	}

}