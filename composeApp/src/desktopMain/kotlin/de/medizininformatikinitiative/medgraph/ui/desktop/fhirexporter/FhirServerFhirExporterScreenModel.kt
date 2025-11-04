package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.medizininformatikinitiative.medgraph.DI
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSinkFactory
import kotlinx.coroutines.Job

/**
 * [FhirExporterScreenModel]-implementation used when exporting to a FHIR server via RESTful API.
 *
 * @author Markus Budeus
 */
class FhirServerFhirExporterScreenModel(
    private val exporterFactory: FhirServerExportSinkFactory = DI.get(FhirServerExportSinkFactory::class.java)
) : FhirExporterScreenModel() {

    var fhirBaseUrl by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var bearerToken by mutableStateOf("")

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

    private fun checkUrl(): String? = getNotBlank(fhirBaseUrl)

    private fun checkUsername(): String? = getNotBlank(username)

    private fun checkPassword(): CharArray? = getNotBlank(password)?.toCharArray()

    private fun checkToken(): String? = getNotBlank(bearerToken)

    private fun getNotBlank(source: String): String? {
        if (!source.isBlank()) return source
        return null
    }

}