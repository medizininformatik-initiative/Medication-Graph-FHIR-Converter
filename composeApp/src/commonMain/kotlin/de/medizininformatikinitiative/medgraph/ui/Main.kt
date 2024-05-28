package de.medizininformatikinitiative.medgraph.ui

import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

fun main() {
    UI.startUi(!hasFunctioningDatabaseConnection())
}

private fun hasFunctioningDatabaseConnection() =
    ConnectionConfiguration.getDefault().testConnection() == ConnectionConfiguration.ConnectionResult.SUCCESS
