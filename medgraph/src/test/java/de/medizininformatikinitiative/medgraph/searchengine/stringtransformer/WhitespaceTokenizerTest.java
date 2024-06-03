package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Markus Budeus
 */
public class WhitespaceTokenizerTest extends UnitTest {

	@ParameterizedTest(name = "quotingEnabled: {0}")
	@ValueSource(booleans = {false, true})
	public void simpleTokenize(boolean quotingEnabled) {
		assertEquals(List.of("Our", "House", "in", "the", "middle"),
				new WhitespaceTokenizer(quotingEnabled).apply("Our House in the middle"));
	}

	@ParameterizedTest(name = "quotingEnabled: {0}")
	@ValueSource(booleans = {false, true})
	public void blankStringsWouldRemain(boolean quotingEnabled) {
		assertEquals(List.of("of", "our", "street,", "our", "House"),
				new WhitespaceTokenizer(quotingEnabled).apply("of  our street, \t our House"));
	}

	@ParameterizedTest(name = "quotingEnabled: {0}")
	@ValueSource(booleans = {false, true})
	public void emptyString(boolean quotingEnabled) {
		assertEquals(Collections.emptyList(), new WhitespaceTokenizer(quotingEnabled).apply(""));
	}

	@Test
	public void quoting() {
		assertEquals(List.of("Holy", "Hand Grenade"),
				new WhitespaceTokenizer(true).apply("Holy \"Hand Grenade\""));
	}

	@Test
	public void quotingWithQuotingDisabled() {
		assertEquals(List.of("Holy", "\"Hand", "Grenade\""),
				new WhitespaceTokenizer(false).apply("Holy \"Hand Grenade\""));
	}

	@Test
	public void unclosedQuote() {
		assertEquals(List.of("Our House", "in", "the middle"),
				new WhitespaceTokenizer(true).apply("\"Our House\" in \"the middle"));
	}

	@ParameterizedTest(name = "quotingEnabled: {0}")
	@ValueSource(booleans = {false, true})
	public void reverseTransformSimpleTokenize(boolean quotingEnabled) {
		String input = "Our House in the middle";
		DistinctMultiSubstringUsageStatement usageStatement =
				new WhitespaceTokenizer(quotingEnabled).reverseTransformUsageStatement(
						input,
						new StringListUsageStatement(
								List.of("Our", "House", "in", "the", "middle"),
								Set.of(1, 4)
						)
				);
		assertEquals(new DistinctMultiSubstringUsageStatement(input, Set.of(
				new IntRange(4, 10), new IntRange(17, 23)
		)), usageStatement);
	}

	@ParameterizedTest(name = "quotingEnabled: {0}")
	@ValueSource(booleans = {false, true})
	public void reverseTransformBlankStringsWouldRemain(boolean quotingEnabled) {
		String input = "of  our street, \t our House";
		DistinctMultiSubstringUsageStatement usageStatement =
				new WhitespaceTokenizer(quotingEnabled).reverseTransformUsageStatement(
						input,
						new StringListUsageStatement(
								List.of("of", "our", "street,", "our", "House"),
								Set.of(0, 1, 2, 3)
						)
				);
		assertEquals(new DistinctMultiSubstringUsageStatement(input, Set.of(
				new IntRange(0, 3), new IntRange(4, 8), new IntRange(8, 16), new IntRange(18, 22)
		)), usageStatement);
	}

	@ParameterizedTest(name = "quotingEnabled: {0}")
	@ValueSource(booleans = {false, true})
	public void reverseTransformEmptyString(boolean quotingEnabled) {
		String input = "";
		DistinctMultiSubstringUsageStatement usageStatement =
				new WhitespaceTokenizer(quotingEnabled).reverseTransformUsageStatement(
						input,
						new StringListUsageStatement(List.of(), Set.of())
				);
		assertEquals(new DistinctMultiSubstringUsageStatement(input, Set.of()), usageStatement);
	}

	@Test
	public void reverseTransformQuoting() {
		String input = "Holy \"Hand Grenade\"";
		DistinctMultiSubstringUsageStatement usageStatement =
				new WhitespaceTokenizer(true).reverseTransformUsageStatement(
						input,
						new StringListUsageStatement(
								List.of("Holy", "Hand Grenade"),
								Set.of(1)
						)
				);
		assertEquals(new DistinctMultiSubstringUsageStatement(input,
				Set.of(new IntRange(5, 19))), usageStatement);
	}

	@Test
	public void reverseTransformQuotingWithQuotingDisabled() {
		String input = "Holy \"Hand Grenade\"";
		DistinctMultiSubstringUsageStatement usageStatement =
				new WhitespaceTokenizer(false).reverseTransformUsageStatement(
						input,
						new StringListUsageStatement(
								List.of("Holy", "\"Hand", "Grenade\""),
								Set.of(1)
						)
				);
		assertEquals(new DistinctMultiSubstringUsageStatement(input,
				Set.of(new IntRange(5, 11))), usageStatement);
	}

	@Test
	public void reverseTransformUnclosedQuote() {
		String input = "\"Our House\" in \"the middle";
		DistinctMultiSubstringUsageStatement usageStatement =
				new WhitespaceTokenizer(true).reverseTransformUsageStatement(
						input,
						new StringListUsageStatement(
								List.of("Our House", "in", "the middle"),
								Set.of(2)
						)
				);
		assertEquals(new DistinctMultiSubstringUsageStatement(input,
				Set.of(new IntRange(15, 26))), usageStatement);
	}

	@ParameterizedTest(name = "quotingEnabled: {0}")
	@ValueSource(booleans = {false, true})
	public void invalidReverseTransformation(boolean quotingEnabled) {
		String input = "\"Our House\" in \"the middle";
		assertThrows(IllegalArgumentException.class, () -> {
				new WhitespaceTokenizer(quotingEnabled).reverseTransformUsageStatement(
						input,
						new StringListUsageStatement(
								List.of("Our", "House", "in", "the", "middle"),
								Set.of(2)
						)
				);
		});
	}
}