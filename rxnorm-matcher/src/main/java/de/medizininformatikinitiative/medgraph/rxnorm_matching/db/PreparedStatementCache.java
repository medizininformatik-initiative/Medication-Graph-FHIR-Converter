package de.medizininformatikinitiative.medgraph.rxnorm_matching.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates SQL statements as PreparedStatements and keeps them im memory such that every query has to be compiled just
 * once.
 * @author Markus Budeus
 */
public class PreparedStatementCache implements AutoCloseable {

	private final Connection connection;

	private final Map<String, Map<Integer, PreparedStatement>> cache = new HashMap<>();

	public PreparedStatementCache(Connection connection) {
		this.connection = connection;
	}

	public ResultSet executeQueryWithOneTupleVar(
			String sql,
			Set<String> parameters) throws SQLException {
		PreparedStatement statement = getStatement(sql, parameters.size());
		int index = 1;
		for (String param: parameters) {
			statement.setString(index, param);
			index++;
		}
		return statement.executeQuery();
	}

	private PreparedStatement getStatement(String sql, int parameterTupleSize) {
		Map<Integer, PreparedStatement> queryCache = cache.compute(sql, (k, v) ->
				v != null ? v : new HashMap<>());

		return queryCache.compute(parameterTupleSize, (k, v) ->
		{
			try {
				return v != null ? v : createPreparedStatement(sql, parameterTupleSize);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Creates a {@link PreparedStatement}, expecting the query to contain exactly one question mark. This is where
	 * the question mark is expanded to form a tuple of the expected size.
	 * <p>
	 * E.g.
	 * createPreparedStatement("SELECT * FROM patients WHERE patId IN ?", 5) becomes the prepared statment
	 * "SELECT * FROM patients WHERE patId IN (?, ?, ?, ?, ?)"
	 */
	private PreparedStatement createPreparedStatement(String sql, int parameterTupleSize) throws SQLException {
		StringBuilder replacement = new StringBuilder("(");
		replacement.append("?, ".repeat(parameterTupleSize));
		if (parameterTupleSize > 0) {
			replacement.delete(replacement.length() - 2, replacement.length());
		}
		replacement.append(")");
		String finalSql = sql.replace("?", replacement);
		return connection.prepareStatement(finalSql);
	}

	@Override
	public void close() throws Exception {
		connection.close();
	}
}
