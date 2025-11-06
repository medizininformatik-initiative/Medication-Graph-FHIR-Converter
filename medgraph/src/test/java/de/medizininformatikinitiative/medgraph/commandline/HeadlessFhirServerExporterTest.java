package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionFailureReason;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionException;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSink;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSinkFactory;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor.OPTION_HTTP_BASIC_AUTH;
import static de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor.OPTION_TOKEN_AUTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

/**
 * @author Markus Budeus
 */
public class HeadlessFhirServerExporterTest extends UnitTest {

	private final String URL = "http://localhost:8080/myFlame";

	@Mock
	private FhirServerExportSinkFactory fhirExportFactory;
	@Mock
	private FhirServerExportSink fhirExport;
	@Mock
	private CommandLine commandLine;
	private DatabaseConnectionService databaseConnectionService;

	private HeadlessFhirServerExporter sut;

	@BeforeEach
	void setUp() {
		insertMockDependency(FhirServerExportSinkFactory.class, fhirExportFactory);
		databaseConnectionService = insertDatabaseConnectionServiceMock();
		Mockito.when(fhirExportFactory.prepareExportWithoutAuth(notNull())).thenReturn(fhirExport);
		Mockito.when(fhirExportFactory.prepareExportWithHttpBasicAuth(notNull(), notNull(), notNull()))
		       .thenReturn(fhirExport);
		Mockito.when(fhirExportFactory.prepareExportWithTokenAuth(notNull(), notNull())).thenReturn(fhirExport);
		sut = new HeadlessFhirServerExporter();
	}

	@Test
	void successfulRunNoAuth() throws IOException {
		ExitStatus exitStatus = sut.invoke(commandLine, List.of(URL));

		assertEquals(ExitStatus.SUCCESS, exitStatus);
		verify(fhirExportFactory).prepareExportWithoutAuth(URL);
		verify(fhirExport).doExport(notNull());
	}

	@Test
	void successfulRunHttpBasicAuth() throws IOException {
		when(commandLine.getOptionValue(OPTION_HTTP_BASIC_AUTH.getOpt())).thenReturn("johndoe:thispwsucks4");
		ExitStatus exitStatus = sut.invoke(commandLine, List.of(URL));

		assertEquals(ExitStatus.SUCCESS, exitStatus);
		verify(fhirExportFactory).prepareExportWithHttpBasicAuth(URL, "johndoe", "thispwsucks4".toCharArray());
		verify(fhirExport).doExport(notNull());
	}

	@Test
	void successfulRunTokenAuth() throws IOException {
		when(commandLine.getOptionValue(OPTION_TOKEN_AUTH.getOpt())).thenReturn("thisIsMyToken");
		ExitStatus exitStatus = sut.invoke(commandLine, List.of(URL));

		assertEquals(ExitStatus.SUCCESS, exitStatus);
		verify(fhirExportFactory).prepareExportWithTokenAuth(URL, "thisIsMyToken");
		verify(fhirExport).doExport(notNull());
	}

	@Test
	void authModeConflict() {
		when(commandLine.getOptionValue(OPTION_HTTP_BASIC_AUTH.getOpt())).thenReturn("user:passw");
		when(commandLine.getOptionValue(OPTION_TOKEN_AUTH.getOpt())).thenReturn("aToken");
		ExitStatus exitStatus = sut.invoke(commandLine, List.of(URL));
		assertEquals(ExitStatus.INCORRECT_USAGE, exitStatus);
	}

	@Test
	void httpBasicAuthNoColon() throws IOException {
		when(commandLine.getOptionValue(OPTION_HTTP_BASIC_AUTH.getOpt())).thenReturn("idontneednopassword");
		ExitStatus exitStatus = sut.invoke(commandLine, List.of(URL));
		assertEquals(ExitStatus.INCORRECT_USAGE, exitStatus);
	}

	@Test
	void httpBasicAuthMultipleColons() throws IOException {
		when(commandLine.getOptionValue(OPTION_HTTP_BASIC_AUTH.getOpt())).thenReturn( "admin:my:password:needs:colons");
		ExitStatus exitStatus = sut.invoke(commandLine,
				List.of("http://otherurl"));

		assertEquals(ExitStatus.SUCCESS, exitStatus);
		verify(fhirExportFactory).prepareExportWithHttpBasicAuth("http://otherurl", "admin",
				"my:password:needs:colons".toCharArray());
		verify(fhirExport).doExport(notNull());
	}

	@Test
	void tooManyArguments() {
		ExitStatus exitStatus = sut.invoke(commandLine, List.of(URL, "whatIsThis"));

		assertEquals(ExitStatus.INCORRECT_USAGE, exitStatus);
	}

	@Test
	void urlMissing() {
		ExitStatus exitStatus = sut.invoke(commandLine, List.of());

		assertEquals(ExitStatus.INCORRECT_USAGE, exitStatus);
	}

	@Test
	void ioException() throws IOException {
		IOException exception = new IOException("Not reachable!");
		doThrow(exception).when(fhirExport).doExport(any());

		ExitStatus exitStatus = sut.invoke(commandLine, List.of("thisWillFail"));
		assertEquals(ExitStatus.ioException(exception), exitStatus);
	}

	@Test
	void neo4jUnreachable() throws DatabaseConnectionException {
		// This test is rather strict. It fails if the SUT does not test the connection beforehand, which may be
		// fine if it catches connection errors later.
		when(databaseConnectionService.createConnection(true)).thenThrow(new DatabaseConnectionException(
				ConnectionFailureReason.SERVICE_UNAVAILABLE));

		ExitStatus exitStatus = sut.invoke(commandLine, List.of("outpath"));

		assertEquals(ExitStatus.NEO4J_SERVICE_UNAVAILABLE, exitStatus);
	}

}