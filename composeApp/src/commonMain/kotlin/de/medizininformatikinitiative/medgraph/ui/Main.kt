package de.medizininformatikinitiative.medgraph.ui

import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration

fun main() {
    UI.startUi(!hasFunctioningDatabaseConnection())
}

private fun hasFunctioningDatabaseConnection() =
    ConnectionConfiguration.getDefault().testConnection() == ConnectionConfiguration.ConnectionResult.SUCCESS
