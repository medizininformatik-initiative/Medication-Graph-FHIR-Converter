package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ToLowerCaseTest {

	@Test
	public void mixed() {
		assertEquals("hallo welt,", new ToLowerCase().transform("Hallo Welt,"));
	}
	@Test
	public void lowerCase() {
		assertEquals("ich gebe nicht auf!", new ToLowerCase().transform("ich gebe nicht auf!"));
	}
	@Test
	public void upperCase() {
		assertEquals("caps lock gilt als schreien", new ToLowerCase().transform("CAPS LOCK GILT ALS SCHREIEN"));
	}
	@Test
	public void blank() {
		assertEquals("", new ToLowerCase().transform(""));
	}
}