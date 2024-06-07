package de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class LevenshteinDistanceServiceTest extends UnitTest {

	@Test
	void unlimited() {
		LevenshteinDistanceService sut = new LevenshteinDistanceService();

		assertEquals(OptionalInt.of(2), sut.apply("House", "Huose"));
		assertEquals(OptionalInt.of(1), sut.apply("Aspirin", "Apirin"));
		assertEquals(OptionalInt.of(6), sut.apply("Apple", "Firefly"));
	}

	@Test
	void limited1() {
		LevenshteinDistanceService sut = new LevenshteinDistanceService(1);

		assertEquals(OptionalInt.empty(), sut.apply("House", "Huose"));
		assertEquals(OptionalInt.of(1), sut.apply("Aspirin", "Apirin"));
		assertEquals(OptionalInt.empty(), sut.apply("Apple", "Firefly"));
	}

	@Test
	void limited2() {
		LevenshteinDistanceService sut = new LevenshteinDistanceService(2);

		assertEquals(OptionalInt.of(2), sut.apply("House", "Huose"));
		assertEquals(OptionalInt.of(1), sut.apply("Aspirin", "Apirin"));
		assertEquals(OptionalInt.empty(), sut.apply("Apple", "Firefly"));
	}

}