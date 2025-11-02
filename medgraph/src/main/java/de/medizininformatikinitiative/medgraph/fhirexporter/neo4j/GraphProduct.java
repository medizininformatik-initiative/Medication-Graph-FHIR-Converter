package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.MedicationReference;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.OrganizationReference;
import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;
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

    public List<Medication> toFhirMedications() {
        System.out.println("\n*** [Product] Converting product: " + name + " (mmiId: " + mmiId + ") with " + drugs.size() + " drug(s) ***");
        
		Medication primary;
		if (drugs.size() == 1) {
			primary = drugs.get(0).toMedication();
            // Try RxNorm match for the single drug
            RxNormProductMatcher matcher = RxNormMatcherSetup.getSharedMatcher();
            if (matcher != null) {
                RxNormProductMatcher.MatchResult scd = matcher.matchSCD(drugs.get(0));
                if (scd != null) {
                    addRxNormCoding(primary, scd);
                }
            }
		} else {
			primary = new Medication();
		}

		applyManufacturer(primary);

		if (primary.code == null) {
			primary.code = new CodeableConcept();
		}
		applyProductAndPackageCodes(primary);
		primary.identifier = new Identifier[] { Identifier.fromProductAndOrganizationMmiId(mmiId, companyMmiId) };
		primary.code.text = name;

		if (drugs.size() > 1) {
            List<Medication> meds = createDrugMedicationsAndApplyAsIngredients(primary);
            // Try RxNorm match for each drug medication
            RxNormProductMatcher matcher = RxNormMatcherSetup.getSharedMatcher();
            if (matcher != null) {
                for (int i = 0; i < drugs.size(); i++) {
                    RxNormProductMatcher.MatchResult scd = matcher.matchSCD(drugs.get(i));
                    if (scd != null) {
                        addRxNormCoding(meds.get(i + 1), scd); // first is primary
                    }
                }
            }
            return meds;
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
		List<Medication> drugMedications = drugs.stream().map(GraphDrug::toMedication).toList();

		to.ingredient = new Ingredient[drugMedications.size()];
		for (int i = 0; i < drugMedications.size(); i++) {
			int childNo = i + 1;
			Medication drugMedication = drugMedications.get(i);
			applyManufacturer(drugMedication);
			drugMedication.code.text = null;
			drugMedication.identifier = new Identifier[] { Identifier.combinedMedicalProductSubproductIdentifier(mmiId, childNo, companyMmiId)};
			to.ingredient[i] = new Ingredient();
			to.ingredient[i].itemReference = new MedicationReference(mmiId, childNo, companyMmiId);
		}

		List<Medication> outList = new ArrayList<>(drugMedications.size() + 1);
		outList.add(to);
		outList.addAll(drugMedications);
		return outList;
	}

	private void applyProductAndPackageCodes(Medication to) {
		Set<Coding> primaryCodings = new LinkedHashSet<>();
		if (codes != null) {
			primaryCodings.addAll(codes.stream().map(GraphCode::toCoding).toList());
		}
		if (packages != null) {
			for (GraphPackage graphPackage: packages) {
				if (graphPackage.codes() != null) {
					primaryCodings.addAll(graphPackage.codes().stream().map(GraphCode::toCoding).toList());
				}
			}
		}

		Coding[] out = primaryCodings.toArray(new Coding[0]);
		if (to.code.coding == null) {
			to.code.coding = out;
		} else {
			to.code.coding = concat(out, to.code.coding);
		}
	}

	private void applyManufacturer(Medication to) {
		if (companyMmiId != null) {
			to.manufacturer = new OrganizationReference(companyMmiId, companyName);
		}
	}

    private void addRxNormCoding(Medication to, RxNormProductMatcher.MatchResult match) {
        if (to.code == null) {
            to.code = new CodeableConcept();
        }
        Coding rx = new Coding();
        rx.system = "https://www.nlm.nih.gov/research/umls/rxnorm";
        rx.code = match.rxcui;
        rx.display = match.name;
        if (to.code.coding == null) {
            to.code.coding = new Coding[] { rx };
        } else {
            to.code.coding = concat(new Coding[] { rx }, to.code.coding);
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
