package de.medizininformatikinitiative.medgraph.db;

/**
 * A set of constants used for defining the database, primarily relation and node labels.
 *
 * @author Markus Budeus
 */
public class DatabaseDefinitions {

	public static final String CODING_SYSTEM_LABEL = "CodingSystem";
	public static final String BELONGS_TO_CODING_SYSTEM_LABEL = "BELONGS_TO";
	public static final String CODE_LABEL = "Code";
	public static final String CODE_REFERENCE_RELATIONSHIP_NAME = "REFERENCES";
	public static final String SUBSTANCE_LABEL = "Substance";
	public static final String PRODUCT_LABEL = "Product";
	public static final String INGREDIENT_LABEL = "Ingredient";
	public static final String MMI_INGREDIENT_LABEL = "MmiIngredient";
	public static final String DRUG_LABEL = "Drug";
	public static final String ASK_LABEL = "ASK";
	public static final String CAS_LABEL = "CAS";
	public static final String INN_LABEL = "INN";
	public static final String RXCUI_LABEL = "RXCUI";
	public static final String UNII_LABEL = "UNII";
	public static final String ATC_LABEL = "ATC";
	public static final String ATC_HAS_PARENT_LABEL = "HAS_PARENT";
	public static final String PZN_LABEL = "PZN";
	public static final String PACKAGE_LABEL = "Package";
	public static final String PACKAGE_HAS_SUCCESSOR_LABEL = "HAS_SUCCESSOR";
	public static final String PACKAGE_IS_ORIGINALLY = "IS_ORIGINALLY";
	public static final String PACKAGE_BELONGS_TO_PRODUCT_LABEL = "BELONGS_TO";
	public static final String COMPANY_LABEL = "Company";
	public static final String ADDRESS_LABEL = "Address";
	public static final String COMPANY_HAS_ADDRESS_LABEL = "HAS_ADDRESS";
	public static final String MANUFACTURES_LABEL = "MANUFACTURES";
	public static final String PRODUCT_CONTAINS_DRUG_LABEL = "CONTAINS";
	public static final String DRUG_CONTAINS_INGREDIENT_LABEL = "CONTAINS";
	public static final String DRUG_MATCHES_ATC_CODE_LABEL = "MATCHES";
	public static final String INGREDIENT_IS_SUBSTANCE_LABEL = "IS_SUBSTANCE";
	public static final String INGREDIENT_CORRESPONDS_TO_LABEL = "CORRESPONDS_TO";
	public static final String INGREDIENT_HAS_UNIT_LABEL = "HAS_UNIT";
	//Do not change this label ideally, as it would break the UnitLoader, which only considers the INGREDIENT_HAS_UNIT_LABEL
	public static final String DRUG_HAS_UNIT_LABEL = INGREDIENT_HAS_UNIT_LABEL;
	public static final String DRUG_HAS_DOSE_FORM_LABEL = "HAS_DOSE_FORM";
	public static final String UNIT_LABEL = "Unit";
	public static final String DOSE_FORM_LABEL = "DoseForm";
	public static final String UCUM_LABEL = "UCUM";
	public static final String EDQM_LABEL = "EDQM";
	public static final String DOSE_FORM_IS_EDQM = "CORRESPONDS_TO";
	public static final String SYNONYME_LABEL = "Synonyme";
	public static final String SYNONYME_REFERENCES_NODE_LABEL = "REFERENCES";

}
