package de.medizininformatikinitiative.medgraph.ui

import de.medizininformatikinitiative.medgraph.DI
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfigurationService
import de.medizininformatikinitiative.medgraph.common.db.ConnectionFailureReason
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionException
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService
import de.medizininformatikinitiative.medgraph.common.logging.Level
import de.medizininformatikinitiative.medgraph.common.logging.LogManager

object Application {
    const val NAME = "Medication Graph FHIR Converter"
}

fun main() {
    LogManager.getLogger("Application").log(Level.INFO, "Application started.")
    UI.startUi(!hasFunctioningDatabaseConnection())
}

private fun hasFunctioningDatabaseConnection(): Boolean {
    try {
        DI.get(DatabaseConnectionService::class.java).verifyConnection()
        return true
    } catch (e: DatabaseConnectionException) {
        return false
    }
}
