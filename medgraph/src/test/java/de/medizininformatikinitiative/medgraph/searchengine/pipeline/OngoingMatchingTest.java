package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ProductOnlyFilter;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;
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
	@ValueSource(booleans = {true, false})
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
	@ValueSource(booleans = {true, false})
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

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void applyScoreJudge(boolean ensureSurvival) {
		OngoingMatching sut = createSut(List.of(
				new Substance(98, ""),
				new Product(102, "Dope"),
				new Product(100, ""),
				new Substance(100, ""),
				new Substance(99, "")
		));

		sut.applyScoreJudge(new IdSizeJudge(100.0), ensureSurvival);


		List<MatchingObject> currentMatches = sut.getCurrentMatches();
		assertEquals(3, currentMatches.size());
		MatchingObject object = currentMatches.getFirst();
		assertEquals(new Product(102, "Dope"), object.getObject());
		assertTrue(object.getAppliedJudgements().getFirst().isPassed());
		assertEquals(new Product(100, ""), currentMatches.get(1).getObject());
		assertEquals(new Substance(100, ""), currentMatches.get(2).getObject());
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void applyScoreJudgeWithSurvivalInQuestion(boolean ensureSurvival) {
		OngoingMatching sut = createSut(List.of(
				new Substance(98, ""),
				new Product(102, "Dope"),
				new Product(100, ""),
				new Substance(100, ""),
				new Substance(99, "")
		));

		sut.applyScoreJudge(new IdSizeJudge(105.0), ensureSurvival);


		List<MatchingObject> currentMatches = sut.getCurrentMatches();

		if (ensureSurvival) {
			assertEquals(5, currentMatches.size());
			MatchingObject object = currentMatches.getFirst();
			// Despite all failing, higher scores still win!
			assertEquals(new Product(102, ""), object.getObject());
			assertFalse(object.getAppliedJudgements().getFirst().isPassed());
		} else {
			assertEquals(0, currentMatches.size());
		}
	}

	@Test
	public void multipleActions() {
		OngoingMatching sut = createSut(List.of(
				new Substance(97, ""),
				new Product(32, "Not so dope"),
				new Product(67, ""),
				new Product(100, "GREAT"),
				new Substance(107, "")
		));

		sut.applyScoreJudge(new IdSizeJudge(200.0), true);

		sut.applyScoreJudge(new IdSizeJudge(40.0), false);

		assertEquals(4, sut.getCurrentMatches().size());

		sut.applyFilter(new ProductOnlyFilter(), true);

		List<MatchingObject> currentMatches = sut.getCurrentMatches();
		assertEquals(2, currentMatches.size());
		assertEquals(new Product(100, "GREAT"), currentMatches.getFirst().getObject());
		assertEquals(new Product(67, ""), currentMatches.getLast().getObject());


		List<Judgement> judgementList = currentMatches.getFirst().getAppliedJudgements();
		assertFalse(judgementList.get(0).isPassed());
		assertTrue(judgementList.get(1).isPassed());
		assertTrue(judgementList.get(2).isPassed());
		assertEquals(IdSizeJudge.NAME, judgementList.get(0).getName());
		assertEquals(IdSizeJudge.NAME, judgementList.get(1).getName());
		assertEquals(ProductOnlyFilter.NAME, judgementList.get(2).getName());
	}

	private OngoingMatching createSut(List<Matchable> matchables) {
		return new OngoingMatching(matchables.stream().map(OriginalMatch::new).toList(), new SearchQuery());
	}


	private static class IdSizeJudge extends ScoreJudge {

		public static final String NAME = "Id Judge";

		public IdSizeJudge(Double passingScore) {
			super(passingScore);
		}

		@Override
		protected double judgeInternal(Matchable matchable, SearchQuery query) {
			return ((IdMatchable) matchable).getId();
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public String getDescription() {
			return "Rates matches by their mmi id. I know, it makes no sense.";
		}
	}

}