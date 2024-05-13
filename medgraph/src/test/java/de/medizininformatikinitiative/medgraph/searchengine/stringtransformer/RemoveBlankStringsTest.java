package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class RemoveBlankStringsTest {

	@Test
	public void someBlankStrings() {
		assertEquals(List.of("Apfel", "Haus", "-"),
				new RemoveBlankStrings().transform(List.of("", "Apfel", "", "Haus", "-")));
	}

	@Test
	public void onlyBlankStrings() {
		assertEquals(List.of(),
				new RemoveBlankStrings().transform(List.of("", "", "")));
	}

	@Test
	public void emptyList() {
		assertEquals(List.of(),
				new RemoveBlankStrings().transform(List.of()));
	}

	@Test
	public void noBlankStrings() {
		assertEquals(List.of("Marie", "Eisenhower", "Burkhardt", "Neverwinter"),
				new RemoveBlankStrings().transform(List.of("Marie", "Eisenhower", "Burkhardt", "Neverwinter")));
	}

}