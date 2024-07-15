package de.medizininformatikinitiative.medgraph.ui.desktop.logging

import de.medizininformatikinitiative.medgraph.common.logging.Logger
import de.medizininformatikinitiative.medgraph.common.logging.LoggerFactory
import org.apache.logging.log4j.LogManager

/**
 * Factory class which produces [Log4j2Logger]-instances.
 *
 * @author Markus Budeus
 */
class Log4J2LoggerFactory : LoggerFactory {
    override fun getLogger(clazz: Class<*>?): Logger {
        return Log4j2Logger(LogManager.getLogger(clazz))
    }
}