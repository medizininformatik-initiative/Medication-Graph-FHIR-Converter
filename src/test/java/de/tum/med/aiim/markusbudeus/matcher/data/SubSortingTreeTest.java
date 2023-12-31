package de.tum.med.aiim.markusbudeus.matcher.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubSortingTreeTest {

	@Test
	public void docExample() {
		List<String> initial = List.of("4A", "2B", "3A", "2A", "4C", "1A");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);


		sut.applySortingStep(new ScoreSortDirective<>("By number", (String c) -> Integer.parseInt(c.substring(0, 1)) * 1.0, null));
		assertEquals(List.of("4A", "4C", "3A", "2B", "2A", "1A"), sut.getContents());
		System.out.println(sut.describe());
		sut.applySortingStep(new ScoreSortDirective<>("By character", c -> c.charAt(1) * 1.0, null));
		System.out.println(sut.describe());
		assertEquals(List.of("4C", "4A", "3A", "2B", "2A", "1A"), sut.getContents());
	}

	@Test
	public void docExampleWithFilter() {
		List<String> initial = List.of("4A", "2B", "3A", "2A", "4C", "1A");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		sut.applySortingStep(new ScoreSortDirective<>("By number", (String c) -> Integer.parseInt(c.substring(0, 1)) * 1.0, 3.0));
		assertEquals(List.of("4A", "4C", "3A"), sut.getContents());
		System.out.println(sut.describe());
	}

}