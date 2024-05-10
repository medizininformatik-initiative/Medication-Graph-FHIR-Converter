package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ScoreSortDirectiveTest {

	private SubSortingTree<Integer> sut;

	@BeforeEach
	public void setUp() {
		sut = new SubSortingTree<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
	}

	@Test
	public void divideBy2() {
		sut.applySortingStep(new ScoreSortDirective<>("Div2",
				v -> (v / 2) * 1.0d, null));

		assertEquals(List.of(8, 9), sut.getTopContents());
		assertEquals(List.of(8, 9, 6, 7, 4, 5, 2, 3, 1), sut.getContents());
	}

	@Test
	public void divideBy2WithElimination() {
		sut.applySortingStep(new ScoreSortDirective<>("Div2",
				v -> (v / 2) * 1.0d, 2.0));

		assertEquals(List.of(8, 9), sut.getTopContents());
		assertEquals(List.of(8, 9, 6, 7, 4, 5), sut.getContents());
	}

	@Test
	public void eliminateAll() {
		sut.applySortingStep(new ScoreSortDirective<>("Div2",
				v -> (v / 2) * 1.0d, 5.0));

		assertTrue(sut.getContents().isEmpty());
	}

	@Test
	public void order() {
		sut = new SubSortingTree<>(List.of(8,4,5,7,3,1,6,2,9));
		sut.applySortingStep(new ScoreSortDirective<>("Div2",
				v -> -1.0 * v, null));

		assertEquals(List.of(1,2,3,4,5,6,7,8,9), sut.getContents());
	}

}