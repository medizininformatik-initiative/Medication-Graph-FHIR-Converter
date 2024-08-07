package de.medizininformatikinitiative.medgraph.common.logging;

import org.jetbrains.annotations.NotNull;

/**
 * Factory class for {@link Logger} instances.
 *
 * @author Markus Budeus
 */
public interface LoggerFactory {

	/**
	 * Creates a logger for the given class.
	 * @param clazz the class by which the logger is meant to be used
	 * @return a {@link Logger}, never null
	 */
	@NotNull
	Logger getLogger(Class<?> clazz);

	/**
	 * Creates a logger with the given tag.
	 * @param tag the tag to assign to the logger
	 * @return a {@link Logger}, never null
	 */
	@NotNull
	Logger getLogger(String tag);

}
