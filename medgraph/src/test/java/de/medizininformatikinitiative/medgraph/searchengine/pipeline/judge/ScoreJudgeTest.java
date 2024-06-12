package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ScoreJudgeTest {

	private static final double PASSING_SCORE = 1;

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Test
	public void singleJudgement() {
		ScoredJudgement judgement = new TestJudge(2, null).judge(null, null);
		assertEquals(2, judgement.getScore());
		assertEquals(PASSING_SCORE, judgement.getPassingScore().getAsDouble());
		assertTrue(judgement.passed());
		assertEquals("test", judgement.name());
		assertEquals("desc", judgement.description());
	}

	@Test
	public void singleJudgement2() {
		ScoredJudgement judgement = new TestJudge(0, null).judge(null, null);
		assertEquals(0, judgement.getScore());
		assertFalse(judgement.passed());
	}

	@Test
	public void batchJudgement() {
		List<ScoredJudgement> judgements = new TestJudge(0, List.of(2.0, 0.0, 1.0)).
				batchJudge(List.of(new Substance(1, "A"), new Substance(2, "N"), new Substance(3, "C")), null);

		assertEquals(3, judgements.size());
		assertEquals(2, judgements.get(0).getScore());
		assertEquals(0, judgements.get(1).getScore());
		assertEquals(1, judgements.get(2).getScore());
		assertTrue(judgements.get(0).passed());
		assertFalse(judgements.get(1).passed());
	}

	private static class TestJudge extends ScoreJudge {

		private final double judgement;
		private final List<Double> batchJudgement;

		public TestJudge(double judgement, List<Double> batchJudgement) {
			super(PASSING_SCORE);
			this.judgement = judgement;
			this.batchJudgement = batchJudgement;
		}

		@Override
		protected double judgeInternal(Matchable matchable, SearchQuery query) {
			return judgement;
		}

		@Override
		protected List<Double> batchJudgeInternal(List<? extends Matchable> matchables, SearchQuery query) {
			return batchJudgement;
		}

		@Override
		public String toString() {
			return "test";
		}

		@Override
		public String getDescription() {
			return "desc";
		}
	}

}