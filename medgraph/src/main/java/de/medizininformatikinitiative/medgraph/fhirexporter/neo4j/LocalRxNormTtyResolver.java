package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Local SQLite-backed resolver for RxNorm TTY (e.g., IN, PIN, SCD).
 * Looks up TTY in RXNCONSO where SAB='RXNORM' and SUPPRESS!='Y'.
 */
public final class LocalRxNormTtyResolver implements RxNormProductMatcher.RxcuiTermTypeResolver {

	private final String jdbcUrl;

	public LocalRxNormTtyResolver(@NotNull String sqliteDbPath) {
		this.jdbcUrl = "jdbc:sqlite:" + sqliteDbPath;
	}

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


