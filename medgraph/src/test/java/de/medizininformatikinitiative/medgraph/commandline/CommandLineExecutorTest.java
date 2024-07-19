package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfigurationService;
import org.apache.commons.cli.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import static de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Markus Budeus
 */
public class CommandLineExecutorTest extends UnitTest {

	private static final String PASSWORD = "sampleP4ssw0rd";

	@Mock
	private ConnectionConfigurationService configurationService;

	@Mock
	private CommandLineUtility utility;

	private CommandLineExecutor sut;

	@BeforeEach
	void setUp() {
		insertMockDependency(ConnectionConfigurationService.class, configurationService);
		when(utility.invoke(any(), any())).thenReturn(ExitStatus.SUCCESS);
		when(utility.getUsage()).thenReturn("testUtil");
		sut = new CommandLineExecutor(new ByteArrayInputStream((PASSWORD + "\n").getBytes(StandardCharsets.UTF_8)),
				Map.of("testUtil", utility));
	}

	@Test
	void noArguments() {
		test(null);
	}

	@Test
	void getHelp() {
		test(ExitStatus.SUCCESS.code, "--help");
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
	@ValueSource(booleans = {false, true})
	void validDatabaseParameters(boolean usePassIn) {
		if (usePassIn) {
			test(null,
					OPTION_DB_USER, "john",
					OPTION_DB_PASSIN,
					OPTION_DB_URI, "neo4j://here");
		} else {
			test(null,
					OPTION_DB_USER, "john",
					OPTION_DB_PASSWORD, PASSWORD,
					OPTION_DB_URI, "neo4j://here");
		}

		ConnectionConfiguration expected = new ConnectionConfiguration(
				"neo4j://here",
				"john",
				PASSWORD.toCharArray()
		);
		verify(configurationService).setConnectionConfiguration(eq(expected), eq(
				ConnectionConfigurationService.SaveOption.DONT_SAVE));
	}

	@Test
	void utilityNotInvokedCorrectly() {
		test(ExitStatus.INCORRECT_USAGE.code, "randomUtility");
	}

	@Test
	void utilityInvoked() {
		test(ExitStatus.SUCCESS.code, "testUtil", "a", "b");
		verify(utility).invoke(any(), eq(List.of("a", "b")));
	}

	@Test
	void utilityFails() {
		when(utility.invoke(any(), any())).thenReturn(ExitStatus.ioException(new IOException("RIP")));
		test(ExitStatus.IO_EXCEPTION_CODE, "testUtil");
	}

	@Test
	void utilityThrows() {
		when(utility.invoke(any(), any())).thenThrow(new UnsupportedOperationException("This failed!"));
		test(ExitStatus.INTERNAL_ERROR_CODE, "testUtil", "x");
	}

	@Test
	void utilityHelpDialogRequested() {
		test(ExitStatus.SUCCESS.code, "testUtil", "-h");
		verify(utility, never()).invoke(any(), any());
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
			else if (arg instanceof Option o) args[i] = "-" + o.getOpt();
			else throw new IllegalArgumentException(
						"Passed an argument of unsupported type " + arguments.getClass().getSimpleName());
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