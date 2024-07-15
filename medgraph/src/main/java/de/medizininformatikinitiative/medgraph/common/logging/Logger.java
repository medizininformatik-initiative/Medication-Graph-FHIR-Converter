package de.medizininformatikinitiative.medgraph.common.logging;


import org.apache.commons.logging.Log;

import java.util.function.Function;

/**
 * Platform-independent interface used for logging.
 *
 * @author Markus Budeus
 */
public interface Logger {

	void log(Level level, String message);
	void log(Level level, String message, Throwable throwable);

}
