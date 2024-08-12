package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphCode;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphSubstance;
import de.medizininformatikinitiative.medgraph.searchengine.model.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.*;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.common.EDQM.*;

/**
 * Provides and/or generates lots of sample entities. The entities provided here are also reflected in the Neo4j test
 * database, whose definition can be found in {@link Neo4jDatabaseTestExtension}.
 *
 * @author Markus Budeus
 */
public class TestFactory {

	/**
	 * Declares all substances found in the default test database.
	 */
	public static class Substances implements Catalogue {
		public static final Substance ACETYLSALICYLIC_ACID = new Substance(1, "Acetylsalicylsäure");
		public static final Substance MIDAZOLAM = new Substance(2, "Midazolam");
		public static final Substance MIDAZOLAM_HYDROCHLORIDE = new Substance(3, "Midazolam hydrochlorid");
		public static final Substance EPINEPHRINE = new Substance(4, "Epinephrin");
		public static final Substance WATER = new Substance(5, "Wasser für Injektionszwecke");
		public static final Substance PREDNISOLONE = new Substance(6, "Prednisolon");
		public static final Substance PREDNISOLONE_HYDROGENSUCCINATE = new Substance(7,
				"Prednisolon 21-hydrogensuccinat, Natriumsalz");
		public static final Substance PARACETAMOL = new Substance(8, "Paracetamol");
	}

	/**
	 * Declares all products found in the default test database.
	 */
	public static class Products implements Catalogue {

		/**
		 * Declares all of the products as {@link DetailedProduct}-instances.
		 */
		public static class Detailed implements Catalogue {

			/**
			 * Aspirin oral granlues, containing 500mg of acetylsalicylic acid.
			 */
			public static final DetailedProduct ASPIRIN = new DetailedProduct(1,
					"Aspirin Complex Granulat-Sticks 500 mg/30 mg Granulat",
					List.of("10000001"),
					List.of(new Drug("Gran. zum Einnehmen", DoseForms.GRANULES, new Amount(BigDecimal.ONE, null),
							List.of(new ActiveIngredient(Substances.ACETYLSALICYLIC_ACID.getName(),
									new Amount(new BigDecimal(500), "mg"))))));

			/**
			 * Dormicum, 3ml of fluid in an ampoule, containing 16.68mg of midazolam hydrochloride, respectively 15mg of
			 * midazolam.
			 */
			public static final DetailedProduct DORMICUM_15 = new DetailedProduct(2,
					"Dormicum® 15 mg/3 ml Injektionslösung",
					List.of("10000002"),
					List.of(new Drug("Injektions-/Infusionslsg.", DoseForms.SOLUTION_FOR_INJECTION_OR_INFUSION,
							new Amount(new BigDecimal(3), "ml"),
							List.of(new CorrespondingActiveIngredient(Substances.MIDAZOLAM_HYDROCHLORIDE.getName(),
									new Amount(new BigDecimal("16.68"), "mg"),
									Substances.MIDAZOLAM.getName(),
									new Amount(new BigDecimal(15), "mg"))))));

			/**
			 * Dormicum, 5ml of fluid in an ampoule, containing 5.5-5.7mg of midazolam hydrochloride, respectively 5mg
			 * of midazolam.
			 */
			public static final DetailedProduct DORMICUM_5 = new DetailedProduct(3,
					"Dormicum® V 5 mg/5 ml Injektionslösung",
					List.of("10000003"),
					List.of(new Drug("Injektions-/Infusionslsg.", DoseForms.SOLUTION_FOR_INJECTION_OR_INFUSION,
							new Amount(new BigDecimal(5), "ml"),
							List.of(new CorrespondingActiveIngredient(Substances.MIDAZOLAM_HYDROCHLORIDE.getName(),
									new AmountRange(new BigDecimal("5.5"), new BigDecimal("5.7"), "mg"),
									Substances.MIDAZOLAM.getName(),
									new Amount(new BigDecimal(5), "mg"))))));


			/**
			 * An EpiPen containing 300ug of Epinephrine.
			 */
			public static final DetailedProduct ANAPEN = new DetailedProduct(4,
					"Anapen 300 µg kohlpharma Injektionslösung",
					List.of("10000004", "10000005"),
					List.of(new Drug("Injektionslsg.", DoseForms.SOLUTION_FOR_INJECTION,
							new Amount(new BigDecimal("0.3"), "ml"),
							List.of(new ActiveIngredient(Substances.EPINEPHRINE.getName(),
									new Amount(new BigDecimal("300"), "ug"))))));

			/**
			 * Aseptoderm, which is a disinfectant. Has no assigned drugs.
			 */
			public static final DetailedProduct ASEPTODERM = new DetailedProduct(5,
					"Aseptoderm",
					List.of("10000006"),
					List.of());

			/**
			 * Prednisolut, powder and solvent in which the powder is to be solved before being injected.
			 * <p>
			 * The powder and solvent are two separate drugs. The latter only specifies 2ml of a solvent with no active
			 * ingredient. The former contains 10.48mg of prednisolone hydrogensuccinate, respectively 7.83mg of
			 * prednisolone.
			 */
			public static final DetailedProduct PREDNISOLUT = new DetailedProduct(6,
					"Prednisolut® 10 mg L, Pulver und Lösungsmittel zur Herstellung einer Injektionslösung",
					List.of("01343446"),
					List.of(
							new Drug("Pulver zur Herst. e. Inj.-Lsg.", DoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION,
									new Amount(BigDecimal.ONE, null),
									List.of(new CorrespondingActiveIngredient(
											Substances.PREDNISOLONE_HYDROGENSUCCINATE.getName(),
											new Amount(new BigDecimal("10.48"), "mg"),
											Substances.PREDNISOLONE.getName(),
											new Amount(new BigDecimal("7.83"), "mg")
									))),
							new Drug("Lösungsmittel", null,
									new Amount(new BigDecimal(2), "ml"),
									List.of())
					)
			);

			/**
			 * Aspirin/Paracetamol tablets. Contains different tablets for day and night, meaning it has two assigned
			 * drugs which have very similar ingredients. (In this test scenario, the ingredients are actually the
			 * same.)
			 */
			public static final DetailedProduct DOLOMO = new DetailedProduct(7,
					"dolomo® TN, Tablette",
					List.of("00778219"),
					List.of(
							new Drug(
									"Tbl.",
									DoseForms.TABLET,
									new Amount(BigDecimal.ONE, null),
									List.of(new ActiveIngredient(
													Substances.ACETYLSALICYLIC_ACID.getName(),
													new Amount(new BigDecimal(250), "mg")
											),
											new ActiveIngredient(
													Substances.PARACETAMOL.getName(),
													new Amount(new BigDecimal(250), "mg")
											))
							),
							new Drug(
									"Tbl.",
									DoseForms.TABLET,
									new Amount(BigDecimal.ONE, null),
									List.of(new ActiveIngredient(
													Substances.ACETYLSALICYLIC_ACID.getName(),
													new Amount(new BigDecimal(250), "mg")
											),
											new ActiveIngredient(
													Substances.PARACETAMOL.getName(),
													new Amount(new BigDecimal(250), "mg")
											))
							)
					)
			);
		}

		public static final Product ASPIRIN = ofDetailed(Detailed.ASPIRIN);
		public static final Product DORMICUM_15 = ofDetailed(Detailed.DORMICUM_15);
		public static final Product DORMICUM_5 = ofDetailed(Detailed.DORMICUM_5);
		public static final Product ANAPEN = ofDetailed(Detailed.ANAPEN);
		public static final Product ASEPTODERM = ofDetailed(Detailed.ASEPTODERM);
		public static final Product PREDNISOLUT = ofDetailed(Detailed.PREDNISOLUT);
		public static final Product DOLOMO = ofDetailed(Detailed.DOLOMO);

		private static Product ofDetailed(DetailedProduct product) {
			return new Product(product.getId(), product.getName());
		}
	}

	/**
	 * Declares all EDQM Standard Terms concepts found in the default test database. Note that direct members of this
	 * class are only the pharmaceutical dose forms.
	 */
	public static class DoseForms implements Catalogue {
		/**
		 * Declares the EDQM Standard Terms characteristics found in the default test database.
		 */
		public static class Characteristics implements Catalogue {
			/**
			 * Powder as a basic dose form.
			 */
			public static final EdqmConcept POWDER = new EdqmConcept("0066", "Powder", BASIC_DOSE_FORM);
			/**
			 * A solution, which is a basic dose form.
			 */
			public static final EdqmConcept SOLUTION = new EdqmConcept("0083", "Solution", BASIC_DOSE_FORM);
			/**
			 * Granules, which are a basic dose form.
			 */
			public static final EdqmConcept GRANULES = new EdqmConcept("0053", "Granules", BASIC_DOSE_FORM);
			/**
			 * Parenteral application, which is an intended site.
			 */
			public static final EdqmConcept PARENTERAL = new EdqmConcept("0033", "Parenteral", INTENDED_SITE);
			/**
			 * Oral application, which is an intended site.
			 */
			public static final EdqmConcept ORAL = new EdqmConcept("0031", "Oral", INTENDED_SITE);
			/**
			 * Conventional, which is the default release characteristic.
			 */
			public static final EdqmConcept CONVENTIONAL = new EdqmConcept("0047", "Conventional",
					RELEASE_CHARACTERISTIC);
			public static final EdqmConcept TABLET = new EdqmConcept("0069", "Tablet", BASIC_DOSE_FORM);
		}

		/**
		 * Oral granules meant to be swallowed.
		 */
		public static final EdqmPharmaceuticalDoseForm GRANULES = new EdqmPharmaceuticalDoseForm("10204000", "Granules",
				Set.of(Characteristics.GRANULES, Characteristics.CONVENTIONAL, Characteristics.ORAL));

		/**
		 * A solution which can be injected or infused into a patient.
		 */
		public static final EdqmPharmaceuticalDoseForm SOLUTION_FOR_INJECTION_OR_INFUSION =
				new EdqmPharmaceuticalDoseForm("50060000", "Solution for injection/infusion",
						Set.of(Characteristics.SOLUTION, Characteristics.CONVENTIONAL, Characteristics.PARENTERAL));

		/**
		 * A solution which can be injected into a patient.
		 */
		public static final EdqmPharmaceuticalDoseForm SOLUTION_FOR_INJECTION =
				new EdqmPharmaceuticalDoseForm("11201000", "Solution for injection",
						Set.of(Characteristics.SOLUTION, Characteristics.CONVENTIONAL, Characteristics.PARENTERAL));

		/**
		 * A powder meant to be dissolved to form a solution to be injected into a patient.
		 */
		public static final EdqmPharmaceuticalDoseForm POWDER_FOR_SOLUTION_FOR_INJECTION =
				new EdqmPharmaceuticalDoseForm("11205000", "Powder for solution for injection",
						Set.of(Characteristics.POWDER, Characteristics.CONVENTIONAL, Characteristics.PARENTERAL));

		public static final EdqmPharmaceuticalDoseForm TABLET =
				new EdqmPharmaceuticalDoseForm("10219000", "Tablet",
						Set.of(Characteristics.TABLET, Characteristics.CONVENTIONAL, Characteristics.ORAL));
	}


	public static final Substance SAMPLE_SUBSTANCE_1 = Substances.ACETYLSALICYLIC_ACID;
	public static final Substance SAMPLE_SUBSTANCE_2 = Substances.MIDAZOLAM;
	public static final Substance SAMPLE_SUBSTANCE_3 = Substances.MIDAZOLAM_HYDROCHLORIDE;
	public static final Product SAMPLE_PRODUCT_1 = Products.ASPIRIN;
	public static final Product SAMPLE_PRODUCT_2 = Products.DORMICUM_15;
	public static final Product SAMPLE_PRODUCT_3 = Products.DORMICUM_5;

	/**
	 * Provider which provides all products given in this test factory.
	 */
	public static final BaseProvider<String, Product> PRODUCTS_PROVIDER = BaseProvider.ofIdentifiableNames(
			Catalogue.getAllFields(Products.class, false)
	);

	/**
	 * Provider which provides all substances given in this test factory.
	 */
	public static final BaseProvider<String, Substance> SUBSTANCES_PROVIDER = BaseProvider.ofIdentifiableNames(
			Catalogue.getAllFields(Substances.class, false)
	);

	/**
	 * Provider which provides all EDQM dose forms and characteristics given in this test factory.
	 */
	public static final BaseProvider<String, EdqmConcept> EDQM_PROVIDER = BaseProvider.ofIdentifiableNames(
			Catalogue.getAllFields(DoseForms.class, true)
	);

	/**
	 * Provider which provides all substances and products given in this test factory.
	 */
	public static final BaseProvider<String, Matchable> PRODUCTS_AND_SUBSTANCES_PROVIDER =
			join(PRODUCTS_PROVIDER, SUBSTANCES_PROVIDER);

	public static final SearchQuery SAMPLE_SEARCH_QUERY = new SearchQuery.Builder()
			.withProductNameKeywords(List.of("Aspirin"))
			.withSubstances(List.of(SAMPLE_SUBSTANCE_1))
			.withActiveIngredientDosages(List.of(Dosage.of(10, "mg")))
			.withDrugAmounts(List.of(new Amount(BigDecimal.ONE, "ml")))
			.build();


	public static final Dosage SAMPLE_DOSAGE_1 = Dosage.of(10, "mg", 1, "ml");
	public static final Dosage SAMPLE_DOSAGE_2 = Dosage.of(250, "mg");

	public static final Amount SAMPLE_AMOUNT_1 = new Amount(new BigDecimal(5), "ml");
	public static final Amount SAMPLE_AMOUNT_2 = new Amount(new BigDecimal("7.5"), "g");
	public static final Amount SAMPLE_AMOUNT_3 = new Amount(BigDecimal.ONE, null);
	public static final Amount SAMPLE_AMOUNT_4 = new Amount(BigDecimal.ONE, "mg");
	public static final Amount SAMPLE_AMOUNT_5 = new Amount(new BigDecimal(500), "ug");
	public static final AmountRange SAMPLE_AMOUNT_RANGE = new AmountRange(new BigDecimal("1.4"), new BigDecimal("1.6"),
			"mg");

	@SafeVarargs
	private static <S, T extends Identifiable> BaseProvider<S, T> join(BaseProvider<S, ? extends T>... providers) {
		Stream<MappedIdentifier<S, ? extends T>> stream = Stream.empty();
		for (BaseProvider<S, ? extends T> provider : providers) {
			stream = Stream.concat(stream, provider.getIdentifiers());
		}
		return BaseProvider.ofIdentifiers(
				stream.map(m -> new MappedIdentifier<S, T>(m.trackableIdentifier, m.target)).toList());
	}

	/**
	 * Builds a {@link DetailedProduct} with default values set for all unspecified fields.
	 *
	 * @param id    the id of the product
	 * @param drugs the drugs the product consists of
	 * @return a corresponding {@link DetailedProduct}
	 */
	public static DetailedProduct buildDetailedProduct(long id, List<Drug> drugs) {
		return new DetailedProduct(id, "Sample", List.of(), drugs);
	}

	/**
	 * Builds a drug, with default values used for the dose form.
	 *
	 * @param drugAmount  the drug amount
	 * @param ingredients the drug's ingredients
	 * @return a corresponding {@link Drug}
	 */
	public static Drug buildDrug(Amount drugAmount, List<ActiveIngredient> ingredients) {
		return new Drug("Unknown", null, drugAmount, ingredients);
	}

}
