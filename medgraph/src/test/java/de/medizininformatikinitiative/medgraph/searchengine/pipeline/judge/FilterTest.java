package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class FilterTest {


	@Test
	public void singleTest() {
		FilteringInfo filteringInfo = new TestFilter(true).judge(null, null);
		assertTrue(filteringInfo.passed());
	}

	@Test
	public void singleTest2() {
		FilteringInfo filteringInfo = new TestFilter(false).judge(null, null);
		assertFalse(filteringInfo.passed());
	}

	@Test
	public void batchTest() {
		List<FilteringInfo> list = new TestFilter(false, false, true, false)
				.batchJudge(List.of(
						new Substance(1, "A"),
						new Substance(2, "A"),
						new Substance(3, "A"),
						new Substance(5, "A")
				), null);

		assertFalse(list.get(0).passed());
		assertFalse(list.get(1).passed());
		assertTrue(list.get(2).passed());
		assertFalse(list.get(3).passed());
	}

	private static class TestFilter implements Filter<Matchable> {

		private final Boolean[] result;

		public TestFilter(Boolean... result) {
			this.result = result;
		}

		@Override
		public boolean passesFilter(Matchable matchable, SearchQuery query) {
			return result[0];
		}

		@Override
		public List<Boolean> batchPassesFilter(List<? extends Matchable> matchables, SearchQuery query) {
			List<Boolean> result = new ArrayList<>(matchables.size());
			result.addAll(Arrays.asList(this.result).subList(0, matchables.size()));
			return result;
		}

		@Override
		public String getDescription() {
			return "does things";
		}
	}

}