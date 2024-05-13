package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ToLowerCaseTest {

	@Test
	public void mixed() {
		assertEquals("hallo welt,", new ToLowerCase().apply("Hallo Welt,"));
	}
	@Test
	public void lowerCase() {
		assertEquals("ich gebe nicht auf!", new ToLowerCase().apply("ich gebe nicht auf!"));
	}
	@Test
	public void upperCase() {
		assertEquals("caps lock gilt als schreien", new ToLowerCase().apply("CAPS LOCK GILT ALS SCHREIEN"));
	}
	@Test
	public void blank() {
		assertEquals("", new ToLowerCase().apply(""));
	}
}