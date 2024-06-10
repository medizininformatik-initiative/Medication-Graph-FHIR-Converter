package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DistanceBasedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class LevenshteinMatcherTest {

	private LevenshteinMatcher sut;

	@BeforeEach
	void setUp() {
		sut = new LevenshteinMatcher();
	}

	@Test
	public void distanceZero() {
		assertEquals(0, sut.calculateDistance("Apfel", "Apfel"));
		assertEquals(0, sut.calculateDistance("", ""));
		assertEquals(0, sut.calculateDistance("House", "House"));
	}

	@Test
	public void distanceOne() {
		assertEquals(1, sut.calculateDistance("Apfel", "Aofel"));
		assertEquals(1, sut.calculateDistance("Apfel", "Afel"));
		assertEquals(1, sut.calculateDistance("Apfel", "Appfel"));
	}

	@Test
	public void distanceTwo() {
		assertEquals(2, sut.calculateDistance("Apfel", "Pafel"));
		assertEquals(2, sut.calculateDistance("Apfel", "Apppfel"));
		assertEquals(2, sut.calculateDistance("Apfel", "Appful"));
	}

	@Test
	public void overThreshold() {
		assertNull(sut.calculateDistance("Apfel", "Pafell"));
		assertNull(sut.calculateDistance("Apfel", "Apüppfel"));
		assertNull(sut.calculateDistance("Apfel", "Appaful"));
	}

	@Test
	public void matchFunctions() {
		assertNull(sut.match(new OriginalIdentifier<>("Appfüll", OriginalIdentifier.Source.RAW_QUERY),
				new MappedIdentifier<>("Apfel", TestFactory.SAMPLE_SUBSTANCE_1)));
		DistanceBasedMatch<String, String> match = sut.match(
				new OriginalIdentifier<>("Abfel", OriginalIdentifier.Source.RAW_QUERY),
				new MappedIdentifier<>("Apfel", TestFactory.SAMPLE_SUBSTANCE_1));
		assertNotNull(match);
		assertEquals(1, match.getDistance());
	}

}