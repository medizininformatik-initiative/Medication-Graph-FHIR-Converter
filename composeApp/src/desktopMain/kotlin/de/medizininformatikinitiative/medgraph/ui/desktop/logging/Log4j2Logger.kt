package de.medizininformatikinitiative.medgraph.ui.desktop.logging

import de.medizininformatikinitiative.medgraph.common.logging.Level
import de.medizininformatikinitiative.medgraph.common.logging.Logger

/**
 * [Logger]-implementation relying on Log4j2.
 *
 * @author Markus Budeus
 */
class Log4j2Logger(
    private val logger: org.apache.logging.log4j.Logger
) : Logger {
    override fun log(level: Level?, message: String?) {
        logger.log(level.toLog4jLevel(), message)
    }

    override fun log(level: Level?, message: String?, throwable: Throwable?) {
        logger.log(level.toLog4jLevel(), message, throwable)
    }

    private fun Level?.toLog4jLevel(): org.apache.logging.log4j.Level? {
        return when (this) {
            null -> null
            Level.FATAL -> org.apache.logging.log4j.Level.FATAL
            Level.ERROR -> org.apache.logging.log4j.Level.ERROR
            Level.WARN -> org.apache.logging.log4j.Level.WARN
            Level.INFO -> org.apache.logging.log4j.Level.INFO
            Level.DEBUG -> org.apache.logging.log4j.Level.DEBUG
        }
    }

}