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

        @JvmStatic
        val title = "MMI DIZ Medication Tool";

        @JvmStatic
        val exit = "Exit"

        @JvmStatic
        val cancel = "Cancel"

        @JvmStatic
        val do_return = "Return"

        @JvmStatic
        val ok = "OK"

        @JvmStatic
        val browse = "Browse"

        @JvmStatic
        val main_menu_populate_database = "Populate Database"

        @JvmStatic
        val main_menu_fhir_exporter = "Export to MMI CDS FHIR Instances"

        @JvmStatic
        val main_menu_search_algorithm = "Search for Medications"

        @JvmStatic
        val main_menu_configure_db = "Configure Database Connection"

        @JvmStatic
        val main_menu_show_licenses = "Open-Source Licenses"

        @JvmStatic
        val db_connection_dialog_uri = "Connection URI"

        @JvmStatic
        val db_connection_dialog_user = "User"

        @JvmStatic
        val db_connection_dialog_password = "Password"

        @JvmStatic
        val db_connection_dialog_test_connection = "Test Connection"

        @JvmStatic
        val db_connection_dialog_test_underway = "Connection test underway..."

        @JvmStatic
        val db_connection_dialog_test_success = "Connection successful!"

        @JvmStatic
        val db_connection_dialog_test_invalid_connection_string = "The given connection URI is invalid!"

        @JvmStatic
        val db_connection_dialog_test_authentication_failed = "Authentication failed!"

        @JvmStatic
        val db_connection_dialog_test_internal_error = "Something went wrong. Please contact the developer."

        @JvmStatic
        val db_connection_dialog_test_service_unavailable = "No Neo4j Service is reachable at the given URI."

        @JvmStatic
        val db_connection_dialog_password_unchanged = "(unchanged)"

        @JvmStatic
        val db_connection_dialog_save_password = "Save password"

        @JvmStatic
        val query_dialog_query_text = "Query"

        @JvmStatic
        val query_dialog_expand = "Advanced..."

        @JvmStatic
        val query_dialog_product_query_text = "Product Name"

        @JvmStatic
        val query_dialog_substance_query_text = "Active Ingredient Name"

        @JvmStatic
        val query_dialog_dosage_query_text = "Dosage"

        @JvmStatic
        val query_dialog_dose_form_query_text = "Dose Form"

        @JvmStatic
        val search_engine_dialog_parse = "Parse"

        @JvmStatic
        val search_engine_dialog_execute = "Search"

        @JvmStatic
        val search_engine_dialog_parse_execute = "Parse and Search"

        @JvmStatic
        val parsed_query_dialog_product_keywords = "Product (keywords)"

        @JvmStatic
        val parsed_query_dialog_substance = "Substances"

        @JvmStatic
        val parsed_query_dialog_dosages = "Dosages"

        @JvmStatic
        val parsed_query_dialog_amounts = "Drug Amounts"

        @JvmStatic
        val parsed_query_dialog_dose_forms = "Pharmaceutical Dose Forms"

        @JvmStatic
        val parsed_query_dialog_dose_form_characteristics = "Dose Form Characteristics"

        @JvmStatic
        val query_result_too_many_matches = "Query returned {0} matches. Only the first {1} results are shown."

        @JvmStatic
        val result_mmi_id = "MMI ID: {0}"

        @JvmStatic
        val result_pzn = "PZN: {0}"

        @JvmStatic
        val judgement_result = "Result: "

        @JvmStatic
        val judgement_passed = "Passed ✅"

        @JvmStatic
        val judgement_failed = "Failed ❌"

        @JvmStatic
        val judgement_score_with_passing_score = "Score: {0}/{1}"

        @JvmStatic
        val judgement_score = "Score: {0}"

        @JvmStatic
        val transformation_no_output = "No output"

        @JvmStatic
        val transformation_outputs = "Outputs:"

        @JvmStatic
        val search_result_show_pipeline_info = "More info"

        @JvmStatic
        val pipeline_info_title = "Matching Pipeline Steps"

        @JvmStatic
        val pipeline_info_subtitle = "How this result came to be"

        @JvmStatic
        val pipeline_merge = "Merge"

        @JvmStatic
        val pipeline_merge_desc = "{0} paths were merged. Only displaying previous pipeline steps of the selected path."

        @JvmStatic
        val pipeline_merge_current_path = "Showing Path: {0}"

        @JvmStatic
        val pipeline_merge_first_path = "<<"

        @JvmStatic
        val pipeline_merge_previous_path = "<"

        @JvmStatic
        val pipeline_merge_next_path = ">"

        @JvmStatic
        val pipeline_merge_last_path = ">>"

        @JvmStatic
        val graph_db_populator_wipe_warning = "Warning: Running the import completely overwrites the database you are currently connected to!"

        @JvmStatic
        val graph_db_populator_preparing = "Preparing data import"

        @JvmStatic
        val graph_db_populator_clearing_db = "Clearing database"

        @JvmStatic
        val graph_db_populator_running_loader = "Running {0}"

        @JvmStatic
        val graph_db_populator_cleaning_up = "Cleaning up"

        @JvmStatic
        val graph_db_populator_done = "Done. The database is ready."

        @JvmStatic
        val graph_db_populator_mmi_pharmindex_path = "Path to MMI Pharmindex raw data"

        @JvmStatic
        val graph_db_populator_neo4j_import_dir = "Path to the Neo4j import directory"

        @JvmStatic
        val graph_db_populator_neo4j_import_dir_description_prefix =
            "On most systems, the default Neo4j import directory is at <NEO4J_HOME>/import, with <NEO4J_HOME> being the installation directory of Neo4j.\n" +
                "On Debian- and RPM-based systems, the default path is "

        @JvmStatic
        val graph_db_populator_neo4j_import_dir_path = "/var/lib/neo4j/import"

        @JvmStatic
        val graph_db_populator_neo4j_import_dir_description_suffix = "."

        @JvmStatic
        val graph_db_populator_amice_stoffbez_path = "Path to AMIce \"Stoffbezeichnungen Rohdaten\" file. (optional)"

        @JvmStatic
        val graph_db_populator_amice_stoffbez_description_p1 = "If included, the knowledge graph will include INNs, synonyms for substances and additional CAS numbers from this file. " +
                "It can be downloaded "

        @JvmStatic
        val graph_db_populator_amice_stoffbez_description_link_text = "here"

        @JvmStatic
        val graph_db_populator_amice_stoffbez_description_link = "https://www.bfarm.de/DE/Arzneimittel/Arzneimittelinformationen/Arzneimittel-recherchieren/Stoffbezeichnungen/_node.html"

        @JvmStatic
        val graph_db_populator_amice_stoffbez_description_p2 = ". Please note you must only provide the .csv file here, NOT the .zip file which contains it."

        @JvmStatic
        val graph_db_populator_access_denied = "Access denied: {0}"

        @JvmStatic
        val graph_db_populator_run = "Run import"

        @JvmStatic
        val fhir_exporter_invalid_output_dir = "The given output path does not point to a directory!"

        @JvmStatic
        val fhir_exporter_output_dir_occupied = "A file named {0} already exists in the output directory!"

        @JvmStatic
        val fhir_exporter_export_path = "Export Output Directory"

        @JvmStatic
        val fhir_exporter_description = "Use this tool to export MII KDS-compliant FHIR Medication, Substance and " +
            "Organization instances from the Graph Database. Specify a directory into which to export and then run " +
            "the export."

        @JvmStatic
        val fhir_exporter_do_export = "Export"

        @JvmStatic
        val fhir_exporter_missing_permissions = "Failed to write output files. Ensure you have write permissions for the provided output path."

        @JvmStatic
        val licenses_about = "Below, open source libraries as well as open data sets used in this project are listed, " +
                "as their producers/developers deserve recognition. Clicking onto a button displays the corresponding " +
                "license."

        @JvmStatic
        val licenses_license_text = "License"
        @JvmStatic
        val licenses_notice_text = "NOTICE.txt"

        val unknown_origin = "(Origin unknown)"

        @JvmStatic
        val pipeline_origin_edit_distance = "---[Edit Distance: {0}]-->"

        @JvmStatic
        val unknown_matcher_description = "Matches search terms to identifiers using an unknown technique."

        @JvmStatic
        val levenshtein_matcher_description =
            "Calculates the levenshtein distance between two terms up to a given upper bound."

        @JvmStatic
        val edit_distance_set_matcher_description = "Matches sets of strings using an edit distance metric."

        @JvmStatic
        val original_identifier = "Original Identifier"

        @JvmStatic
        val identifier_source_known_identifier = "Known identifier from the knowledge graph."

        @JvmStatic
        val identifier_source_raw_query = "Taken from raw search query."

        @JvmStatic
        val identifier_source_search_query = "Taken from refined search query."

        @JvmStatic
        val transformer_description_CollectionToLowerCase = "Make all characters lowercase:"

        @JvmStatic
        val transformer_description_IdentityTransformer = "This transformation does nothing. (Why was it used even??)"

        @JvmStatic
        val transformer_description_ListToSet = "Convert lists to sets, i.e. discard ordering:"

        @JvmStatic
        val transformer_description_MinimumTokenLength = "Remove tokens which are less than {0} characters long:"

        @JvmStatic
        val transformer_description_RemoveBlankStrings = "Remove blank tokens:"

        @JvmStatic
        val transformer_description_RemoveDosageInformation = "Remove dosage information:"

        @JvmStatic
        val transformer_description_ToLowerCase = "Make all characters lowercase:"

        @JvmStatic
        val transformer_description_TrimSpecialSuffixSymbols = "Remove special character suffices:"

        @JvmStatic
        val transformer_description_WhitespaceTokenizer = "Split along whitespaces:"

        @JvmStatic
        val transformer_description_unknown = "[Unknown Transformation]:"

        @JvmStatic
        val edit_distance = "Edit Distance: {0}"

        @JvmStatic
        val dosage_detector_title = "Dosage Detector"

        @JvmStatic
        val dosage_detector_desc = "Detects dosages in strings."


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