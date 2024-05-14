package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.InitialMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining.MatchRefiner;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Markus Budeus
 */
public class MatchingAlgorithmTest extends UnitTest {

	@Mock
	private InitialMatchFinder initialMatchFinder;
	@Mock
	private MatchRefiner matchRefiner;
	@Mock
	private SearchQuery query;

	private MatchingAlgorithm sut;


	@BeforeEach
	void setUp() {
		sut = new MatchingAlgorithm(initialMatchFinder, matchRefiner);
	}

	@Test
	public void passesOnInitialMatches() {
		List<OriginalMatch> list = List.of(
				new OriginalMatch(SAMPLE_PRODUCT_1),
				new OriginalMatch(SAMPLE_PRODUCT_2),
				new OriginalMatch(SAMPLE_SUBSTANCE_3)
		);
		when(initialMatchFinder.findInitialMatches(query)).thenReturn(list.stream());

		sut.findMatches(query);

		verify(matchRefiner).refineMatches(list, query);
	}

	@Test
	public void mergesDuplicateInitialMatches() {
		List<OriginalMatch> list = List.of(
				new OriginalMatch(SAMPLE_PRODUCT_1),
				new OriginalMatch(SAMPLE_PRODUCT_2),
				new OriginalMatch(SAMPLE_PRODUCT_1),
				new OriginalMatch(SAMPLE_SUBSTANCE_3),
				new OriginalMatch(SAMPLE_SUBSTANCE_3)
		);
		when(initialMatchFinder.findInitialMatches(query)).thenReturn(list.stream());

		sut.findMatches(query);

		List<MatchingObject> expectedList = List.of(
				new Merge(List.of(list.get(0), list.get(2))),
				list.get(1),
				new Merge(List.of(list.get(3), list.get(4)))
		);

		verify(matchRefiner).refineMatches(expectedList, query);
	}
}