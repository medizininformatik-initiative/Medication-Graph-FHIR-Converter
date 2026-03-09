package de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.strengths;

import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Unit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class MyUnitTest {

	@Test
	public void multiply1() {
		assertEquals(new Unit("mg", null),
				new Unit("mg", "ml").multiply(new Unit("ml", null)));
	}

	@Test
	public void multiply2() {
		assertEquals(new Unit("mg", "kg"),
				new Unit("mg", "d").multiply(new Unit("d", "kg")));
	}

	@Test
	public void divide1() {
		assertEquals(new Unit("mg", "ml"),
				new Unit("mg", null).divide(new Unit("ml", null)));
	}

	@Test
	public void divide2() {
		assertEquals(new Unit("mg", "dl"),
				new Unit("mg", null).divide(new Unit("dl", null)));
	}

}