package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringSetUsageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ListToSetTest extends UnitTest {

	private ListToSet sut;

	@BeforeEach
	void setUp() {
		sut = new ListToSet();
	}

	@Test
	void reverseTransformBasic() {
		List<String> original = List.of("Water", "Fire", "Earth", "Air");
		StringListUsageStatement usageStatement = sut.reverseTransformUsageStatement(
				original,
				new StringSetUsageStatement(
						new HashSet<>(original),
						Set.of("Water", "Air")
				)
		);
		assertEquals(new StringListUsageStatement(original, Set.of(0, 3)), usageStatement);
	}

	@Test
	void orderingPreserved() {
		StringSetUsageStatement setUsageStatement = new StringSetUsageStatement(
				Set.of("Water", "Fire", "Earth", "Air"),
				Set.of("Fire", "Air", "Earth")
		);

		List<String> original1 = List.of("Water", "Fire", "Earth", "Air");
		List<String> original2 = List.of("Earth", "Fire", "Water", "Air");
		List<String> original3 = List.of("Air", "Water", "Earth", "Fire");
		StringListUsageStatement usageStatement1 = sut.reverseTransformUsageStatement(original1, setUsageStatement);
		StringListUsageStatement usageStatement2 = sut.reverseTransformUsageStatement(original2, setUsageStatement);
		StringListUsageStatement usageStatement3 = sut.reverseTransformUsageStatement(original3, setUsageStatement);
		assertEquals(new StringListUsageStatement(original1, Set.of(1, 2, 3)), usageStatement1);
		assertEquals(new StringListUsageStatement(original2, Set.of(0, 1, 3)), usageStatement2);
		assertEquals(new StringListUsageStatement(original3, Set.of(0, 2, 3)), usageStatement3);
	}

	@Test
	void reverseTransformEmpty() {
		List<String> original = List.of();
		StringListUsageStatement usageStatement = sut.reverseTransformUsageStatement(
				original,
				new StringSetUsageStatement(
						Set.of(),
						Set.of()
				)
		);
		assertEquals(new StringListUsageStatement(original, Set.of()), usageStatement);
	}

	@Test
	void reverseTransformWithDuplicates() {
		List<String> original = List.of("Water", "Fire", "Water", "Earth", "Air", "Earth");
		StringListUsageStatement usageStatement = sut.reverseTransformUsageStatement(
				original,
				new StringSetUsageStatement(
						new HashSet<>(original),
						Set.of("Water", "Air")
				)
		);
		assertEquals(new StringListUsageStatement(original, Set.of(0, 2, 4)), usageStatement);
	}

	@Test
	void performsValidityCheck() {
		List<String> original = List.of("Water", "Fire", "Earth", "Air");
		assertThrows(IllegalArgumentException.class, () -> {
			sut.reverseTransformUsageStatement(
					original,
					new StringSetUsageStatement(
							Set.of("Water", "Fire", "Air"), // Not a valid output of the original list
							Set.of("Water", "Air")
					)
			);
		});
	}

}