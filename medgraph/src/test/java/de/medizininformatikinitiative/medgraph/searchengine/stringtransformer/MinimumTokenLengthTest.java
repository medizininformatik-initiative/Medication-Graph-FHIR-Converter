package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class MinimumTokenLengthTest extends UnitTest {

	@Test
	void transform() {
		MinimumTokenLength sut = new MinimumTokenLength(2);
		assertEquals(List.of("House", "Tar", "F ame"), sut.apply(List.of("House", "W", "Tar", " ", "", "F ame")));
	}

	@Test
	void transformEmpty() {
		MinimumTokenLength sut = new MinimumTokenLength(2);
		assertEquals(List.of(), sut.apply(List.of()));
	}

	@Test
	void reverseTransform() {
		MinimumTokenLength sut = new MinimumTokenLength(2);
		List<String> input = List.of("House", "W", "Tar", " ", "", "F ame");

		StringListUsageStatement usageStatement = sut.reverseTransformUsageStatement(
				input,
				new StringListUsageStatement(List.of("House", "Tar", "F ame"), Set.of(1, 2))
		);
		assertEquals(new StringListUsageStatement(input, Set.of(2, 5)), usageStatement);
	}

	@Test
	void reverseTransfromEmpty() {
		MinimumTokenLength sut = new MinimumTokenLength(1);
		List<String> input = List.of();

		StringListUsageStatement usageStatement = sut.reverseTransformUsageStatement(
				input,
				new StringListUsageStatement(List.of(), Set.of())
		);
		assertEquals(new StringListUsageStatement(input, Set.of()), usageStatement);
	}

	@Test
	void reverseTransformInvalid() {
		MinimumTokenLength sut = new MinimumTokenLength(1);
		List<String> input = List.of("House", "W", "Tar", " ", "", "F ame");

		assertThrows(IllegalArgumentException.class, () -> {
			sut.reverseTransformUsageStatement(
					input,
					new StringListUsageStatement(List.of("House", "Tar", "F ame"), Set.of(1, 2))
			);
		});
	}

}