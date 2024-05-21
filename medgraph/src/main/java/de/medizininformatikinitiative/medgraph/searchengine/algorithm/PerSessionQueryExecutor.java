package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
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
public class PerSessionQueryExecutor implements QueryExecutor {

	private final Function<Session, QueryExecutor> queryExecutorFactory;
	private final DatabaseConnection connection;

	public PerSessionQueryExecutor(Function<Session, QueryExecutor> queryExecutorFactory,
	                               DatabaseConnection connection) {
		this.queryExecutorFactory = queryExecutorFactory;
		this.connection = connection;
	}

	@Override
	public List<MatchingObject> executeQuery(SearchQuery query) {
		try (Session session = connection.createSession()) {
			return queryExecutorFactory.apply(session).executeQuery(query);
		}
	}
}
