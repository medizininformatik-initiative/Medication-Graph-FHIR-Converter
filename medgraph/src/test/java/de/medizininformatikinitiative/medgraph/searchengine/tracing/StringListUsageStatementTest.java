package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class StringListUsageStatementTest {

	@Test
	void negativeIndex() {
		assertThrows(IllegalArgumentException.class, () -> {
			new StringListUsageStatement(List.of("Hello", "Dear", "World"), Set.of(1, 0, -1));
		});
	}

	@Test
	void indexTooLarge() {
		assertThrows(IllegalArgumentException.class, () -> {
			new StringListUsageStatement(List.of("Hello", "Dear", "World"), Set.of(1, 4));
		});
	}

	@Test
	void indexTooLarge2() {
		assertThrows(IllegalArgumentException.class, () -> {
			new StringListUsageStatement(List.of("Hello", "Dear"), Set.of(2, 0));
		});
	}

	@Test
	void sample1() {
		StringListUsageStatement sut = new StringListUsageStatement(List.of("This", "is", "a", "list"), Set.of(0, 3));
		assertEquals(List.of("This", "list"), sut.getUsedParts());
		assertEquals(List.of("is", "a"), sut.getUnusedParts());
	}

	@Test
	void sample2() {
		StringListUsageStatement sut = new StringListUsageStatement(List.of("Something", "", "bugs", "me!"), Set.of(0, 1, 3));
		assertEquals(List.of("Something", "", "me!"), sut.getUsedParts());
		assertEquals(List.of("bugs"), sut.getUnusedParts());
	}

	@Test
	void nothingUsed() {
		StringListUsageStatement sut = new StringListUsageStatement(List.of("Who", "needs", "words", "anyway"), Set.of());
		assertEquals(List.of(), sut.getUsedParts());
		assertEquals(List.of("Who", "needs", "words", "anyway"), sut.getUnusedParts());
	}

	@Test
	void everythingUsed() {

		StringListUsageStatement sut = new StringListUsageStatement(List.of("I", "need", "words"), Set.of(0, 1, 2));
		assertEquals(List.of("I", "need", "words"), sut.getUsedParts());
		assertEquals(List.of(), sut.getUnusedParts());
	}

	@Test
	void nothingThere() {
		StringListUsageStatement sut = new StringListUsageStatement(List.of(), Set.of());
		assertEquals(List.of(), sut.getUsedParts());
		assertEquals(List.of(), sut.getUnusedParts());
	}

}