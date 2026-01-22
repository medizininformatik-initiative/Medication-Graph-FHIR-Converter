package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Local SQLite-backed resolver for RxNorm TTY (Term Type) lookups.
 * <p>
 * Resolves the Term Type (TTY) for a given RxCUI by querying the local RxNorm SQLite database.
 * Common TTY values include IN (Ingredient Name), PIN (Precise Ingredient Name), SCD (Semantic Clinical Drug).
 * <p>
 * Used by {@link RxNormProductMatcher} to select the best RxCUI when multiple options are available
 * (e.g., preferring PIN over IN for more specific ingredient matching).
 *
 * @author Lucy Strüfing
 */
public final class LocalRxNormTtyResolver implements RxNormProductMatcher.RxcuiTermTypeResolver {

	private final String jdbcUrl;

	/**
	 * Creates a new resolver that connects to the specified RxNorm SQLite database.
	 *
	 * @param sqliteDbPath path to the SQLite database file
	 */
	public LocalRxNormTtyResolver(@NotNull String sqliteDbPath) {
		this.jdbcUrl = "jdbc:sqlite:" + sqliteDbPath;
	}

	/**
	 * Resolves the Term Type (TTY) for the given RxCUI.
	 * <p>
	 * Queries the RXNCONSO table for the TTY value, filtering for RXNORM source
	 * and non-suppressed entries. Returns null if the RxCUI is not found or an error occurs.
	 *
	 * @param rxcui the RxCUI to look up
	 * @return the Term Type (e.g., "IN", "PIN", "SCD") or null if not found
	 */
	@Override
	public @Nullable String resolveTty(@NotNull String rxcui) {
		final String sql = """
				SELECT TTY
				FROM RXNCONSO
				WHERE RXCUI = ?
				  AND SAB = 'RXNORM'
				  AND (SUPPRESS IS NULL OR SUPPRESS <> 'Y')
				LIMIT 1
				""";
		try (Connection conn = DriverManager.getConnection(jdbcUrl);
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, rxcui);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("TTY");
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormTtyResolver] SQL error: " + e.getMessage());
		}
		return null;
	}
}