package de.medizininformatikinitiative.medgraph.ui.resources

import java.text.DecimalFormat

/**
 * Since the Multiplatform Resource API is experimental, this class acts as a stable container for string resources.
 *
 * @author Markus Budeus
 */
class StringRes {

    companion object {
        private val decimalFormat = DecimalFormat("0.0")

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
        @JvmStatic val search_engine_dialog_parse = "Parse"
        @JvmStatic val search_engine_dialog_execute = "Search"
        @JvmStatic val search_engine_dialog_parse_execute = "Parse and Search"
        @JvmStatic val parsed_query_dialog_product = "Product"
        @JvmStatic val parsed_query_dialog_substance = "Substance"
        @JvmStatic val parsed_query_dialog_dosages = "Dosages"
        @JvmStatic val parsed_query_dialog_amounts = "Drug amounts"
        @JvmStatic val query_result_too_many_matches = "Query returned {0} matches. Only the first {1} results are shown."
        @JvmStatic val result_mmi_id = "MMI ID: {0}"
        @JvmStatic val result_pzn = "PZN: {0}"
        @JvmStatic val judgement_result = "Result: "
        @JvmStatic val judgement_passed = "Passed ✅"
        @JvmStatic val judgement_failed = "Failed ❌"
        @JvmStatic val judgement_score_with_passing_score = "Score: {0}/{1}"
        @JvmStatic val judgement_score = "Score: {0}"
        @JvmStatic val transformation_no_output = "No output"
        @JvmStatic val transformation_outputs = "Outputs:"
        @JvmStatic val search_result_show_pipeline_info = "More info"
        @JvmStatic val pipeline_info_title = "Matching Pipeline Steps"
        @JvmStatic val pipeline_info_subtitle = "How this result came to be"

        @JvmStatic
        fun get(template: String, vararg arguments: Any): String {
            var actual = template
            arguments.forEachIndexed { index, arg ->
                actual = actual.replace("{$index}", arg.toString())
            }
            return actual
        }

        @JvmStatic
        fun parseDecimal(double: Double): String = decimalFormat.format(double)
    }

}