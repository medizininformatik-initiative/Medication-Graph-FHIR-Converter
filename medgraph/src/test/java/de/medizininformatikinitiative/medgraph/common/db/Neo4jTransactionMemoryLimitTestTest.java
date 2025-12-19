package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class Neo4jTransactionMemoryLimitTestTest extends UnitTest {

	private Neo4jTransactionMemoryLimitTest sut;

	@BeforeEach
	void setUp() {
		sut = new Neo4jTransactionMemoryLimitTest();
	}

	@Test
	void nullSizeLimit() {
		assertTrue(sut.checkSizeLimit(null).orElse("").contains("Could not determine"));
	}

	@Test
	void unparseableSizeLimit() {
		assertTrue(sut.checkSizeLimit("100.00.00GB").orElse("").contains("Could not determine"));
	}

	@Test
	void gibSizeLimit() {
		assertTrue(sut.checkSizeLimit("3GiB").isEmpty());
	}

	@Test
	void mibSizeLimit() {
		assertTrue(sut.checkSizeLimit("6192MiB").isEmpty());
	}

	@Test
	void exactLimit1() {
		assertTrue(sut.checkSizeLimit("2048MiB").isEmpty());
	}
	@Test
	void exactLimit2() {
		assertTrue(sut.checkSizeLimit("2GiB").isEmpty());
	}

	@Test
	void tooLow1() {
		assertTrue(sut.checkSizeLimit("2047MiB").orElse("").contains("below the recommended limit"));
	}

	@Test
	void tooLow2() {
		assertTrue(sut.checkSizeLimit("1.99GB").orElse("").contains("below the recommended limit"));
	}

}