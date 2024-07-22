package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionFailureReason;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionException;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulation;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulationFactory;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Markus Budeus
 */
public class HeadlessGraphDbPopulatorTest extends UnitTest {

	@Mock
	private GraphDbPopulationFactory factory;
	@Mock
	private GraphDbPopulation population;
	@Mock
	private CommandLine commandLine;
	private DatabaseConnectionService databaseConnectionService;

	private HeadlessGraphDbPopulator sut;

	@BeforeEach
	void setUp() {
		insertMockDependency(GraphDbPopulationFactory.class, factory);
		insertMockDependency(DatabaseConnectionService.class, databaseConnectionService);
		Mockito.when(factory.prepareDatabasePopulation(notNull(), notNull(), any())).thenReturn(population);
		databaseConnectionService = insertDatabaseConnectionServiceMock();
		sut = new HeadlessGraphDbPopulator();
	}

	@Test
	void notEnoughArgs() {
		assertEquals(ExitStatus.INCORRECT_USAGE, sut.invoke(commandLine, List.of("A")));
	}

	@Test
	void tooManyArgs() {
		assertEquals(ExitStatus.INCORRECT_USAGE, sut.invoke(commandLine, List.of("A", "B", "C", "D")));
	}

	@Test
	void correctPathsWithoutAmice() {
		assertEquals(ExitStatus.SUCCESS, sut.invoke(commandLine, List.of("A", "B")));
		verify(factory).prepareDatabasePopulation(eq(Path.of("A")), eq(Path.of("B")), isNull());
	}

	@Test
	void correctPathsWithAmice() {
		assertEquals(ExitStatus.SUCCESS, sut.invoke(commandLine, List.of("A", "B", "C/d.txt")));
		verify(factory).prepareDatabasePopulation(eq(Path.of("A")), eq(Path.of("B")), eq(Path.of("C", "d.txt")));
	}

	@Test
	void invalidMmiPath() {
		assertEquals(ExitStatus.INVALID_PATH_CODE, sut.invoke(commandLine,
				List.of("Inval\0id", "Valid", "Also/Valid")).code);
	}

	@Test
	void invalidNeo4jPath() {
		assertEquals(ExitStatus.INVALID_PATH_CODE, sut.invoke(commandLine,
				List.of("", "N\0pe")).code);
	}

	@Test
	void invalidAmicePath() {
		assertEquals(ExitStatus.INVALID_PATH_CODE, sut.invoke(commandLine,
				List.of("This", "is", "n\0t fine")).code);
	}

	@ParameterizedTest
	@EnumSource
	void neo4jUnreachable(ConnectionFailureReason failureReason) throws DatabaseConnectionException {
		RuntimeException cause = new RuntimeException("RIP");
		when(databaseConnectionService.createConnection(true)).thenThrow(
				new DatabaseConnectionException(failureReason, "Ooops...", cause));

		ExitStatus exitStatus = sut.invoke(commandLine, List.of("A", "2", "5"));

		switch (failureReason) {
			case INVALID_CONNECTION_STRING -> assertEquals(exitStatus, ExitStatus.INVALID_DB_CONNECTION_STRING);
			case SERVICE_UNAVAILABLE -> assertEquals(exitStatus, ExitStatus.NEO4J_SERVICE_UNAVAILABLE);
			case AUTHENTICATION_FAILED -> assertEquals(exitStatus, ExitStatus.NEO4J_AUTHENTICATION_FAILED);
			case INTERNAL_ERROR -> assertEquals(exitStatus, ExitStatus.internalError(cause));
		}
	}

	@Test
	void accessDenied() throws IOException {
		AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied! LOL!");
		doThrow(accessDeniedException).when(population).executeDatabasePopulation(notNull());

		assertEquals(ExitStatus.accessDenied(accessDeniedException), sut.invoke(commandLine, List.of("1", "2", "3")));
	}

	@Test
	void randomIOException() throws IOException {
		IOException ioOutOfOrder = new IOException("Yeah no I'm not motivated today.");
		doThrow(ioOutOfOrder).when(population).executeDatabasePopulation(notNull());

		assertEquals(ExitStatus.ioException(ioOutOfOrder), sut.invoke(commandLine, List.of("1", "2", "3")));
	}

}