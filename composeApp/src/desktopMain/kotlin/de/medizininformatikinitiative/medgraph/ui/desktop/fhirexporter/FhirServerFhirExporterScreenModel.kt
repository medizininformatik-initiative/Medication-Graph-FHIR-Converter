package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.runtime.mutableStateOf
import de.medizininformatikinitiative.medgraph.DI
import de.medizininformatikinitiative.medgraph.common.db.Neo4jTransactionMemoryLimitTest
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSinkFactory
import kotlinx.coroutines.Job

/**
 * [FhirExporterScreenModel]-implementation used when exporting to a FHIR server via RESTful API.
 *
 * @author Markus Budeus
 */
class FhirServerFhirExporterScreenModel(
    private val exporterFactory: FhirServerExportSinkFactory = DI.get(FhirServerExportSinkFactory::class.java),
    neo4jTransactionMemoryLimitTest: Neo4jTransactionMemoryLimitTest? = DI.get(Neo4jTransactionMemoryLimitTest::class.java)
) : FhirExporterScreenModel(neo4jTransactionMemoryLimitTest) {

    var fhirBaseUrl = mutableStateOf("http://localhost:8080/fhir")
    var username = mutableStateOf("")
    var password = mutableStateOf("")
    var bearerToken = mutableStateOf("")

    fun exportNoAuth(): Job? {
        return checkUrl()?.let { super.doExport(exporterFactory.prepareExportWithoutAuth(it)) }
    }

    fun exportBasicAuth(): Job? {
        return checkUrl()?.let { url ->
            checkUsername()?.let { username ->
                checkPassword()?.let { password ->
                    super.doExport(exporterFactory.prepareExportWithHttpBasicAuth(url, username, password))
                }
            }
        }
    }

    fun exportTokenAuth(): Job? {
        return checkUrl()?.let { url ->
            checkToken()?.let { token ->
                super.doExport(exporterFactory.prepareExportWithTokenAuth(url, token))
            }
        }
    }

    private fun checkUrl(): String? = getNotBlank(fhirBaseUrl.value)

    private fun checkUsername(): String? = getNotBlank(username.value)

    private fun checkPassword(): CharArray? = getNotBlank(password.value)?.toCharArray()

    private fun checkToken(): String? = getNotBlank(bearerToken.value)

    private fun getNotBlank(source: String): String? {
        if (!source.isBlank()) return source
        return null
    }

}