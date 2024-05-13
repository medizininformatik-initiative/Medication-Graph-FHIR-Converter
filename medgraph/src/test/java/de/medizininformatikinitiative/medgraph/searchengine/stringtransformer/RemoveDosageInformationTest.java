package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class RemoveDosageInformationTest {

	private RemoveDosageInformation sut;

	@BeforeEach
	void setUp() {
		sut = new RemoveDosageInformation();
	}

	@Test
	public void noDosageInfo() {
		assertEquals("Hello world", sut.apply("Hello world"));
	}

	@Test
	public void someDosageInfo() {
		assertEquals("Hello  World", sut.apply("Hello 10 mg World"));
	}

	@Test
	public void lotsaDosageInfo() {
		assertEquals("Hello  World with  ", sut.apply("Hello 10 mg World with 0.1mg/ml "));
	}
}