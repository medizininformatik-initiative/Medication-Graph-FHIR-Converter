package de.medizininformatikinitiative.medgraph.common.logging;

import org.jetbrains.annotations.NotNull;

/**
 * Class which provides access to {@link Logger} instances.
 *
 * @author Markus Budeus
 */
public class LogManager {

	private static LoggerFactory loggerFactory = SystemOutLogger.FACTORY;

	/**
	 * Creates a logger for the given class.
	 *
	 * @param clazz the class by which the logger is meant to be used
	 * @return a {@link Logger}, never null
	 */
	@NotNull
	public static Logger get(Class<?> clazz) {
		return loggerFactory.getLogger(clazz);
	}

	/**
	 * Sets the log factory to use for acquiring loggers.
	 *
	 * @param loggerFactory the log factory to use
	 */
	public static void setLogFactory(@NotNull LoggerFactory loggerFactory) {
		LogManager.loggerFactory = loggerFactory;
	}

}
