package de.medizininformatikinitiative.medgraph.commandline;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.InvalidPathException;
import java.util.Objects;

/**
 * Represents an exit status for the application when running without UI. This class is immutable.
 *
 * @author Markus Budeus
 */
public class ExitStatus {

	public static final ExitStatus SUCCESS = new ExitStatus(0, null);

	public static final int INTERNAL_ERROR_CODE = 1;

	public static ExitStatus internalError(Throwable t) {
		return new ExitStatus(INTERNAL_ERROR_CODE, "Something went wrong: "+t.getMessage());
	}

	/**
	 * The command line could not be parsed successfully.
	 */
	public static final ExitStatus COMMAND_LINE_PARSING_UNSUCCESSFUL = new ExitStatus(3, null);
	/**
	 * Indicates that the command line parameters do not match the usage description of the invoked utility.
	 */
	public static final ExitStatus INCORRECT_USAGE = new ExitStatus(4, null);
	/**
	 * Some options related to the database connection were provided, but some were missing.
	 */
	public static final ExitStatus INCOMPLETE_CONNECTION_DATA = new ExitStatus(5,
			"Incomplete connection data! The database uri, user and password must either all be provided or none.");
	public static final ExitStatus INVALID_DB_CONNECTION_STRING = new ExitStatus(6,
			"The provided Neo4j connection uri is invalid.");
	public static final ExitStatus NEO4J_SERVICE_UNAVAILABLE = new ExitStatus(7,
			"No Neo4j service is reachable at the configured connection uri.");
	public static final ExitStatus NEO4J_AUTHENTICATION_FAILED = new ExitStatus(8,
			"Authentication at the Neo4j database unsuccessful!");

	public static final int INVALID_PATH_CODE = 9;

	/**
	 * Builds an {@link ExitStatus} indicating that a path provided via CLI is invalid.
	 */
	public static ExitStatus invalidPath(InvalidPathException e) {
		return new ExitStatus(INVALID_PATH_CODE, "Invalid path provided! "+e.getMessage());
	}

	public static final int ACCESS_DENIED_CODE = 10;
	public static ExitStatus accessDenied(AccessDeniedException e) {
		return new ExitStatus(ACCESS_DENIED_CODE, e.getMessage());
	}

	public static final int IO_EXCEPTION_CODE = 11;

	/**
	 * Builds an {@link ExitStatus} based on an {@link IOException}.
	 */
	public static ExitStatus ioException(IOException e) {
		return new ExitStatus(IO_EXCEPTION_CODE, "An I/O operation was unsuccessful: "+e.getMessage());
	}

	/**
	 * The process exit code associated with this status.
	 */
	public final int code;
	/**
	 * A message for the user describing this status.
	 */
	@Nullable
	public final String message;

	public ExitStatus(int code, @Nullable String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		ExitStatus that = (ExitStatus) object;
		return code == that.code && Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, message);
	}

	@Override
	public String toString() {
		return "Status "+code+": "+message;
	}
}
