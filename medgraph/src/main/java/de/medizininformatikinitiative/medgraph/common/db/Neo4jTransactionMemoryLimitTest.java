package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class which probes the transaction memory limit on Neo4j. Needed because the FHIR export requires substantial
 * amounts of memory and can fail if the limit is too low.
 *
 * @author Joshua Wiedekopf
 * @author Markus Budeus
 */
public class Neo4jTransactionMemoryLimitTest {

	private final static Logger logger = LogManager.getLogger("ConnectionTest");

	private static final long BYTE = 1L;
	private static final long KiB = BYTE << 10;
	private static final long MiB = KiB << 10;
	private static final long GiB = MiB << 10;

	private static final String TRANSACTION_SIZE_SETTING = "dbms.memory.transaction.total.max";
	private static final long RECOMMENDED_TRANSACTION_SIZE_MEBIBYTES = 2048L;

	private static final String WARNING_UNKNOWN_LIMIT = "Could not determine the Neo4j transaction size limit. " +
			"It might be set either too low (recommended value: " + RECOMMENDED_TRANSACTION_SIZE_MEBIBYTES + " MiB), in which case queries can fail.";

	private static final String WARNING_LIMIT_TOO_LOW = "Your transaction size limit is set to %d MiB, " +
			"which is below the recommended limit of "+RECOMMENDED_TRANSACTION_SIZE_MEBIBYTES+" MiB. " +
			"Please consider increasing it using the neo4j setting '"+TRANSACTION_SIZE_SETTING+"'.";

	/**
	 * Probes the configured initial transaction size limit on the given Neo4j Session. If it cannot be found or is
	 * too low, a corresponding warning message is returned. Otherwise, this function returns nothing.
	 *
	 * @param session The session on which to probe the size limit.
	 * @return An {@link Optional} containing a warning message if the limit cannot be determined or is too low,
	 * otherwise an empty {@link Optional}.
	 */
	public Optional<String> probeNeo4jTransactionSizeLimit(Session session) {
		Result result = session.run("SHOW SETTINGS \"" + TRANSACTION_SIZE_SETTING + "\"");
		if (!result.hasNext()) {
			return Optional.of(WARNING_UNKNOWN_LIMIT);
		}
		Record record = result.next();
		if (result.hasNext()) {
			logger.log(Level.ERROR, "Got multiple results when querying the transaction size limit!");
		}

		return checkSizeLimit(record.get("value").asString());
	}

	/**
	 * Checks whether the size limit as given in Neo4j (e.g, "5.3GiB") is considered sufficient. Returns an appropriate
	 * warning message if the size cannot be interpreted or is too low.
	 */
	Optional<String> checkSizeLimit(String neo4jSizeLimit) {
		if (neo4jSizeLimit == null) {
			return Optional.of(WARNING_UNKNOWN_LIMIT);
		}
		if (neo4jSizeLimit.equals("0")) {
			// Zero means unlimited.
			return Optional.empty();
		}
		Pattern valuePattern = Pattern.compile("^(?<val>[0-9.]+)(?<unit>mib|mb|m|gib|gb|g)$");
		var matcher = valuePattern.matcher(neo4jSizeLimit.toLowerCase());
		if (!matcher.matches()) {
			return Optional.of(WARNING_UNKNOWN_LIMIT);
		}
		long bytes;
		try {
			bytes = getBytes(matcher);
		} catch (IllegalStateException | NumberFormatException e) {
			logger.log(Level.ERROR, "Failed to parse Neo4j size description: "+neo4jSizeLimit, e);
			return Optional.of(WARNING_UNKNOWN_LIMIT);
		}

		if (bytes == 0L) {
			// Zero means unlimited.
			return Optional.empty();
		}
		long mebibytes = bytes / MiB;
		if (mebibytes < RECOMMENDED_TRANSACTION_SIZE_MEBIBYTES) {
			return Optional.of(WARNING_LIMIT_TOO_LOW.formatted(mebibytes));
		}
		return Optional.empty();
	}

	/**
	 * Convert a Neo4J size setting to bytes. The provided matcher must have two groups, 'val' and 'unit'. Only
	 * lower-case values can be parsed. Implementation inspired by <a
	 * href="https://www.baeldung.com/java-human-readable-byte-size">this baeldung post</a>.
	 *
	 * @param matcher the input matcher
	 * @return the number of bytes
	 */
	private static long getBytes(Matcher matcher) {
		double numericValue = Double.parseDouble(matcher.group("val"));
		String unit = matcher.group("unit");
		// From the Neo4J settings file at version 5.26.7:
		// Memory settings are specified kibibytes with the 'k' suffix, mebibytes with 'm' and gibibytes with 'g'.

		// However, the online documentation allows more values: B, KiB, KB, K, kB, kb, k, MiB, MB, M, mB, mb, m, GiB, GB, G, gB, gb, g, TiB, TB, PiB, PB, EiB, EB
		// The switch statement is reduced to 'reasonable' units.
		double bytesLong = switch (unit) {
			case "mib", "m", "mb" -> numericValue * MiB;
			case "gib", "g", "gb" -> numericValue * GiB;
			default -> throw new IllegalStateException("Unexpected value: " + unit);
		};
		return (long) bytesLong;
	}
}
