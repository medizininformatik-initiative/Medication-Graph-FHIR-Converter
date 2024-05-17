package de.medizininformatikinitiative.medgraph.ui.resources

/**
 * Since the Multiplatform Resource API is experimental, this class acts as a stable container for string resources.
 *
 * @author Markus Budeus
 */
class StringRes {

    companion object {
        @JvmStatic val cancel: String = "Cancel"
        @JvmStatic val ok: String = "OK"
        @JvmStatic val db_connection_dialog_uri: String = "Connection URI"
        @JvmStatic val db_connection_dialog_user: String = "User"
        @JvmStatic val db_connection_dialog_password: String = "Password"
        @JvmStatic val db_connection_dialog_test_connection: String = "Test Connection"
        @JvmStatic val db_connection_dialog_test_underway: String = "Connection test underway..."
        @JvmStatic val db_connection_dialog_test_success: String = "Connection successful!"
        @JvmStatic val db_connection_dialog_test_invalid_connection_string: String = "The given connection URI is invalid!"
        @JvmStatic val db_connection_dialog_test_authentication_failed: String = "Authentication failed!"
        @JvmStatic val db_connection_dialog_test_service_unavailable: String = "No Neo4j Service is reachable at the given URI."
        @JvmStatic val db_connection_dialog_password_unchanged: String = "(unchanged)"
    }

}