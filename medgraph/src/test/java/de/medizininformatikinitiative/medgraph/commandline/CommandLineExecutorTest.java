package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration;
import org.apache.commons.cli.Option;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;

import static de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class CommandLineExecutorTest extends UnitTest {

	private static final String INPUT_STREAM_DATA = "sampleP4ss\n";

	private static ConnectionConfiguration cachedConConfig;
	private CommandLineExecutor sut;

	@BeforeAll
	static void cacheConnectionConfig() {
		cachedConConfig = ConnectionConfiguration.getDefault();
	}

	@AfterAll
	static void uncacheConnectionConfig() {
		ConnectionConfiguration.setDefault(cachedConConfig);
	}

	@BeforeEach
	void setUp() {
		ConnectionConfiguration.setDefault(new ConnectionConfiguration("", "", new char[0]));
		sut = new CommandLineExecutor(new ByteArrayInputStream(INPUT_STREAM_DATA.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void noArguments() {
		test(null);
	}

	@Test
	void invalidCommandLine() {
		test(ExitStatus.COMMAND_LINE_PARSING_UNSUCCESSFUL.code, "-whatthef");
	}

	@Test
	void incompleteDatabaseParameters1() {
		test(ExitStatus.INCOMPLETE_CONNECTION_DATA.code,
				OPTION_DB_USER, "john",
				OPTION_DB_PASSWORD, "aaa");
	}

	@Test
	void incompleteDatabaseParameters2() {
		test(ExitStatus.INCOMPLETE_CONNECTION_DATA.code,
				OPTION_DB_URI, "neo4j://somewhere",
				OPTION_DB_PASSWORD, "passs");
	}

	@Test
	void incompleteDatabaseParameters3() {
		test(ExitStatus.INCOMPLETE_CONNECTION_DATA.code,
				OPTION_DB_URI, "neo4j://somewhere",
				OPTION_DB_USER, "jim");
	}


	@Test
	void incompleteDatabaseParameters4() {
		test(ExitStatus.INCOMPLETE_CONNECTION_DATA.code,
				OPTION_DB_USER, "john",
				OPTION_DB_PASSIN);
	}

	@Test
	void incompleteDatabaseParameters5() {
		test(ExitStatus.INCOMPLETE_CONNECTION_DATA.code,
				OPTION_DB_PASSIN, OPTION_DB_URI, "neo4j://somewhere");
	}

	@ParameterizedTest(name = "usePassIn: {0}")
	@ValueSource(booleans = { false, true })
	void validDatabaseParameters(boolean usePassIn) {
		if (usePassIn) {
			test(null,
					OPTION_DB_USER, "john",
					OPTION_DB_PASSIN,
					OPTION_DB_URI, "neo4j://here");
		} else {
			test(null,
					OPTION_DB_USER, "john",
					OPTION_DB_PASSWORD, "password",
					OPTION_DB_URI, "neo4j://here");
		}

		ConnectionConfiguration config = ConnectionConfiguration.getDefault();
		assertEquals("neo4j://here", config.getUri());
		assertEquals("john", config.getUser());
	}

	/**
	 * Runs the command line executor using the given arguments (only accepts strings and
	 * {@link org.apache.commons.cli.Option}).
	 *
	 * @param expectedStatusCode the expected status code
	 * @param arguments          the arguments to run with
	 */
	private void test(Integer expectedStatusCode, Object... arguments) {
		String[] args = new String[arguments.length];

		for (int i = 0; i < arguments.length; i++) {
			Object arg = arguments[i];
			if (arg instanceof String s) args[i] = s;
			else if (arg instanceof Option o) args[i] = "-"+o.getOpt();
			else throw new IllegalArgumentException("Passed an argument of unsupported type "+arguments.getClass().getSimpleName());
		}


		OptionalInt statusCode = sut.evaluateAndExecuteCommandLineArguments(args);
		if (expectedStatusCode == null) {
			assertTrue(statusCode.isEmpty(),
					"Expected no exit status, but got status code " + statusCode.orElse(0) + "!");
		} else {
			assertTrue(statusCode.isPresent(),
					"Expected exit status " + expectedStatusCode + ", but got no exit status!");
			assertEquals(expectedStatusCode, statusCode.getAsInt(),
					"Expected status " + expectedStatusCode + ", but got status " + statusCode.getAsInt());
		}
	}

}