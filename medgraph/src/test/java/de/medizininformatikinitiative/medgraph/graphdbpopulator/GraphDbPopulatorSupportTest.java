package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphDbPopulatorSupportTest {

	/**
	 * For example, this is allowed:<br>
	 * <code>"ABCD";"TEST"</code><br>
	 * <code>ABCD;TEST</code><br>
	 * <code>ABCD;"TE;ST"</code> (in this case, the second column value is <code>TE;ST</code>)<br>
	 * <code>AB"CD;"TE;ST"</code> (in this case, the first column value is <code>AB"CD</code>)<br>
	 * But this is not:<br>
	 * <code>ABCD;"T"EST"</code>
	 */


	@Test
	void isValidCsvLine1() {
		assertTrue(GraphDbPopulatorSupport.isValidCsvLine("\"ABCD\";\"TEST\""));
	}

	@Test
	void isValidCsvLine2() {
		assertTrue(GraphDbPopulatorSupport.isValidCsvLine("ABCD;TEST"));
	}

	@Test
	void isValidCsvLine3() {
		assertTrue(GraphDbPopulatorSupport.isValidCsvLine(";;Aa;"));
	}

	@Test
	void isValidCsvLine4() {
		assertTrue(GraphDbPopulatorSupport.isValidCsvLine("AB\"CD;\"TE;ST\""));
	}

	@Test
	void isValidCsvLine5() {
		assertFalse(GraphDbPopulatorSupport.isValidCsvLine("ABCD;\"T\"EST\""));
	}

	@Test
	void isValidCsvLine6() {
		assertFalse(GraphDbPopulatorSupport.isValidCsvLine("ABCD;\"INCOMPLETE LINE"));
	}

}