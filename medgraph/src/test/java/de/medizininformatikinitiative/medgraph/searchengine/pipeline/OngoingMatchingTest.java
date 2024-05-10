package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ProductOnlyFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class OngoingMatchingTest {

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void applyFilter(boolean ensureSurvival) {
		OngoingMatching sut = createSut(List.of(
				SAMPLE_PRODUCT_1,
				SAMPLE_SUBSTANCE_1,
				SAMPLE_SUBSTANCE_3
		));

		sut.applyFilter(new ProductOnlyFilter(), ensureSurvival);

		List<MatchingObject> currentMatches = sut.getCurrentMatches();
		assertEquals(1, currentMatches.size());
		MatchingObject object = currentMatches.getFirst();
		assertEquals(SAMPLE_PRODUCT_1, object.getObject());
		assertTrue(object.getAppliedJudgements().getFirst().isPassed());
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void applyFilterWithSurvivalInQuestion(boolean ensureSurvival) {
		OngoingMatching sut = createSut(List.of(
				SAMPLE_SUBSTANCE_1,
				SAMPLE_SUBSTANCE_3
		));

		sut.applyFilter(new ProductOnlyFilter(), ensureSurvival);

		List<MatchingObject> currentMatches = sut.getCurrentMatches();

		if (ensureSurvival) {
			assertEquals(2, currentMatches.size());
			MatchingObject object = currentMatches.getFirst();
			assertEquals(SAMPLE_SUBSTANCE_1, object.getObject());
			assertFalse(object.getAppliedJudgements().getFirst().isPassed());
		} else {
			assertTrue(currentMatches.isEmpty());
		}
	}

	private OngoingMatching createSut(List<Matchable> matchables) {
		return new OngoingMatching(matchables.stream().map(OriginalMatch::new).toList(), new SearchQuery());
	}

}