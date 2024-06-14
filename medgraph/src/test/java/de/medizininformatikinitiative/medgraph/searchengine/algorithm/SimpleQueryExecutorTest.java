package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.InitialMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining.MatchRefiner;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.SubSortingTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Markus Budeus
 */
public class SimpleQueryExecutorTest extends UnitTest {

	@Mock
	private InitialMatchFinder initialMatchFinder;
	@Mock
	private MatchRefiner matchRefiner;
	@Mock
	private RefinedQuery query;
	@Mock
	private SearchQuery searchQuery;

	private SimpleQueryExecutor sut;


	@BeforeEach
	void setUp() {
		sut = new SimpleQueryExecutor(initialMatchFinder, matchRefiner);
		when(query.toSearchQuery()).thenReturn(searchQuery);
		when(matchRefiner.refineMatches(any(), any())).thenAnswer(req -> new SubSortingTree<>(req.getArgument(0)));
	}

	@Test
	public void passesOnInitialMatches() {
		List<OriginalMatch<?>> list = List.of(
				new OriginalMatch<>(SAMPLE_PRODUCT_1),
				new OriginalMatch<>(SAMPLE_PRODUCT_2),
				new OriginalMatch<>(SAMPLE_SUBSTANCE_3)
		);
		when(initialMatchFinder.findInitialMatches(searchQuery)).thenReturn(list.stream());

		assertEquals(list, sut.executeQuery(query));

		verify(matchRefiner).refineMatches(list, query);
	}

	@Test
	public void mergesDuplicateInitialMatches() {
		List<OriginalMatch<?>> list = List.of(
				new OriginalMatch<>(SAMPLE_PRODUCT_1),
				new OriginalMatch<>(SAMPLE_PRODUCT_2),
				new OriginalMatch<>(SAMPLE_PRODUCT_1),
				new OriginalMatch<>(SAMPLE_SUBSTANCE_3),
				new OriginalMatch<>(SAMPLE_SUBSTANCE_3)
		);
		when(initialMatchFinder.findInitialMatches(searchQuery)).thenReturn(list.stream());


		List<MatchingObject<?>> expectedList = List.of(
				new Merge<>(List.of(list.get(0), list.get(2))),
				list.get(1),
				new Merge<>(List.of(list.get(3), list.get(4)))
		);

		assertEquals(expectedList, sut.executeQuery(query));
		verify(matchRefiner).refineMatches(expectedList, query);
	}
}