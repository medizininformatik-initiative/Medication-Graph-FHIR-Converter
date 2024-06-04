package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Markus Budeus
 */
public class MultiSubstringUsageStatementTest extends UnitTest {

	@Test
	void rangeExceedsLowerBound() {
		assertThrows(IllegalArgumentException.class, () -> {
			new MultiSubstringUsageStatement("What is this?", Set.of(new IntRange(-1, 5)));
		});
	}

	@Test
	void rangeExceedsUpperBound() {
		assertThrows(IllegalArgumentException.class, () -> {
			new MultiSubstringUsageStatement("What is this?", Set.of(new IntRange(10, 14)));
		});
	}


	@Test
	void rangeExceedsLowerBound2() {
		assertThrows(IllegalArgumentException.class, () -> {
			new MultiSubstringUsageStatement("SAMPLE", Set.of(new IntRange(2, 3), new IntRange(-2, 1)));
		});
	}

	@Test
	void rangeExceedsUpperBound2() {
		assertThrows(IllegalArgumentException.class, () -> {
			new MultiSubstringUsageStatement("SAMPLE", Set.of(new IntRange(0, 1), new IntRange(3, 7)));
		});
	}

	@Test
	void rangesOverlap() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("long long string",
				Set.of(new IntRange(0, 3), new IntRange(3, 6), new IntRange(4, 8)));
		assertEquals("long lon", sut.getUsedParts());
		assertEquals("g string", sut.getUnusedParts());
	}

	@Test
	void workingSample1() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("A great string!",
				Set.of(new IntRange(2, 8)));
		assertEquals("great ", sut.getUsedParts());
		assertEquals("A string!", sut.getUnusedParts());
	}

	@Test
	void workingSample2() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("Wh0lesome",
				Set.of(new IntRange(0, 3), new IntRange(5, 9)));
		assertEquals("Wh0some", sut.getUsedParts());
		assertEquals("le", sut.getUnusedParts());
	}

	@Test
	void workingSample3() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("May the force be with you",
				Set.of(new IntRange(0, 4), new IntRange(5, 13), new IntRange(21, 25)));
		assertEquals("May he force you", sut.getUsedParts());
		assertEquals("t be with", sut.getUnusedParts());
	}

	@Test
	void emptyRange() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("Waterfall", Set.of(
				new IntRange(2, 2), new IntRange(4, 4)));
		assertEquals("", sut.getUsedParts());
		assertEquals("Waterfall", sut.getUnusedParts());
	}

	@Test
	void fullRange() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("Waterfall",
				Set.of(new IntRange(0, 9)));
		assertEquals("Waterfall", sut.getUsedParts());
		assertEquals("", sut.getUnusedParts());
	}

	@Test
	void splitFullRange() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("Waterfall",
				Set.of(new IntRange(0, 5), new IntRange(5, 9)));
		assertEquals("Waterfall", sut.getUsedParts());
		assertEquals("", sut.getUnusedParts());
	}

	@Test
	void overlappingFullRange() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("Waterfall",
				Set.of(new IntRange(0, 7), new IntRange(2, 9)));
		assertEquals("Waterfall", sut.getUsedParts());
		assertEquals("", sut.getUnusedParts());
	}

	@Test
	void emptyString() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("",
				Set.of(new IntRange(0, 0)));
		assertEquals("", sut.getUsedParts());
		assertEquals("", sut.getUnusedParts());
	}

	@Test
	void distinct() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("What a wonderful world",
				Set.of(
						new IntRange(0, 4),      // What__________________
						new IntRange(2, 6),      // __at a________________
						new IntRange(16, 19),   // ________________ wo___
						new IntRange(19, 22)    // ___________________rld
				));
		assertEquals("What a world", sut.getUsedParts());
		assertEquals("What a world", sut.distinct().getUsedParts());
		assertEquals(Set.of(new IntRange(0, 6), new IntRange(16, 22)), sut.distinct().getUsedRanges());
	}

	@Test
	void rangelessDistinct() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("What a wonderful world",
				Set.of());
		assertEquals("What a wonderful world", sut.distinct().getOriginal());
		assertEquals(Set.of(), sut.distinct().getUsedRanges());
	}

	@Test
	void emptyDistinct() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("", Set.of());
		assertEquals(Set.of(), sut.distinct().getUsedRanges());
	}

	@Test
	void distinctWithoutOverlaps() {
		MultiSubstringUsageStatement sut = new MultiSubstringUsageStatement("Waterfall",
				Set.of(new IntRange(0, 4), new IntRange(5, 8)));

		assertEquals("Watefal", sut.distinct().getUsedParts());
		assertEquals(Set.of(new IntRange(0, 4), new IntRange(5, 8)), sut.distinct().getUsedRanges());
	}

}