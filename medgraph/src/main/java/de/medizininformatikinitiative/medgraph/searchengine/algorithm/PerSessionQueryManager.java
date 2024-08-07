package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.QueryRefiner;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.function.Function;

/**
 * {@link QueryExecutor}-implementation which delegates to a seperate {@link QueryExecutor} which is created on-demand
 * for every query. Together with it, a separate {@link Session} is generated for every query.
 *
 * @author Markus Budeus
 */
public class PerSessionQueryManager<T extends Matchable> implements QueryExecutor<T>, QueryRefiner {

	private final Function<Session, QueryRefiner> queryRefinerFactory;
	private final Function<Session, QueryExecutor<T>> queryExecutorFactory;
	private final DatabaseConnection connection;

	public PerSessionQueryManager(Function<Session, QueryRefiner> queryRefinerFactory,
	                              Function<Session, QueryExecutor<T>> queryExecutorFactory,
	                              DatabaseConnection connection) {
		this.queryExecutorFactory = queryExecutorFactory;
		this.queryRefinerFactory = queryRefinerFactory;
		this.connection = connection;
	}

	@Override
	public List<MatchingObject<T>> executeQuery(RefinedQuery query) {
		try (Session session = connection.createSession()) {
			return queryExecutorFactory.apply(session).executeQuery(query);
		}
	}

	@Override
	public RefinedQuery refine(RawQuery query) {
		try (Session session = connection.createSession()) {
			return queryRefinerFactory.apply(session).refine(query);
		}
	}
}
