package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.*;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication.Ingredient;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication.Medication;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication.MedicationReference;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication.Meta;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization.OrganizationReference;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.Substance;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.SubstanceReference;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.CodingSystem;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class Neo4jMedicationExporter extends Neo4jExporter<Medication> {

	public static final String CODE = "code";
	public static final String SYSTEM_URI = "uri";
	public static final String SYSTEM_DATE = "date";
	public static final String SYSTEM_VERSION = "version";

	private static final String UCUM_SYSTEM = "http://unitsofmeasure.org";

	private final boolean allowMedicationsWithoutIngredients;
	private final boolean collectStatistics;
	private Statistics statistics;

	public Neo4jMedicationExporter(Session session, boolean allowMedicationsWithoutIngredients,
	                               boolean collectStatistics) {
		super(session);
		this.allowMedicationsWithoutIngredients = allowMedicationsWithoutIngredients;
		this.collectStatistics = collectStatistics;
	}

	/**
	 * Reads all medications with their assigned codes and coding systems as well as ingredients and dose form from the
	 * database and returns them as a stream of {@link Substance Substances}.
	 */
	@Override
	public Stream<Medication> exportObjects() {
		if (collectStatistics) {
			this.statistics = new Statistics();
		} else {
			this.statistics = null;
		}

		// This is a complicated query. Sorry about that. :(
		String query =
				"MATCH (p:" + PRODUCT_LABEL + ")-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ") " +
						"OPTIONAL MATCH (d)-[:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(df:" + DOSE_FORM_LABEL + ") " +
						"OPTIONAL MATCH (df)-[:" + DOSE_FORM_IS_EDQM + "]->(de:" + EDQM_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(dfcs:" + CODING_SYSTEM_LABEL + ") " +
						(allowMedicationsWithoutIngredients ? "OPTIONAL " : "") +
						"MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:" + MMI_INGREDIENT_LABEL + ")-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s:" + SUBSTANCE_LABEL + ") " +
						"OPTIONAL MATCH (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(iu:" + UNIT_LABEL + ") " +
						"WITH p, d, df," +
						"CASE WHEN de IS NOT NULL THEN " + groupCodingSystem("de", "dfcs", "name:de.name") +
						" ELSE null END AS edqmDoseForm, " +
						"collect(CASE WHEN s IS NOT NULL THEN {" +
						"substanceMmiId:s.mmiId," +
						"substanceName:s.name," +
						"isActive:i.isActive," +
						"massFrom:i.massFrom," +
						"massTo:i.massTo," +
						"unit:iu" +
						"} ELSE null END) AS ingredients " +
						"OPTIONAL MATCH (d)-[:" + DRUG_MATCHES_ATC_CODE_LABEL + "]->(a:" + ATC_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(acs:" + CODING_SYSTEM_LABEL + ") " +
						"WITH p, d, df, ingredients, " +
						"collect(" + groupCodingSystem("a", "acs", "description:a.description") +
						") AS atcCodes, edqmDoseForm " +
						"OPTIONAL MATCH (d)-[:" + DRUG_HAS_UNIT_LABEL + "]->(du:" + UNIT_LABEL + ") " +
						"WITH p, collect({" +
						"ingredients:ingredients," +
						"atcCodes:atcCodes," +
						"mmiDoseForm:df.mmiName," +
						"edqmDoseForm:edqmDoseForm," +
						"amount:d.amount," +
						"unit:du" +
						"}) AS drugs " +
						"OPTIONAL MATCH (p)<-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]-(pk:" + PACKAGE_LABEL + ") " +
						"OPTIONAL MATCH (pkcs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(pkc:" + CODE_LABEL + ")-->(pk) " +
						"WITH p, drugs, pk, collect(" + groupCodingSystem("pkc", "pkcs") + ") as packageCodes " +
						"WITH p, drugs, collect({" +
						"name:pk.name," +
						"amount:pk.amount," +
						"onMarketDate:pk.onMarketDate," +
						"codes:packageCodes" +
						"}) as packages " +
						"OPTIONAL MATCH (pcs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(pc:" + CODE_LABEL + ")-->(p) " +
						"OPTIONAL MATCH (c:" + COMPANY_LABEL + ")-[:" + MANUFACTURES_LABEL + "]->(p) " +
						"RETURN p.name AS productName," +
						"p.mmiId AS mmiId," +
						"c.mmiId AS companyMmiId," +
						"c.name AS companyName," +
						"collect(" + groupCodingSystem("pc", "pcs") + ") AS productCodes, " +
						"drugs, " +
						"packages";

//		System.out.println(query);

		Result result = session.run(new Query(query));

		Stream<Medication> stream = result.stream().flatMap(Neo4jMedicationExporter::toMedication)
		                                  .filter(Objects::nonNull);
		if (collectStatistics) {
			stream = stream.map(this::addToStatistics);
		}
		return stream;
	}

	private Medication addToStatistics(Medication medication) {
		statistics.add(medication);
		return medication;
	}

	private static Stream<Medication> toMedication(Record record) {
		Neo4jExportProduct exportProduct = new Neo4jExportProduct(record);

		if (exportProduct.drugs.isEmpty()) {
			System.err.println("Skipping product '" + exportProduct.name + "', because it contains no drugs!");
			return null;
		}

		Medication medication = createMedication();
		applyProductInfoToMedication(exportProduct, medication);

		if (exportProduct.drugs.size() == 1) {
			// Simple case. Simply merge product and drug information into medication object
			applyDrugInfoToMedication(exportProduct.drugs.get(0), medication);
			return Stream.of(medication);
		} else {
			// Tougher case. We need a medication object for each drug and a "parent" medication object.

			List<Neo4jExportDrug> drugs = exportProduct.drugs;
			List<Ingredient> ingredients = new ArrayList<>();
			List<Medication> childMedicationObjects = new ArrayList<>();
			for (int i = 0; i < drugs.size(); i++) {
				int childNo = i + 1;
				Medication childMedication = createMedication();
				childMedication.identifier = new Identifier[]{
						Identifier.combinedMedicalProductSubproductIdentifier(exportProduct.mmiId, childNo,
								exportProduct.companyMmiId)
				};
				applyManufacturerInfoToMedication(exportProduct, childMedication);
				applyDrugInfoToMedication(drugs.get(i), childMedication);

				Ingredient childMedIngredient = new Ingredient();
				childMedIngredient.isActive = null;
				childMedIngredient.itemReference = new MedicationReference(exportProduct.mmiId, childNo,
						exportProduct.companyMmiId);
				ingredients.add(childMedIngredient);
				childMedicationObjects.add(childMedication);
			}
			medication.ingredient = ingredients.toArray(new Ingredient[0]);
			Ratio parentAmount = null;
			for (Medication child : childMedicationObjects) {
				if (parentAmount == null) parentAmount = child.amount;
				else {
					parentAmount = parentAmount.plus(child.amount);
					if (parentAmount == null) {
						System.err.println(
								"Combined preparation " + exportProduct.name + " contains ingredients with incompatible units. Parent Medication will contain no amount data.");
						break;
					}
				}
			}
			medication.amount = parentAmount;

			List<Medication> allMedications = new ArrayList<>(childMedicationObjects);
			allMedications.add(medication);

			return Stream.of(allMedications.toArray(new Medication[0]));
		}
	}

	private static Medication createMedication() {
		Medication medication = new Medication();
		Meta meta = new Meta();
		// TODO Somehow reference my Graph DB in meta.source?
		meta.profile = new String[]{"https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/Medication"};
		meta.source = "https://www.mmi.de/mmi-pharmindex/mmi-pharmindex-daten";
		medication.meta = meta;
		return medication;
	}

	private static void applyProductInfoToMedication(Neo4jExportProduct product, Medication target) {
		List<Coding> codings = getCodings(target);
		codings.addAll(product.codes.stream().map(Neo4jExportCode::toCoding).toList()); // Add product codes
		product.packages.forEach(
				p -> codings.addAll(p.codes.stream().map(Neo4jExportCode::toCoding).toList())); // Add package codes
		applyCodings(codings, target);

		target.code.text = product.name;
		applyManufacturerInfoToMedication(product, target);

		target.identifier = new Identifier[]{Identifier.fromProductAndOrganizationMmiId(product.mmiId,
				product.companyMmiId)};
	}

	private static void applyDrugInfoToMedication(Neo4jExportDrug drug, Medication target) {
		target.form = new CodeableConcept();
		if (drug.edqmDoseForm != null) {
			Coding edqmCoding = drug.edqmDoseForm.toCoding();
			edqmCoding.display = drug.edqmDoseForm.name;
			target.form.coding = new Coding[]{edqmCoding};
			target.form.text = drug.edqmDoseForm.name;
		} else {
			target.form.text = drug.mmiDoseForm;
		}

		target.amount = ratioFromMassAndUnit(drug.amount, null, drug.unit);

		List<Ingredient> ingredients = drug.ingredients.stream().map(exportIngredient -> {
			Ingredient ingredient = new Ingredient();
			ingredient.isActive = exportIngredient.isActive;
			ingredient.itemReference = new SubstanceReference(exportIngredient.substanceMmiId,
					exportIngredient.substanceName);
			ingredient.strength = exportIngredient.getStrength();
			return ingredient;
		}).toList();

		target.ingredient = ingredients.toArray(new Ingredient[0]);

		List<Coding> codings = getCodings(target);
		codings.addAll(drug.atcCodes.stream().map(Neo4jExportCode::toCoding).toList());
		applyCodings(codings, target);
	}

	private static void applyManufacturerInfoToMedication(Neo4jExportProduct product, Medication target) {
		if (product.companyMmiId != null) {
			target.manufacturer = new OrganizationReference(product.companyMmiId, product.companyName);
		}
	}

	private static List<Coding> getCodings(Medication medication) {
		if (medication.code == null) {
			return new ArrayList<>();
		}
		List<Coding> codings;
		if (medication.code.coding == null) {
			codings = new ArrayList<>();
		} else {
			codings = new java.util.ArrayList<>(List.of(medication.code.coding));
		}
		return codings;
	}

	private static void applyCodings(List<Coding> codings, Medication medication) {
		if (medication.code == null) {
			medication.code = new CodeableConcept();
		}
		medication.code.coding = codings.toArray(new Coding[0]);
	}

	public void printStatistics() {
		System.out.println(statistics);
	}

	private static class Neo4jExportProduct {
		final String name;
		final long mmiId;
		final Long companyMmiId;
		final String companyName;
		final List<Neo4jExportCode> codes;
		final List<Neo4jExportDrug> drugs;
		final List<Neo4jExportPackage> packages;

		Neo4jExportProduct(MapAccessorWithDefaultValue value) {
			name = value.get("productName", (String) null);
			mmiId = value.get("mmiId").asLong();
			companyMmiId = (Long) value.get("companyMmiId", (Long) 0L);
			companyName = value.get("companyName", (String) null);
			codes = value.get("productCodes").asList(Neo4jExportCode::new);
			drugs = value.get("drugs").asList(Neo4jExportDrug::new);
			packages = value.get("packages").asList(Neo4jExportPackage::new);
		}
	}

	private static class Neo4jExportPackage {
		final String name;
		final BigDecimal amount;
		final LocalDate onMarketDate;
		final List<Neo4jExportCode> codes;

		Neo4jExportPackage(MapAccessorWithDefaultValue value) {
			name = value.get("name").asString(null);
			amount = toBigDecimal(value.get("amount").asString(null));
			onMarketDate = value.get("onMarketDate").asLocalDate(null);
			codes = value.get("codes").asList(Neo4jExportCode::new);
		}
	}

	private static class Neo4jExportDrug {
		final List<Neo4jExportIngredient> ingredients;
		final List<Neo4jExportAtc> atcCodes;
		final String mmiDoseForm;
		final Neo4jExportEdqm edqmDoseForm;
		final BigDecimal amount;
		final Neo4jExportUnit unit;

		Neo4jExportDrug(MapAccessorWithDefaultValue value) {
			ingredients = value.get("ingredients").asList(Neo4jExportIngredient::new);
			atcCodes = value.get("atcCodes").asList(Neo4jExportAtc::new);
			mmiDoseForm = value.get("mmiDoseForm", (String) null);
			Value edqm = value.get("edqmDoseForm");
			edqmDoseForm = edqm.isNull() ? null : new Neo4jExportEdqm(edqm);
			amount = toBigDecimal(value.get("amount", (String) null));
			Value vUnit = value.get("unit");
			unit = vUnit.isNull() ? null : new Neo4jExportUnit(vUnit);
		}
	}

	private static class Neo4jExportIngredient {
		final long substanceMmiId;
		final String substanceName;
		final BigDecimal massFrom;
		final BigDecimal massTo;
		final boolean isActive;
		final Neo4jExportUnit unit;

		private Neo4jExportIngredient(MapAccessorWithDefaultValue value) {
			substanceMmiId = value.get("substanceMmiId").asLong();
			substanceName = value.get("substanceName").asString();
			isActive = value.get("isActive").asBoolean();
			massFrom = toBigDecimal(value.get("massFrom", (String) null));
			massTo = toBigDecimal(value.get("massTo", (String) null));
			Value vUnit = value.get("unit");
			unit = vUnit.isNull() ? null : new Neo4jExportUnit(vUnit);
		}

		public Ratio getStrength() {
			return ratioFromMassAndUnit(massFrom, massTo, unit);
		}
	}

	private static class Neo4jExportUnit {
		final String mmiCode;
		final String mmiName;
		final String ucumCs;
		final String ucumCi;
		final String print;

		private Neo4jExportUnit(MapAccessorWithDefaultValue value) {
			mmiCode = value.get("mmiCode").asString();
			mmiName = value.get("mmiName", (String) null);
			ucumCs = value.get("ucumCs", (String) null);
			ucumCi = value.get("ucumCi", (String) null);
			print = value.get("unitPrint", mmiName);
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

		public Coding toCoding() {
			return CodingProvider.createCoding(
					code,
					codingSystemUri,
					codingSystemVersion,
					codingSystemDate
			);
		}
	}

	private static class Neo4jExportEdqm extends Neo4jExportCode {

		final String name;

		protected Neo4jExportEdqm(MapAccessorWithDefaultValue value) {
			super(value);
			this.name = value.get("name", (String) null);
		}
	}

	private static class Neo4jExportAtc extends Neo4jExportCode {
		final String description;

		protected Neo4jExportAtc(MapAccessorWithDefaultValue value) {
			super(value);
			this.description = value.get("description", (String) null);
		}

		@Override
		public Coding toCoding() {
			Coding coding = super.toCoding();
			coding.display = description;
			return coding;
		}
	}

	/**
	 * Returns a Cypher statement which groups codes into a single value object where the code node is referred to by
	 * the given codeVariableName and the assigned codingSystem node is referred to by the given
	 * codingSystemVariableName. The collection happens in a way that the {@link Neo4jExportCode} can read the resulting
	 * value.
	 */
	public static String groupCodingSystem(String codeVariableName, String codingSystemVariableName) {
		return groupCodingSystem(codeVariableName, codingSystemVariableName, null);
	}

	/**
	 * Returns a Cypher statement which groups codes into a single value object where the code node is referred to by
	 * the given codeVariableName and the assigned codingSystem node is referred to by the given
	 * codingSystemVariableName. The collection happens in a way that the {@link Neo4jExportCode} can read the resulting
	 * value. Additionally, you can add more properties to the resulting object using the given "extra" parameter.
	 */
	public static String groupCodingSystem(String codeVariableName, String codingSystemVariableName, String extra) {
		return "CASE WHEN NOT " + codeVariableName + " IS NULL THEN {" + (extra != null ? extra + "," : "") +
				CODE + ":" + codeVariableName + ".code," +
				SYSTEM_URI + ":" + codingSystemVariableName + ".uri," +
				SYSTEM_DATE + ":" + codingSystemVariableName + ".date," +
				SYSTEM_VERSION + ":" + codingSystemVariableName + ".version" +
				"} ELSE NULL END";
	}


	private static Ratio ratioFromMassAndUnit(BigDecimal massFrom, BigDecimal massTo, Neo4jExportUnit unit) {
		Quantity quantity = quantityFromMassFromToAndUnit(massFrom, massTo, unit);
		if (quantity == null) return null;
		return new Ratio(quantity, Quantity.one());
	}

	private static Quantity quantityFromMassFromToAndUnit(BigDecimal massFrom, BigDecimal massTo,
	                                                      Neo4jExportUnit unit) {
		Quantity quantity;
		if (massFrom == null) {
			if (massTo == null) {
				return null;
			} else {
				quantity = quantityFromMassAndUnit(massTo, unit);
				quantity.setComparator(Quantity.Comparator.EXACT);
			}
		} else {
			quantity = quantityFromMassAndUnit(massFrom, unit);
			if (massTo == null) {
				quantity.setComparator(Quantity.Comparator.EXACT);
			} else {
				quantity.setComparator(Quantity.Comparator.GREATER_OR_EQUAL);
			}
		}
		return quantity;
	}

	private static Quantity quantityFromMassAndUnit(BigDecimal mass, Neo4jExportUnit unit) {
		if (mass == null && unit == null) return null;

		Quantity quantity = new Quantity();

		if (mass != null) {
			quantity.value = mass;
			quantity.setComparator(Quantity.Comparator.EXACT);
		}

		if (unit != null) {
			quantity.unit = unit.print;

			if (unit.ucumCs != null) {
				quantity.code = unit.ucumCs;
				quantity.system = UCUM_SYSTEM;
			}
		}
		return quantity;
	}

	private static BigDecimal toBigDecimal(String germanValue) {
		if (germanValue == null) return null;
		return new BigDecimal(germanValue.replace(',', '.'));
	}

	private static class Statistics {

		private final AtomicInteger simpleMedicationsTotal = new AtomicInteger();
		private final AtomicInteger compositeMedicationsTotal = new AtomicInteger();
		private final AtomicInteger compositeMedicationChildrenTotal = new AtomicInteger();
		private final AtomicInteger compositeChildObjectsWithAtc = new AtomicInteger();
		private final AtomicInteger atcOccurrencesInCompositeChildren = new AtomicInteger();
		private final AtomicInteger simpleObjectsWithAtc = new AtomicInteger();
		private final AtomicInteger atcOccurrencesInSimpleObjects = new AtomicInteger();
		private final AtomicInteger objectsWithPzn = new AtomicInteger();
		private final AtomicInteger pznOccurrences = new AtomicInteger();

		private final AtomicInteger simpleObjectsWithDoseForm = new AtomicInteger();
		private final AtomicInteger simpleObjectsWithEdqmDoseForm = new AtomicInteger();

		private final AtomicInteger compositeChildrenWithDoseForm = new AtomicInteger();
		private final AtomicInteger compositeChildrenWithEdqmDoseForm = new AtomicInteger();


		private final AtomicInteger ucumCompliantObjects = new AtomicInteger();
		private final AtomicInteger nonUcumCompliantObjects = new AtomicInteger();

		private final AtomicInteger ucumCompliantRatios = new AtomicInteger();
		private final AtomicInteger nonUcumCompliantRatios = new AtomicInteger();

		public void add(Medication medication) {
			if (medication == null) return;
			Type type = inferObjectType(medication);

			switch (type) {
				case COMPOSITION_PARENT -> {
					compositeMedicationsTotal.incrementAndGet();
				}
				case COMPOSITION_CHILD -> {
					compositeMedicationChildrenTotal.incrementAndGet();
					addDoseForm(medication, compositeChildrenWithDoseForm, compositeChildrenWithEdqmDoseForm);
					addCodes(medication, CodingSystem.ATC.uri, compositeChildObjectsWithAtc,
							atcOccurrencesInCompositeChildren);
				}
				case SIMPLE -> {
					simpleMedicationsTotal.incrementAndGet();
					addDoseForm(medication, simpleObjectsWithDoseForm, simpleObjectsWithEdqmDoseForm);
					addCodes(medication, CodingSystem.ATC.uri, simpleObjectsWithAtc,
							atcOccurrencesInSimpleObjects);
				}
			}


			if (type == Type.SIMPLE || type == Type.COMPOSITION_PARENT) {
				addCodes(medication, CodingSystem.PZN.uri, objectsWithPzn, pznOccurrences);
			}

			boolean isUcumCompliant = areIngredientsUcumCompliant(medication);

			if (type == Type.SIMPLE || type == Type.COMPOSITION_CHILD) {
				isUcumCompliant = isUcumCompliant && isUcumCompliant(medication.amount);
			}

			if (isUcumCompliant)
				ucumCompliantObjects.incrementAndGet();
			else
				nonUcumCompliantObjects.incrementAndGet();
		}

		private boolean areIngredientsUcumCompliant(Medication medication) {
			boolean result = true;
			if (medication.ingredient != null) {
				for (Ingredient i: medication.ingredient) {
					result  = isUcumCompliant(i.strength) && result;
				}
			}
			return true;
		}

		private boolean isUcumCompliant(Ratio ratio) {
			if (ratio == null) return true;
			boolean result = UCUM_SYSTEM.equals(ratio.numerator.system) && UCUM_SYSTEM.equals(ratio.denominator.system);
			if (result) {
				ucumCompliantRatios.incrementAndGet();
			} else {
				nonUcumCompliantRatios.incrementAndGet();
			}
			return result;
		}


		private Type inferObjectType(Medication medication) {
			if (MedicationReference.TYPE.value.equals(medication.ingredient[0].itemReference.type)) {
				return Type.COMPOSITION_PARENT;
			} else if (medication.code.text != null) {
				return Type.SIMPLE;
			} else {
				return Type.COMPOSITION_CHILD;
			}
		}

		private void addCodes(Medication medication, String codeSystem, AtomicInteger objects,
		                      AtomicInteger occurrences) {
			boolean anyMatch = false;
			for (Coding c : medication.code.coding) {
				if (codeSystem.equals(c.system)) {
					occurrences.getAndIncrement();
					if (!anyMatch) {
						objects.getAndIncrement();
						anyMatch = true;
					}
				}
			}
		}

		private void addDoseForm(Medication medication, AtomicInteger doseFormOnly, AtomicInteger edqmDoseForm) {
			if (medication.form == null) return;
			if (medication.form.text == null && medication.form.coding == null) return;
			doseFormOnly.incrementAndGet();
			if (medication.form.coding == null) {
				return;
			}
			if (medication.form.coding.length > 1) {
				throw new IllegalStateException(
						"Medication " + medication.identifier[0].value + " contains multiple dose forms!");
			}
			if (CodingSystem.EDQM.uri.equals(medication.form.coding[0].system)) {
				edqmDoseForm.incrementAndGet();
			}
		}

		private enum Type {
			COMPOSITION_PARENT,
			COMPOSITION_CHILD,
			SIMPLE,
		}

		@Override
		public String toString() {
			return "Statistics{" +
					"simpleMedicationsTotal=" + simpleMedicationsTotal +
					", compositeMedicationsTotal=" + compositeMedicationsTotal +
					", compositeMedicationChildrenTotal=" + compositeMedicationChildrenTotal +
					", compositeChildObjectsWithAtc=" + compositeChildObjectsWithAtc +
					", atcOccurrencesInCompositeChildren=" + atcOccurrencesInCompositeChildren +
					", simpleObjectsWithAtc=" + simpleObjectsWithAtc +
					", atcOccurrencesInSimpleObjects=" + atcOccurrencesInSimpleObjects +
					", objectsWithPzn=" + objectsWithPzn +
					", pznOccurrences=" + pznOccurrences +
					", simpleObjectsWithDoseForm=" + simpleObjectsWithDoseForm +
					", simpleObjectsWithEdqmDoseForm=" + simpleObjectsWithEdqmDoseForm +
					", compositeChildrenWithDoseForm=" + compositeChildrenWithDoseForm +
					", compositeChildrenWithEdqmDoseForm=" + compositeChildrenWithEdqmDoseForm +
					", ucumCompliantObjects=" + ucumCompliantObjects +
					", nonUcumCompliantObjects=" + nonUcumCompliantObjects +
					", ucumCompliantRatios=" + ucumCompliantRatios +
					", nonUcumCompliantRatios=" + nonUcumCompliantRatios +
					'}';
		}
	}

}
