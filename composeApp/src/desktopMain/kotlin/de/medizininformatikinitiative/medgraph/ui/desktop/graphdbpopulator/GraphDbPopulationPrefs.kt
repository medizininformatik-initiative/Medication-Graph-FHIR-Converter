package de.medizininformatikinitiative.medgraph.ui.desktop.graphdbpopulator

import de.medizininformatikinitiative.medgraph.ApplicationPreferences.getApplicationRootNode

/**
 * @author Markus Budeus
 */
class GraphDbPopulationPrefs {

    private constructor()

    companion object {
        private const val PATH = "graphdbpopulation"
        private const val MMI_PHARMINDEX_LOCATION = "mmiPharmindexData"
        private const val NEO4J_IMPORT_DIR = "neo4jImportDirectory"
        private const val AMICE_FILE_PATH = "amiceFilePath"
        private const val INCLUDE_ARCHIVE = "includeArchive"

        val INSTANCE: GraphDbPopulationPrefs by lazy { GraphDbPopulationPrefs() }

    }

    private val prefs = getApplicationRootNode().node(PATH)

    var mmiPharmindexDataLocation: String = ""
        private set
    var neo4jImportDir: String = ""
        private set
    var amiceFilePath: String = ""
        private set
    var includeArchive: Boolean
        private set

    init {
        mmiPharmindexDataLocation = prefs.get(MMI_PHARMINDEX_LOCATION, "")
        neo4jImportDir = prefs.get(NEO4J_IMPORT_DIR, "")
        amiceFilePath = prefs.get(AMICE_FILE_PATH, "")
        includeArchive = prefs.getBoolean(INCLUDE_ARCHIVE, true)
    }

    fun update(
        mmiPharmindexDataLocation: String,
        neo4jImportDir: String,
        amiceFilePath: String,
        includeArchive: Boolean,
    ) {
        this.mmiPharmindexDataLocation = mmiPharmindexDataLocation
        this.neo4jImportDir = neo4jImportDir
        this.amiceFilePath = amiceFilePath
        this.includeArchive = includeArchive

        prefs.put(MMI_PHARMINDEX_LOCATION, mmiPharmindexDataLocation)
        prefs.put(NEO4J_IMPORT_DIR, neo4jImportDir)
        prefs.put(AMICE_FILE_PATH, amiceFilePath)
        prefs.putBoolean(INCLUDE_ARCHIVE, includeArchive)
        prefs.flush()
    }

}