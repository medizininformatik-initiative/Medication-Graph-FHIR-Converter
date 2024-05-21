package de.medizininformatikinitiative.medgraph.ui.resources

/**
 * Since the Multiplatform Resource API is experimental, this class acts as a stable container for string resources.
 *
 * @author Markus Budeus
 */
class StringRes {

    companion object {
        @JvmStatic val exit = "Exit"
        @JvmStatic val cancel = "Cancel"
        @JvmStatic val ok = "OK"
        @JvmStatic val main_menu_search_algorithm = "Search for Medications"
        @JvmStatic val main_menu_configure_db = "Configure Database Connection"
        @JvmStatic val db_connection_dialog_uri = "Connection URI"
        @JvmStatic val db_connection_dialog_user = "User"
        @JvmStatic val db_connection_dialog_password = "Password"
        @JvmStatic val db_connection_dialog_test_connection = "Test Connection"
        @JvmStatic val db_connection_dialog_test_underway = "Connection test underway..."
        @JvmStatic val db_connection_dialog_test_success = "Connection successful!"
        @JvmStatic val db_connection_dialog_test_invalid_connection_string = "The given connection URI is invalid!"
        @JvmStatic val db_connection_dialog_test_authentication_failed = "Authentication failed!"
        @JvmStatic val db_connection_dialog_test_service_unavailable = "No Neo4j Service is reachable at the given URI."
        @JvmStatic val db_connection_dialog_password_unchanged = "(unchanged)"
        @JvmStatic val query_dialog_query_text = "Query"
        @JvmStatic val query_dialog_product_query_text = "Query (products only)"
        @JvmStatic val query_dialog_substance_query_text = "Query (substances only)"
        @JvmStatic val query_dialog_parse = "Parse"
        @JvmStatic val parsed_query_dialog_product = "Product"
        @JvmStatic val parsed_query_dialog_substance = "Substance"
        @JvmStatic val parsed_query_dialog_dosages = "Dosages"
        @JvmStatic val parsed_query_dialog_amounts = "Drug amounts"
    }

}