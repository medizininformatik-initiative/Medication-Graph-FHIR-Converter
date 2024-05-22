package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class PerSessionQueryExecutorTest extends UnitTest {

	@Mock
	private QueryExecutor queryExecutor;
	@Mock
	private DatabaseConnection connection;
	@Mock
	private Session session1;
	@Mock
	private SearchQuery query;

	private boolean sessionClosed;


	private PerSessionQueryExecutor sut;

	@BeforeEach
	void setUp() {
		sessionClosed = false;
		when(connection.createSession()).thenReturn(session1);
		Mockito.doAnswer(req -> {
			sessionClosed = true;
			return null;
		}).when(session1).close();

		sut = new PerSessionQueryExecutor(s ->  queryExecutor, connection);
	}

	@Test
	public void sessionNotReused() {
		Session session2 = mock(Session.class);
		when(connection.createSession()).thenReturn(session1)
				.thenReturn(session2);

		AtomicReference<Session> session = new AtomicReference<>();
		sut = new PerSessionQueryExecutor(s -> {
			session.set(s);
			return queryExecutor;
		}, connection);

		sut.executeQuery(query);
		assertSame(session1, session.get());
		sut.executeQuery(query);
		assertSame(session2, session.get());
	}

	@Test
	public void sessionOpenDuringQueryExecutorInvocation() {
		AtomicBoolean sessionWasClosed = new AtomicBoolean(true);
		when(queryExecutor.executeQuery(any())).thenAnswer(req -> {
			sessionWasClosed.set(sessionClosed);
			return null;
		});

		sut.executeQuery(query);
		assertFalse(sessionWasClosed.get());
	}

	@Test
	public void sessionClosedAfterExecution() {
		sut.executeQuery(query);
		assertTrue(sessionClosed);
	}

	@Test
	public void sessionClosedInCaseOfCrash() {
		when(queryExecutor.executeQuery(any())).thenThrow(new RuntimeException("This went downhill quickly..."));
		assertThrows(RuntimeException.class, () -> {
			sut.executeQuery(query);
		});
		assertTrue(sessionClosed);
	}

	@Test
	public void correctResultReturned() {
		List<MatchingObject> resultList = List.of(
				new OriginalMatch(SAMPLE_PRODUCT_1),
				new Merge(List.of(new OriginalMatch(SAMPLE_PRODUCT_2), new OriginalMatch(SAMPLE_PRODUCT_2)))
		);
		when(queryExecutor.executeQuery(query)).thenReturn(resultList);

		assertEquals(resultList, sut.executeQuery(query));
	}


}