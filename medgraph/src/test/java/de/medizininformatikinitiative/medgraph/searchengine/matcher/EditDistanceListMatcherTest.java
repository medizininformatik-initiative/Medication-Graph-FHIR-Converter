package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.EditDistance;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class EditDistanceListMatcherTest extends UnitTest {

	private final EditDistanceListMatcher sut = new EditDistanceListMatcher(new LevenshteinDistanceService(1));

	@Test
	void simpleExample() {
		test(List.of("Amoxicillin", "Infusionslösung"),
				List.of("Infusionslösung"),
				new EditDistance("Infusionslösung", "Infusionslösung", 0),
				Set.of(1));
	}

	@Test
	void simpleExampleWithSpellingError() {
		test(List.of("Amoxicillin", "Infusionslosung"),
				List.of("Infusionslösung"),
				new EditDistance("Infusionslosung", "Infusionslösung", 1),
				Set.of(1));
	}

	@Test
	void multiWordExample() {
		test(List.of("Amoxicillin", "Gran.", "zum", "Einnehmen"),
				List.of("Gran.", "zum", "Einnehmen"),
				new EditDistance("Gran. zum Einnehmen", "Gran. zum Einnehmen", 0),
				Set.of(1, 2, 3));
	}

	@Test
	void multiWordExampleWithSpellingError() {
		test(List.of("Amoxicillin", "Gran.", "zum", "Einehmen"),
				List.of("Gran.", "zum", "Einnehmen"),
				new EditDistance("Gran. zum Einehmen", "Gran. zum Einnehmen", 1),
				Set.of(1, 2, 3));
	}

	@Test
	void tooManySpellingErrors() {
		test(List.of("Amoxicillin", "Gran.", "z.", "Einehmen"),
				List.of("Gran.", "zum", "Einnehmen"),
				null,
				null);
	}

	@Test
	void bestMatchLater() {
		test(List.of("Amoxicillin", "Inj-Lsg.", "Thiamin", "Inj.-Lsg."),
				List.of("Inj.-Lsg."),
				new EditDistance("Inj.-Lsg.", "Inj.-Lsg.", 0),
				Set.of(3));
	}

	@Test
	void multipleEquallyGoodMatches() {
		test(List.of("Tranexamsäure", "Tablette", "Thiamin", "Tablette"),
				List.of("Tablette"),
				new EditDistance("Tablette", "Tablette", 0),
				Set.of(1));
	}

	@Test
	void noMatch() {
		test(List.of("Tranexamsäure", "Tablette", "Thiamin", "Tablette"),
				List.of("Infusionslösung"),
				null,
				null);
	}

	@Test
	void searchTermTooShort() {
		test(List.of("Aspirin", "HEXAL"),
				List.of("Granulat", "zum", "Einnehmen"),
				null,
				null);
	}

	@Test
	void emptyIdentifier() {
		test(Collections.emptyList(),
				Collections.emptyList(),
				null,
				null);
	}

	private void test(
			List<String> searchTerm,
			List<String> identifier,
			EditDistance expectedEditDistance,
			Set<Integer> expectedUsedSearchTermTokens
	) {
		EditDistanceListMatcher.MatchInfo match = sut.match(searchTerm, identifier);
		if (expectedEditDistance == null) {
			assertNull(match);
		} else {
			assertNotNull(match);
			assertEquals(expectedEditDistance, match.getDistance());
			assertEquals(expectedUsedSearchTermTokens, match.getUsageStatement().getUsedIndices());
		}
	}

}