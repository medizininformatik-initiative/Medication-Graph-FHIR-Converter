package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class LevenshteinSetMatcherTest {

	private LevenshteinSetMatcher sut;

	@BeforeEach
	void setUp() {
		sut = new LevenshteinSetMatcher();
	}

	@Test
	public void perfectMatch() {
		// House -> House = 1 Pt.
		// Party -> Party = 1 Pt.
		// 2 Pt. / 2 entries = 1.0
		assertEquals(1.0, sut.calculateScore(
				Set.of("House" , "Party"),
				Set.of("Party", "House")
		), 0.01);
	}

	@Test
	public void limitedMatch() {
		// House -> House = 1 Pt.
		// Home -> House = 0.33 Pt.
		// 1,33 Pt. / 2 entries = 0.667
		assertEquals(0.667, sut.calculateScore(
				Set.of("House" , "Home"),
				Set.of("Party", "House")
		), 0.01);
	}

	@Test
	public void excessWord() {
		// House -> House = 1 Pt.
		// Party -> Party = 1 Pt.
		// 2 Pt. / 3 entries = 1.0
		assertEquals(0.667, sut.calculateScore(
				Set.of("House" , "Wife", "Party"),
				Set.of("Party", "House")
		), 0.01);
	}

	@Test
	public void withSpellingErrors() {
		// Huose -> House = 0.33
		// 0.33 / 1 entry = 0.33
		assertEquals(0.333, sut.calculateScore(
				Set.of("Huose"),
				Set.of("Party", "House")
		), 0.01);
	}
}