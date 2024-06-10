package de.medizininformatikinitiative.medgraph.searchengine.tools;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class SearchEngineToolsTest extends UnitTest {

	@Test
	void removeConflictingOverlaps() {
		List<String> inputList = new ArrayList<>(
				List.of("Apfel", "Banane", "Auster", "Blutorange", "Ananas", "Grapefruit", "Banane", "Auster"));
		SearchEngineTools.removeConflictingOverlaps(inputList, this::firstCharEqual, this::keepSmaller);
		assertEquals(List.of("Banane", "Ananas", "Grapefruit", "Banane"), inputList);
	}

	@Test
	void removeConflictingOverlaps2() {
		List<String> inputList = new ArrayList<>(
				List.of("Marmelade", "Brot", "Käse", "Kaugummi", "Kuchen", "Butter"));
		SearchEngineTools.removeConflictingOverlaps(inputList, this::firstCharEqual, this::keepSmaller);
		assertEquals(List.of("Marmelade", "Brot", "Kaugummi"), inputList);
	}


	@Test
	void removeConflictingOverlapsEmpty() {
		List<String> inputList = new ArrayList<>();
		SearchEngineTools.removeConflictingOverlaps(inputList, this::firstCharEqual, this::keepSmaller);
		assertEquals(List.of(), inputList);
	}

	private boolean firstCharEqual(String s1, String s2) {
		return s1.charAt(0) == s2.charAt(0);
	}

	private SearchEngineTools.OverlapResolutionStrategy keepSmaller(String s1, String s2) {
		if (s1.compareTo(s2) < 0) return SearchEngineTools.OverlapResolutionStrategy.KEEP_FIRST;
		else if (s1.compareTo(s2) > 0) return SearchEngineTools.OverlapResolutionStrategy.KEEP_SECOND;
		else return SearchEngineTools.OverlapResolutionStrategy.KEEP_BOTH;
	}


}