package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class SubSortingTreeTest {

	@Test
	public void docExample() {
		List<String> initial = List.of("4A", "2B", "3A", "2A", "4C", "1A");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);


		sut.applySortingStep(
				new ScoreSortDirective<>("By number", (String c) -> Integer.parseInt(c.substring(0, 1)) * 1.0, null));
		assertEquals(List.of("4A", "4C", "3A", "2B", "2A", "1A"), sut.getContents());
		sut.applySortingStep(new ScoreSortDirective<>("By character", c -> c.charAt(1) * 1.0, null));
		assertEquals(List.of("4C", "4A", "3A", "2B", "2A", "1A"), sut.getContents());
	}

	@Test
	public void docExampleWithFilter() {
		List<String> initial = List.of("4A", "2B", "3A", "2A", "4C", "1A");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		sut.applySortingStep(
				new ScoreSortDirective<>("By number", (String c) -> Integer.parseInt(c.substring(0, 1)) * 1.0, 3.0));
		assertEquals(List.of("4A", "4C", "3A"), sut.getContents());
	}

	@Test
	public void getTopContents() {
		List<String> initial = List.of("1", "2", "3", "1", "3");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		assertEquals(initial, sut.getTopContents());

		sut.applySortingStep(new BinarySortDirective<>("Only ones",
				s -> s.equals("1"),
				false));

		assertEquals(List.of("1", "1"), sut.getTopContents());
	}

	@Test
	public void getTopContents2() {
		List<String> initial = List.of("1", "2", "3", "1", "3");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		assertEquals(initial, sut.getTopContents());

		sut.applySortingStep(
				new ScoreSortDirective<>("Lowest first",
						m -> -1D * Integer.parseInt(m),
						null)
		);

		assertEquals(List.of("1", "1"), sut.getTopContents());
	}

	@Test
	public void clearDuplicates() {
		List<Integer> initial = List.of(1, 7, 1, 2, 3, 4, 3);
		SubSortingTree<Integer> sut = new SubSortingTree<>(initial);

		sut.clearDuplicates();

		assertEquals(List.of(1, 7, 2, 3, 4), sut.getContents());
	}

	@Test
	public void batchReplace() {
		List<String> initial = List.of("A", "B", "A", "K", "L", "B", "B");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		Map<String, List<String>> replaceMap = new HashMap<>();
		replaceMap.put("B", List.of("C"));
		replaceMap.put("K", List.of("X", "Y", "Z"));

		sut.batchReplace(replaceMap);

		assertEquals(List.of("A", "C", "A", "X", "Y", "Z", "L", "C", "C"), sut.getContents());
	}

	@Test
	public void noTransisitveReplace1() {
		List<String> initial = List.of("A", "B", "C");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		Map<String, List<String>> replaceMap = new HashMap<>();
		replaceMap.put("A", List.of("B"));
		replaceMap.put("B", List.of("C"));

		sut.batchReplace(replaceMap);
		assertEquals(List.of("B", "C", "C"), sut.getContents());
	}

	@Test
	public void noTransisitveReplace2() {
		List<String> initial = List.of("A", "B", "C");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		Map<String, List<String>> replaceMap = new HashMap<>();
		replaceMap.put("A", List.of("A", "A"));

		sut.batchReplace(replaceMap);
		assertEquals(List.of("A", "A", "B", "C"), sut.getContents());
	}

	@Test
	public void forEach() {
		List<String> initial = List.of("4A", "2B", "3A", "2A", "4C", "1A");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		sut.applySortingStep(
				new ScoreSortDirective<>("By number", (String c) -> Integer.parseInt(c.substring(0, 1)) * 1.0, null));

		List<String> outList = new ArrayList<>();
		sut.forEach(s -> outList.add(s + "1"));
		assertEquals(List.of("4A1", "4C1", "3A1", "2B1", "2A1", "1A1"), outList);
	}

	@Test
	public void sortAndBatchReplaceAndClearDuplicates() {
		List<String> initial = List.of("A1", "A2", "B1", "C3", "B4", "B2", "A3", "C2", "B3", "C4");
		SubSortingTree<String> sut = new SubSortingTree<>(initial);

		sut.applySortingStep(new ScoreSortDirective<>("By number ascending",
				s -> Integer.parseInt(s.substring(1)) * -1.0,
				-3.0
		));

		assertEquals(List.of("A1", "B1", "A2", "B2", "C2", "C3", "A3", "B3"), sut.getContents());

		Map<String, List<String>> replacementMap = new HashMap<>();
		replacementMap.put("A1", List.of("B2"));
		replacementMap.put("C2", List.of("A1"));
		replacementMap.put("C3", List.of("B1", "B3"));
		sut.batchReplace(replacementMap);

		assertEquals(List.of("B2", "B1", "A2", "B2", "A1", "B1", "B3", "A3", "B3"), sut.getContents());

		sut.clearDuplicates();

		assertEquals(List.of("B2", "B1", "A2", "A1", "B3", "A3"), sut.getContents());
	}

	@Test
	public void merge() {
		List<Integer> initial1 = List.of(1, 1, 3, 2, 5, 4);
		List<Integer> initial2 = List.of(61, 33, 73, 41, 1, 82, 94, 14, 24);

		SubSortingTree<Integer> tree1 = new SubSortingTree<>(initial1);
		SubSortingTree<Integer> tree2 = new SubSortingTree<>(initial2);

		tree2.applySortingStep(new ScoreSortDirective<>("last digit descending",
				value -> (double) (value % 10), null));
		assertEquals(List.of(94, 14, 24, 33, 73, 82, 61, 41, 1), tree2.getContents());

		SubSortingTree<Integer> merge = SubSortingTree.merge(tree1, tree2);
		assertEquals(List.of(
				1, 1, 3, 2, 5, 4,
				94, 14, 24, 33, 73, 82, 61, 41, 1
		), merge.getContents());

		merge.applySortingStep(new ScoreSortDirective<>("number ascending",
				value -> (double) -value, null));
		assertEquals(List.of(
				1, 1, 2, 3, 4, 5,
				14, 24, 94, 33, 73, 82, 1, 41, 61
		), merge.getContents());
		assertEquals(List.of(1, 1), merge.getTopContents());
	}

}