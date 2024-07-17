package de.medizininformatikinitiative.medgraph.commandline;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an exit status for the application when running without UI.
 *
 * @author Markus Budeus
 */
public class ExitStatus {

	public static final ExitStatus SUCCESS = new ExitStatus(0, null);
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
	public static final ExitStatus INCOMPLETE_CONNECTION_DATA = new ExitStatus(5, "Incomplete connection data! The database uri, user and password must either all be provided or none.");

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
}