package de.medizininformatikinitiative.medgraph.ui.desktop

import de.medizininformatikinitiative.medgraph.common.logging.LogManager
import de.medizininformatikinitiative.medgraph.ui.desktop.logging.Log4J2LoggerFactory

fun main() {
    LogManager.setLogFactory(Log4J2LoggerFactory())
    de.medizininformatikinitiative.medgraph.ui.main()
}