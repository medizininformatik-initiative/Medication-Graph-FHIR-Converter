package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.QueryRefiner;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.ScoreMergingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static de.medizininformatikinitiative.medgraph.TestFactory.SAMPLE_PRODUCT_1;
import static de.medizininformatikinitiative.medgraph.TestFactory.SAMPLE_PRODUCT_2;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Markus Budeus
 */
public class PerSessionQueryManagerTest extends UnitTest {

	@Mock
	private QueryRefiner queryRefiner;
	@Mock
	private QueryExecutor<Product> queryExecutor;
	@Mock
	private DatabaseConnection connection;
	@Mock
	private Session session1;
	@Mock
	private RefinedQuery query;
	@Mock
	private RawQuery rawQuery;

	private boolean sessionClosed;


	private PerSessionQueryManager<Product> sut;

	@BeforeEach
	void setUp() {
		sessionClosed = false;
		when(connection.createSession()).thenReturn(session1);
		Mockito.doAnswer(req -> {
			sessionClosed = true;
			return null;
		}).when(session1).close();

		sut = new PerSessionQueryManager<>(s -> queryRefiner, s -> queryExecutor, connection);
	}

	@ParameterizedTest(name = "Refiner: {0}")
	@ValueSource(booleans = {true, false})
	public void sessionNotReused(boolean useRefiner) {
		Session session2 = mock(Session.class);
		when(connection.createSession()).thenReturn(session1)
		                                .thenReturn(session2);

		AtomicReference<Session> session = new AtomicReference<>();
		sut = new PerSessionQueryManager<>(s -> {
			session.set(s);
			return queryRefiner;
		}, s -> {
			session.set(s);
			return queryExecutor;
		}, connection);

		refineOrExecute(useRefiner);
		assertSame(session1, session.get());
		refineOrExecute(useRefiner);
		assertSame(session2, session.get());
	}

	@ParameterizedTest(name = "Refiner: {0}")
	@ValueSource(booleans = {true, false})
	public void sessionOpenDuringQueryExecutorOrRefinerInvocation(boolean refiner) {
		AtomicBoolean sessionWasClosed = new AtomicBoolean(true);
		when(queryExecutor.executeQuery(any())).thenAnswer(req -> {
			sessionWasClosed.set(sessionClosed);
			return null;
		});
		when(queryRefiner.refine(any())).thenAnswer(req -> {
			sessionWasClosed.set(sessionClosed);
			return null;
		});

		refineOrExecute(refiner);
		assertFalse(sessionWasClosed.get());
	}

	@ParameterizedTest(name = "Refiner: {0}")
	@ValueSource(booleans = {true, false})
	public void sessionClosedAfterExecution(boolean refiner) {
		refineOrExecute(refiner);
		assertTrue(sessionClosed);
	}

	@ParameterizedTest(name = "Refiner: {0}")
	@ValueSource(booleans = {true, false})
	public void sessionClosedInCaseOfCrash(boolean refiner) {
		when(queryExecutor.executeQuery(any())).thenThrow(new RuntimeException("This went downhill quickly..."));
		when(queryRefiner.refine(any())).thenThrow(new RuntimeException("This went downhill VERY quickly..."));
		assertThrows(RuntimeException.class, () -> {
			refineOrExecute(refiner);
		});
		assertTrue(sessionClosed);
	}

	@Test
	public void correctRefinementResultReturned() {
		RefinedQuery refinedQuery = mock();
		when(queryRefiner.refine(rawQuery)).thenReturn(refinedQuery);

		assertEquals(refinedQuery, sut.refine(rawQuery));
	}

	@Test
	public void correctExecutionResultReturned() {
		List<MatchingObject<Product>> resultList = List.of(
				new OriginalMatch<>(SAMPLE_PRODUCT_1),
				new Merge<>(List.of(new OriginalMatch<>(SAMPLE_PRODUCT_2), new OriginalMatch<>(SAMPLE_PRODUCT_2)), ScoreMergingStrategy.MAX)
		);
		when(queryExecutor.executeQuery(query)).thenReturn(resultList);

		assertEquals(resultList, sut.executeQuery(query));
	}

	private void refineOrExecute(boolean refine) {
		if (refine)
			sut.refine(rawQuery);
		else
			sut.executeQuery(query);
	}


}