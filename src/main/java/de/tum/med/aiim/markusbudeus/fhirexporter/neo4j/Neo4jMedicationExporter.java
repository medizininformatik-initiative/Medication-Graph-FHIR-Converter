package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication.Medication;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.Substance;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.CODE_LABEL;

public class Neo4jMedicationExporter {

	private static final String CODE = "code";
	private static final String SYSTEM_URI = "uri";
	private static final String SYSTEM_DATE = "date";
	private static final String SYSTEM_VERSION = "version";

	private final Session session;

	public Neo4jMedicationExporter(Session session) {
		this.session = session;
	}

	public static void main(String[] args) {
		try (DatabaseConnection connection = new DatabaseConnection();
		Session session = connection.createSession()){
			Neo4jMedicationExporter exporter = new Neo4jMedicationExporter(session);
			exporter.loadMedications().forEach(med -> {});
		}
	}

	/**
	 * Reads all medications with their assigned codes and coding systems as well as ingredients and dose form from the
	 * database and returns them as a stream of {@link Substance Substances}.
	 */
	public Stream<Medication> loadMedications() {
		Result result = session.run(new Query(
				// This is a complicated query. Sorry about that. :(
				"MATCH (p:" + PRODUCT_LABEL + " {name: 'Aspirin® N 100 mg, Tablette'})-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ")" +
						"-[:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(df:" + DOSE_FORM_LABEL + ") " +
						"OPTIONAL MATCH (df)-[:" + DOSE_FORM_IS_EDQM + "]->(de:" + EDQM_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(dfcs:" + CODING_SYSTEM_LABEL + ") " +
						"MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:" + MMI_INGREDIENT_LABEL + ")-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(iu:" + UNIT_LABEL + ") " +
						"MATCH (i)-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s:" + SUBSTANCE_LABEL + ") " +
						"WITH p, d, df," +
						"CASE WHEN de IS NOT NULL THEN " + groupCodingSystem("de", "dfcs", "name:de.name,") +
						" ELSE null END AS edqmDoseForm, " +
						"collect({" +
						"substanceMmiId:i.mmiId," +
						"massFrom:i.massFrom," +
						"massTo:i.massTo," +
						"unitUcumCs:iu.ucumCs," +
						"unitMmi:iu.mmiName" +
						"}) AS ingredients " +
						"OPTIONAL MATCH (d)-[:" + DRUG_MATCHES_ATC_CODE_LABEL + "]->(a:" + ATC_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(acs:" + CODING_SYSTEM_LABEL + ") " +
						"WITH p, d, df, ingredients, collect(" + groupCodingSystem("a", "acs") + ") AS atcCodes, edqmDoseForm " +
						"WITH p, collect({" +
						"ingredients:ingredients," +
						"atcCodes:atcCodes," +
						"mmiDoseForm:df.mmiName," +
						"edqmDoseForm:edqmDoseForm" +
						"}) AS drugs " +
						"MATCH (pcs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(pc:" + CODE_LABEL + ")-->(p) " +
						"RETURN p.name AS productName, collect(" + groupCodingSystem("pc", "pcs") + ") AS productCodes, drugs"
		));

		return result.stream().map(Neo4jMedicationExporter::toMedication);
	}

	private static Medication toMedication(Record record) {
		Neo4jExportProduct exportProduct = new Neo4jExportProduct(record);

		Medication medication = new Medication();



		return medication;
	}

	private static class Neo4jExportProduct {
		final String name;
		final List<Neo4jExportCode> codes;
		final List<Neo4jExportDrug> drugs;

		Neo4jExportProduct(MapAccessorWithDefaultValue value) {
			name = value.get("productName", (String) null);
			codes = value.get("productCodes").asList(Neo4jExportCode::new);
			drugs = value.get("drugs").asList(Neo4jExportDrug::new);
		}
	}

	private static class Neo4jExportDrug {
		final List<Neo4jExportIngredient> ingredients;
		final List<Neo4jExportCode> atcCodes;
		final String mmiDoseForm;
		final Neo4jExportEdqm edqmDoseForm;

		Neo4jExportDrug(MapAccessorWithDefaultValue value) {
			ingredients = value.get("ingredients").asList(Neo4jExportIngredient::new);
			atcCodes = value.get("atcCodes").asList(Neo4jExportCode::new);
			mmiDoseForm = value.get("mmiDoseForm", (String) null);
			Value edqm = value.get("edqmDoseForm");
			if (edqm.isNull()) {
				edqmDoseForm = null;
			} else {
				edqmDoseForm = new Neo4jExportEdqm(edqm);
			}
		}
	}

	private static class Neo4jExportIngredient {
		final long substanceMmiId;
		final String massFrom;
		final String massTo;
		final String unitUcumCs;
		final String unitMmi;

		private Neo4jExportIngredient(MapAccessorWithDefaultValue value) {
			substanceMmiId = value.get("substanceMmiId").asLong();
			massFrom = value.get("massFrom", (String) null);
			massTo = value.get("massTo", (String) null);
			unitUcumCs = value.get("unitUcumCs", (String) null);
			unitMmi = value.get("unitMmi", (String) null);
		}
	}

	private static class Neo4jExportCode {
		final String code;
		final String codingSystemUri;
		final String codingSystemVersion;
		final LocalDate codingSystemDate;

		public Neo4jExportCode(MapAccessorWithDefaultValue value) {
			code = value.get(CODE, (String) null);
			codingSystemUri = value.get(SYSTEM_URI, (String) null);
			codingSystemVersion = value.get(SYSTEM_VERSION, (String) null);
			codingSystemDate = (LocalDate) value.get(SYSTEM_DATE, (LocalDate) null);
		}
	}

	private static class Neo4jExportEdqm extends Neo4jExportCode {

		final String name;

		protected Neo4jExportEdqm(MapAccessorWithDefaultValue value) {
			super(value);
			this.name = value.get("name", (String) null);
		}
	}

	/**
	 * Returns a Cypher statement which groups codes into a single value object where the code node is referred to by
	 * the given codeVariableName and the assigned codingSystem node is referred to by the given
	 * codingSystemVariableName. The collection happens in a way that the {@link Neo4jExportCode} can read the resulting
	 * value.
	 */
	private String groupCodingSystem(String codeVariableName, String codingSystemVariableName) {
		return groupCodingSystem(codeVariableName, codingSystemVariableName, "");
	}

	/**
	 * Returns a Cypher statement which groups codes into a single value object where the code node is referred to by
	 * the given codeVariableName and the assigned codingSystem node is referred to by the given
	 * codingSystemVariableName. The collection happens in a way that the {@link Neo4jExportCode} can read the resulting
	 * value. Additionally, you can add more properties to the resulting object using the given "extra" parameter.
	 */
	private String groupCodingSystem(String codeVariableName, String codingSystemVariableName, String extra) {
		return "{" + extra +
				CODE + ":" + codeVariableName + ".code," +
				SYSTEM_URI + ":" + codingSystemVariableName + ".uri," +
				SYSTEM_DATE + ":" + codingSystemVariableName + ".date," +
				SYSTEM_VERSION + ":" + codingSystemVariableName + ".version" +
				"}";
	}

}
