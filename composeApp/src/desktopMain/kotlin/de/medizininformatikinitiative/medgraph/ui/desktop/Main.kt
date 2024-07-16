package de.medizininformatikinitiative.medgraph.ui.desktop

import de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor
import de.medizininformatikinitiative.medgraph.common.logging.LogManager
import de.medizininformatikinitiative.medgraph.ui.desktop.logging.Log4J2LoggerFactory

fun main(args: Array<String>) {
    LogManager.setLogFactory(Log4J2LoggerFactory())

    val cmdLineExecutor = CommandLineExecutor();
    val exitStatus = cmdLineExecutor.evaluateAndExecuteCommandLineArguments(args)
    if (exitStatus.isPresent) System.exit(exitStatus.asInt)
    else de.medizininformatikinitiative.medgraph.ui.main()
}