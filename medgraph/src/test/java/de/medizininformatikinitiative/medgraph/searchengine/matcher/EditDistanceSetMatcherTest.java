package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.EditDistance;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class EditDistanceSetMatcherTest {

	private EditDistanceSetMatcher sut;

	@BeforeEach
	void setUp() {
		sut = new EditDistanceSetMatcher(new LevenshteinDistanceService(2));
	}

	@Test
	public void perfectMatch() {
		// House -> House = 1 Pt.
		// Party -> Party = 1 Pt.
		// 2 Pt. / 2 entries = 1.0
		test(
				Set.of("House" , "Party"),
				Set.of("Party", "House"),
				1.0,
				Set.of(
						new EditDistance("House", "House", 0),
						new EditDistance("Party", "Party", 0)
				),
				Set.of("House" , "Party")
		);
	}

	@Test
	public void limitedMatch() {
		// House -> House = 1 Pt.
		// Home -> House = 0.33 Pt.
		// 1,33 Pt. / 2 entries = 0.667
		test(
				Set.of("House" , "Home"),
				Set.of("Party", "House"),
				0.667,
				Set.of(
						new EditDistance("House", "House", 0),
						new EditDistance("Home", "House", 2)
				),
				Set.of("House", "Home")
		);
	}

	@Test
	public void excessWord() {
		// House -> House = 1 Pt.
		// Party -> Party = 1 Pt.
		// 2 Pt. / 3 entries = 0.667
		test(
				Set.of("House" , "Wife", "Party"),
				Set.of("Party", "House"),
				0.667,
				Set.of(
						new EditDistance("House", "House", 0),
						new EditDistance("Party", "Party", 0)
				),
				Set.of("House", "Party")
		);
	}

	@Test
	public void withSpellingErrors() {
		// Huose -> House = 0.33
		// 0.33 / 1 entry = 0.33
		test(
				Set.of("Huose"),
				Set.of("Party", "House"),
				0.33,
				Set.of(
						new EditDistance("Huose", "House", 2)
				),
				Set.of("Huose")
		);
	}

	@Test
	public void noMatch() {
		testNoMatch(
				Set.of("Apple", "Maniac"),
				Set.of("Flower", "Boredom")
		);
	}

	@Test
	public void lotsOfInput() {
		// Huose -> House = 0.33
		// 0.33 / 1 entry = 0.33
		test(
				Set.of("Dormicum", "V", "5 mg/ml", "Bayer", "GmbH", "Midazolam"),
				Set.of("Midazolam"),
				0.167,
				Set.of(
						new EditDistance("Midazolam", "Midazolam", 0)
				),
				Set.of("Midazolam")
		);
	}

	private void testNoMatch(
			Set<String> searchTerm,
			Set<String> targetIdentifier
	) {
		test(searchTerm, targetIdentifier, 0, null, null);
	}

	private void test(
			Set<String> searchTerm,
			Set<String> targetIdentifier,
			double expectedScore,
			Set<EditDistance> expectedEditDistances,
			Set<String> expectedUsedStrings
	) {
		OriginalIdentifier<Set<String>> stIdentifier = new OriginalIdentifier<>(searchTerm, OriginalIdentifier.Source.RAW_QUERY);
		EditDistanceSetMatcher.Match match = sut.match(stIdentifier, new MappedIdentifier<>(targetIdentifier, TestFactory.SAMPLE_SUBSTANCE_1));
		if (expectedScore == 0) {
			assertNull(match);
		} else {
			assertNotNull(match);
			assertSame(stIdentifier, match.getSearchTerm());
			assertEquals(expectedScore, match.getScore(), 0.01);
			if (expectedUsedStrings != null) {
				assertEquals(expectedUsedStrings, match.getUsageStatement().getUsedParts());
			}
			if (expectedEditDistances != null) {
				assertEquals(expectedEditDistances, new HashSet<>(match.getEditDistances()));
			}
		}
	}

}