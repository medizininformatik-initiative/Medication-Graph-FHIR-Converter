package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.OrganizationReference;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.MedicationReference;
import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Medication;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Markus Budeus
 */
public record GraphProduct(String name, long mmiId, Long companyMmiId, String companyName, List<GraphCode> codes,
                           List<GraphDrug> drugs, List<GraphPackage> packages) {

	public static final String PRODUCT_NAME = "productName";
	public static final String MMI_ID = "mmiId";
	public static final String COMPANY_MMI_ID = "companyMmiId";
	public static final String COMPANY_NAME = "companyName";
	public static final String PRODUCT_CODES = "productCodes";
	public static final String DRUGS = "drugs";
	public static final String PACKAGES = "packages";

	public GraphProduct(MapAccessorWithDefaultValue value) {
		this(
				value.get(PRODUCT_NAME, (String) null),
				value.get(MMI_ID).asLong(),
				(Long) value.get(COMPANY_MMI_ID, (Long) null),
				value.get(COMPANY_NAME, (String) null),
				value.get(PRODUCT_CODES).asList(GraphCode::new),
				value.get(DRUGS).asList(GraphDrug::new),
				value.get(PACKAGES).asList(GraphPackage::new)
		);
	}

	@Deprecated
	public List<de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication> toLegacyFhirMedications() {
		de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication primary;
		if (drugs.size() == 1) {
			primary = drugs.getFirst().toLegacyMedication();
		} else {
			primary = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication();
		}

		legacyApplyManufacturer(primary);

		if (primary.code == null) {
			primary.code = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.CodeableConcept();
		}
		legacyApplyProductAndPackageCodes(primary);
		primary.identifier = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier[] { de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier.fromProductAndOrganizationMmiId(mmiId, companyMmiId) };
		primary.code.text = name;

		if (drugs.size() > 1) {
			return createLegacyDrugMedicationsAndApplyAsIngredients(primary);
		}

		return List.of(primary);
	}

	/**
	 * Creates {@link de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication} objects from all drugs and returns a list of all those objects, including the passed
	 * primary object, which is always the first list entry. Furthermore, the ingredients of the passed object are set
	 * as referenced to the drug medication objects. Also, the drug medication objects have their manufacturer
	 * set accordingly.
	 */
	@Deprecated
	private List<de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication> createLegacyDrugMedicationsAndApplyAsIngredients(de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication to) {
		List<de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication> drugMedications = drugs.stream().map(GraphDrug::toLegacyMedication).toList();

		to.ingredient = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient[drugMedications.size()];
		for (int i = 0; i < drugMedications.size(); i++) {
			int childNo = i + 1;
			de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication drugMedication = drugMedications.get(i);
			legacyApplyManufacturer(drugMedication);
			drugMedication.code.text = null;
			drugMedication.identifier = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier[] { de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier.combinedMedicalProductSubproductIdentifier(mmiId, childNo, companyMmiId)};
			to.ingredient[i] = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient();
			to.ingredient[i].itemReference = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.LegacyMedicationReference(mmiId, childNo, companyMmiId);
		}

		List<de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication> outList = new ArrayList<>(drugMedications.size() + 1);
		outList.add(to);
		outList.addAll(drugMedications);
		return outList;
	}

	@Deprecated
	private void legacyApplyProductAndPackageCodes(de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication to) {
		Set<de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding> primaryCodings = new LinkedHashSet<>();
		if (codes != null) {
			primaryCodings.addAll(codes.stream().map(GraphCode::toLegacyCoding).toList());
		}
		if (packages != null) {
			for (GraphPackage graphPackage: packages) {
				if (graphPackage.codes() != null) {
					primaryCodings.addAll(graphPackage.codes().stream().map(GraphCode::toLegacyCoding).toList());
				}
			}
		}

		de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding[] out = primaryCodings.toArray(new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding[0]);
		if (to.code.coding == null) {
			to.code.coding = out;
		} else {
			to.code.coding = concat(out, to.code.coding);
		}
	}

	@Deprecated
	private void legacyApplyManufacturer(de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication to) {
		if (companyMmiId != null) {
			to.manufacturer = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.LegacyOrganizationReference(companyMmiId, companyName);
		}
	}

	public List<Medication> toFhirMedications() {
		Medication primary;
		if (drugs.size() == 1) {
			primary = drugs.getFirst().toFhirMedication();
		} else {
			primary = new Medication();
		}

		applyManufacturer(primary);
		applyProductAndPackageCodes(primary);
		primary.setId(IdProvider.fromProductMmiId(mmiId));
		primary.getCode().setText(name);

		if (drugs.size() > 1) {
			return createDrugMedicationsAndApplyAsIngredients(primary);
		}

		return List.of(primary);
	}

	/**
	 * Creates {@link Medication} objects from all drugs and returns a list of all those objects, including the passed
	 * primary object, which is always the first list entry. Furthermore, the ingredients of the passed object are set
	 * as referenced to the drug medication objects. Also, the drug medication objects have their manufacturer
	 * set accordingly.
	 */
	private List<Medication> createDrugMedicationsAndApplyAsIngredients(Medication to) {
		List<Medication> drugMedications = drugs.stream().map(GraphDrug::toFhirMedication).toList();

		for (int i = 0; i < drugMedications.size(); i++) {
			int childNo = i + 1;
			Medication drugMedication = drugMedications.get(i);
			applyManufacturer(drugMedication);
			drugMedication.getCode().setText(null);
			drugMedication.setId(IdProvider.combinedMedicalProductSubproductIdentifier(mmiId, childNo));
			to.addIngredient().setItem(new MedicationReference(mmiId, childNo, null));
		}

		List<Medication> outList = new ArrayList<>(drugMedications.size() + 1);
		outList.add(to);
		outList.addAll(drugMedications);
		return outList;
	}

	private void applyProductAndPackageCodes(Medication to) {
		Set<GraphCode> graphCodes = new LinkedHashSet<>();
		if (codes != null) {
			graphCodes.addAll(codes);
		}
		if (packages != null) {
			for (GraphPackage graphPackage: packages) {
				List<GraphCode> pkCodes = graphPackage.codes();
				if (pkCodes != null) {
					graphCodes.addAll(pkCodes);
				}
			}
		}

		CodeableConcept toConcept = to.getCode();
		graphCodes.stream().filter(Objects::nonNull).map(GraphCode::toCoding).forEach(toConcept::addCoding);
	}

	private void applyManufacturer(Medication to) {
		if (companyMmiId != null) {
			to.setManufacturer(new OrganizationReference(companyMmiId, companyName));
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T[] concat(T[] array1, T[] array2) {
		T[] out = (T[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
		System.arraycopy(array1, 0, out, 0, array1.length);
		System.arraycopy(array2, 0, out, array1.length, array2.length);
		return out;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		GraphProduct product = (GraphProduct) object;
		return mmiId == product.mmiId && Objects.equals(name, product.name) && Objects.equals(
				companyMmiId, product.companyMmiId) && Objects.equals(companyName,
				product.companyName) && Util.equalsIgnoreOrder(codes, product.codes) && Util.equalsIgnoreOrder(drugs,
				product.drugs) && Util.equalsIgnoreOrder(packages, product.packages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, mmiId, companyMmiId, companyName, codes, drugs, packages);
	}
}
