package de.medizininformatikinitiative.medgraph.ui

import de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import de.medizininformatikinitiative.medgraph.common.logging.Level
import de.medizininformatikinitiative.medgraph.common.logging.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    LogManager.getLogger("Application").log(Level.INFO, "Application started.")

    val cmdLineExecutor = CommandLineExecutor();
    val exitStatus = cmdLineExecutor.evaluateAndExecuteCommandLineArguments(args)
    if (exitStatus.isPresent) System.exit(exitStatus.asInt)
    else UI.startUi(!hasFunctioningDatabaseConnection())
}

private fun hasFunctioningDatabaseConnection() =
    ConnectionConfiguration.getDefault().testConnection() == ConnectionConfiguration.ConnectionResult.SUCCESS
