package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class StepwiseGeneralQueryParserTest extends UnitTest {

	@Test
	public void initialState() {
		String s = "Earth, Wind and Fire";
		StepwiseGeneralQueryParser sut = new StepwiseGeneralQueryParser(s);
		assertEquals(s, sut.getOriginalQuery());
		assertEquals(new DistinctMultiSubstringUsageStatement(s, Collections.emptySet()),
				sut.getQueryUsageStatement());
	}

	@Test
	public void singleUse() {
		String s = "Luke, it's a trap!";
		StepwiseGeneralQueryParser sut = new StepwiseGeneralQueryParser(s);
		SubstringUsageStatement usageStatement = sut.useRemainingQueryParts(value -> {
			assertEquals(s, value);
			return new SingleSubstringUsageStatement(s, new IntRange(0, 4));
		});

		assertEquals(Set.of(new IntRange(0, 4)), usageStatement.getUsedRanges());
		assertEquals(s, usageStatement.getOriginal());
		assertEquals(s, sut.getOriginalQuery());
		assertEquals(", it's a trap!", sut.getQueryUsageStatement().getUnusedParts());
	}

	@Test
	public void doubleUse() {
		String s = "Dirty deeds done dirt cheap";
		StepwiseGeneralQueryParser sut = new StepwiseGeneralQueryParser(s);
		sut.useRemainingQueryParts(value -> new SingleSubstringUsageStatement(value, new IntRange(0, 12)));

		SubstringUsageStatement usageStatement = sut.useRemainingQueryParts(value -> {
			assertEquals("done dirt cheap", value);
			// We use "done " and "cheap"
			return new MultiSubstringUsageStatement(value, Set.of(new IntRange(0, 5), new IntRange(10, 15)));
		});

		assertEquals(s, usageStatement.getOriginal());
		assertEquals("Dirty deeds dirt ", usageStatement.getUnusedParts());
		assertEquals(Set.of(new IntRange(12, 17), new IntRange(22, 27)), usageStatement.getUsedRanges());

		assertEquals("dirt ", sut.getQueryUsageStatement().getUnusedParts());
	}

	@Test
	public void tripleUse() {
		String s = "Ice Ice Baby love";
		StepwiseGeneralQueryParser sut = new StepwiseGeneralQueryParser(s);
		// Remove first "Ice "
		sut.useRemainingQueryParts(value -> new SingleSubstringUsageStatement(value, new IntRange(0, 4)));
		// Remove second "Ice " and " love"
		sut.useRemainingQueryParts(value -> new DistinctMultiSubstringUsageStatement(value,
				Set.of(new IntRange(0, 4), new IntRange(8, 13))));

		SubstringUsageStatement usageStatement = sut.useRemainingQueryParts(value -> {
			assertEquals("Baby", value);
			return new DistinctMultiSubstringUsageStatement(value, Set.of());
		});

		assertEquals("", usageStatement.getUsedParts());
		assertEquals(s, usageStatement.getUnusedParts());
		assertEquals("Ice Ice  love", sut.getQueryUsageStatement().getUsedParts());
		assertEquals("Baby", sut.getQueryUsageStatement().getUnusedParts());
	}

	@Test
	public void misusedFunction() {
		String s = "Wonderwall";
		StepwiseGeneralQueryParser sut = new StepwiseGeneralQueryParser(s);
		assertThrows(IllegalStateException.class, () -> {
			// Invalid String usage statement returned!
			sut.useRemainingQueryParts(value -> new SingleSubstringUsageStatement("Wonderfull", new IntRange(0, 4)));
		});
	}

}