package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class BinarySortDirectiveTest {

	private SubSortingTree<Integer> sut;

	@BeforeEach
	public void setUp() {
		sut = new SubSortingTree<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
	}

	@Test
	public void passAll() {
		sut.applySortingStep(new BinarySortDirective<>("Allow all", v -> true, false));
		assertEquals(List.of(1,2,3,4,5,6,7,8,9), sut.getTopContents());
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void failAll(boolean eliminateNegatives) {
		sut.applySortingStep(new BinarySortDirective<>("Deny all", v -> false, eliminateNegatives));

		if (eliminateNegatives) {
			assertTrue(sut.getContents().isEmpty());
		} else {
			assertEquals(List.of(1,2,3,4,5,6,7,8,9), sut.getContents());
		}
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void onlyLessThan5(boolean eliminateNegatives) {
		sut.applySortingStep(new BinarySortDirective<>("LT5", v -> v < 5, eliminateNegatives));

		assertEquals(List.of(1,2,3,4), sut.getTopContents());
		if (eliminateNegatives) {
			assertEquals(List.of(1,2,3,4), sut.getContents());
		} else {
			assertEquals(List.of(1,2,3,4,5,6,7,8,9), sut.getContents());
		}
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void onlyEven(boolean eliminateNegatives) {
		sut.applySortingStep(new BinarySortDirective<>("LT5", v -> v % 2 == 0, eliminateNegatives));

		assertEquals(List.of(2,4,6,8), sut.getTopContents());
		if (eliminateNegatives) {
			assertEquals(List.of(2,4,6,8), sut.getContents());
		} else {
			assertEquals(List.of(2,4,6,8,1,3,5,7,9), sut.getContents());
		}
	}

}