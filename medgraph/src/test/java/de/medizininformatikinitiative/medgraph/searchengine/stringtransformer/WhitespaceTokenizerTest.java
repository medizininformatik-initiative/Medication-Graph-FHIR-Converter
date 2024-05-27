package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
		assertEquals(List.of("Our House", "in", "the middle"),
				new WhitespaceTokenizer(true).apply("\"Our House\" in \"the middle\""));
	}

	@Test
	public void quotingWithQuotingDisabled() {
		assertEquals(List.of("\"Our", "House\"", "in", "\"the", "middle\""),
				new WhitespaceTokenizer(false).apply("\"Our House\" in \"the middle\""));
	}

}