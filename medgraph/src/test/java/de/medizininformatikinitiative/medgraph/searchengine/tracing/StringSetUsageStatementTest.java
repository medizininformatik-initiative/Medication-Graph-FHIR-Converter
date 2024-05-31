package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class StringSetUsageStatementTest {

	@Test
	void usedTokenMissing() {
		assertThrows(IllegalArgumentException.class, () -> {
			new StringSetUsageStatement(Set.of("Hello", "Dear", "World"), Set.of("hello"));
		});
	}

	@Test
	void usedTokenMissing2() {
		assertThrows(IllegalArgumentException.class, () -> {
			new StringSetUsageStatement(Set.of("Hello", "Dear", "World"), Set.of("Hello", "World", "!"));
		});
	}

	@Test
	void sample1() {
		StringSetUsageStatement sut = new StringSetUsageStatement(Set.of("This", "is", "a", "list"), Set.of("This", "list"));
		assertEquals(Set.of("This", "list"), sut.getUsedParts());
		assertEquals(Set.of("is", "a"), sut.getUnusedParts());
	}

	@Test
	void sample2() {
		StringSetUsageStatement sut = new StringSetUsageStatement(Set.of("Something", "", "bugs", "me!"), Set.of("Something", "me!", ""));
		assertEquals(Set.of("Something", "", "me!"), sut.getUsedParts());
		assertEquals(Set.of("bugs"), sut.getUnusedParts());
	}

	@Test
	void nothingUsed() {
		StringSetUsageStatement sut = new StringSetUsageStatement(Set.of("Who", "needs", "words", "anyway"), Set.of());
		assertEquals(Set.of(), sut.getUsedParts());
		assertEquals(Set.of("Who", "needs", "words", "anyway"), sut.getUnusedParts());
	}

	@Test
	void everythingUsed() {

		StringSetUsageStatement sut = new StringSetUsageStatement(Set.of("I", "need", "words"), Set.of("words", "I", "need"));
		assertEquals(Set.of("I", "need", "words"), sut.getUsedParts());
		assertEquals(Set.of(), sut.getUnusedParts());
	}

	@Test
	void nothingThere() {
		StringSetUsageStatement sut = new StringSetUsageStatement(Set.of(), Set.of());
		assertEquals(Set.of(), sut.getUsedParts());
		assertEquals(Set.of(), sut.getUnusedParts());
	}

}