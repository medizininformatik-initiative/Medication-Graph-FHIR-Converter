package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionFailureReason;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionException;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService;
import de.medizininformatikinitiative.medgraph.fhirexporter.FileFhirExportSink;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportFactory;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Markus Budeus
 */
public class HeadlessFhirExporterTest extends UnitTest {

	@Mock
	private FhirExportFactory fhirExportFactory;
	@Mock
	private FileFhirExportSink fhirExport;
	@Mock
	private CommandLine commandLine;
	private DatabaseConnectionService databaseConnectionService;

	private HeadlessFhirExporter sut;

	@BeforeEach
	void setUp() {
		insertMockDependency(FhirExportFactory.class, fhirExportFactory);
		databaseConnectionService = insertDatabaseConnectionServiceMock();
		Mockito.when(fhirExportFactory.prepareExport(notNull())).thenReturn(fhirExport);
		sut = new HeadlessFhirExporter();
	}

	@Test
	void successfulRun() throws IOException {
		ExitStatus exitStatus = sut.invoke(commandLine, List.of("path" + File.separator + "to" + File.separator + "greatness"));

		assertEquals(ExitStatus.SUCCESS, exitStatus);
		verify(fhirExportFactory).prepareExport(eq(Path.of("path", "to", "greatness")));
		verify(fhirExport).doExport(notNull());
	}

	@Test
	void tooManyArguments() {
		ExitStatus exitStatus = sut.invoke(commandLine, List.of("outpath", "whatIsThis"));

		assertEquals(ExitStatus.INCORRECT_USAGE, exitStatus);
	}

	@Test
	void exportPathMissing() {
		ExitStatus exitStatus = sut.invoke(commandLine, List.of());

		assertEquals(ExitStatus.INCORRECT_USAGE, exitStatus);
	}

	@Test
	void ioException() throws IOException {
		IOException exception = new IOException("The disk is full!");
		doThrow(exception).when(fhirExport).doExport(any());

		ExitStatus exitStatus = sut.invoke(commandLine, List.of("thisWillFail"));
		assertEquals(ExitStatus.ioException(exception), exitStatus);
	}

	@Test
	void accessDenied() throws IOException {
		AccessDeniedException exception = new AccessDeniedException("Nope.");
		doThrow(exception).when(fhirExport).doExport(any());

		ExitStatus exitStatus = sut.invoke(commandLine, List.of("thisWillFail"));
		assertEquals(ExitStatus.accessDenied(exception), exitStatus);
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

	@Test
	void invalidPath() {
		ExitStatus exitStatus = sut.invoke(commandLine, List.of("invalidP\0th"));

		assertEquals(ExitStatus.INVALID_PATH_CODE, exitStatus.code);
	}

}