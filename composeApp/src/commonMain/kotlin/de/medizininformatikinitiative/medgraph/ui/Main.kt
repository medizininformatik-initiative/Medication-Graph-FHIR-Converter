package de.medizininformatikinitiative.medgraph.ui

import de.medizininformatikinitiative.medgraph.DI
import de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfigurationService
import de.medizininformatikinitiative.medgraph.common.db.ConnectionResult
import de.medizininformatikinitiative.medgraph.common.logging.Level
import de.medizininformatikinitiative.medgraph.common.logging.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

fun main() {
    LogManager.getLogger("Application").log(Level.INFO, "Application started.")
    UI.startUi(!hasFunctioningDatabaseConnection())
}

private fun hasFunctioningDatabaseConnection() =
    DI.get(ConnectionConfigurationService::class.java).connectionConfiguration.testConnection() == ConnectionResult.SUCCESS
