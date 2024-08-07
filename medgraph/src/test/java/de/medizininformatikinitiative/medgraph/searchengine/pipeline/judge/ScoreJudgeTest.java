package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class ScoreJudgeTest {

	@Test
	public void singleJudgement() {
		ScoreJudgementInfo judgement = new TestJudge(2, null).judge(null, null);
		assertEquals(2, judgement.getScore());
	}

	@Test
	public void singleJudgement2() {
		ScoreJudgementInfo judgement = new TestJudge(0, null).judge(null, null);
		assertEquals(0, judgement.getScore());
	}

	@Test
	public void batchJudgement() {
		List<ScoreJudgementInfo> judgements = new TestJudge(0, List.of(2.0, 0.0, 1.0)).
				batchJudge(List.of(new Substance(1, "A"), new Substance(2, "N"), new Substance(3, "C")), null);

		assertEquals(3, judgements.size());
		assertEquals(2, judgements.get(0).getScore());
		assertEquals(0, judgements.get(1).getScore());
		assertEquals(1, judgements.get(2).getScore());
	}

	private static class TestJudge extends ScoreJudge<Matchable> {

		private final double judgement;
		private final List<Double> batchJudgement;

		public TestJudge(double judgement, List<Double> batchJudgement) {
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
		public String getDescription() {
			return "desc";
		}
	}

}