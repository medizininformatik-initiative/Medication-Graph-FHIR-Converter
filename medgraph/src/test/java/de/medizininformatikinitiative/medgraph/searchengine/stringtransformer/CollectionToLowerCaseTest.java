package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class CollectionToLowerCaseTest extends UnitTest {


	@Test
	public void lowerCase() {
		assertEquals("ich gebe nicht auf!", new ToLowerCase().apply("ich gebe nicht auf!"));
	}

	@Test
	public void upperCase() {
		assertEquals("caps lock gilt als schreien", new ToLowerCase().apply("CAPS LOCK GILT ALS SCHREIEN"));
	}

	@Test
	public void singleEntry() {
		assertEquals(List.of("hallo welt,"), new CollectionToLowerCase<>().apply(Set.of("Hallo Welt,")));
	}

	@Test
	public void multipleEntriesList() {
		assertEquals(List.of("this is fine", "this is fine", "this is still fine"),
				new CollectionToLowerCase<>().apply(List.of("this is fine", "This is Fine", "THIS IS STILL FINE")));
	}

	@Test
	public void multipleEntriesSet() {
		assertEquals(Set.of("waterloo", "what-to_do;with;special,chars?"),
				new HashSet<>(new CollectionToLowerCase<>().apply(
						Set.of("Waterloo", "What-to_do;WIth;Special,Chars?"))));
	}

	@Test
	public void blank() {
		assertEquals("", new ToLowerCase().apply(""));
	}

	@Test
	public void emptyList() {
		assertEquals(Collections.emptyList(), new CollectionToLowerCase<>().apply(Collections.emptyList()));
	}

}