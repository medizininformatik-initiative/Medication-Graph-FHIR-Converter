package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.searchengine.db.DbAmount;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

import java.math.BigDecimal;
import java.util.List;

/**
 * Provides and/or generates lots of sample entities.
 *
 * @author Markus Budeus
 */
public class TestFactory {

	public static class Substances {
		public static final Substance ACETYLSALICYLIC_ACID = new Substance(1, "Acetylsalicylsäure");
		public static final Substance MIDAZOLAM = new Substance(2, "Midazolam");
		public static final Substance MIDAZOLAM_HYDROCHLORIDE = new Substance(3, "Midazolam hydrochlorid");
		public static final Substance EPINEPHRINE = new Substance(4, "Epinephrin");
		public static final Substance WATER = new Substance(5, "Wasser für Injektionszwecke");
	}

	public static class Products {

		public static class Detailed {
			public static final DetailedProduct ASPIRIN = new DetailedProduct(1,
					"Aspirin Complex Granulat-Sticks 500 mg/30 mg Granulat",
					List.of("10000001"),
					List.of(new Drug("Gran. zum Einnehmen", "Granules", new Amount(BigDecimal.ONE, null),
							List.of(new ActiveIngredient(Substances.ACETYLSALICYLIC_ACID.getName(),
									new Amount(new BigDecimal(500), "mg"))))));

			public static final DetailedProduct DORMICUM_15 = new DetailedProduct(2,
					"Dormicum® 15 mg/3 ml Injektionslösung",
					List.of("10000002"),
					List.of(new Drug("Injektions-/Infusionslsg.", "Solution for injection/infusion",
							new Amount(new BigDecimal(3), "ml"),
							List.of(new CorrespondingActiveIngredient(Substances.MIDAZOLAM_HYDROCHLORIDE.getName(),
									new Amount(new BigDecimal("16.68"), "mg"),
									Substances.MIDAZOLAM.getName(),
									new Amount(new BigDecimal(15), "mg"))))));

			public static final DetailedProduct DORMICUM_5 = new DetailedProduct(3,
					"Dormicum® V 5 mg/5 ml Injektionslösung",
					List.of("10000003"),
					List.of(new Drug("Injektions-/Infusionslsg.", "Solution for injection/infusion",
							new Amount(new BigDecimal(5), "ml"),
							List.of(new CorrespondingActiveIngredient(Substances.MIDAZOLAM_HYDROCHLORIDE.getName(),
									new AmountRange(new BigDecimal("5.5"), new BigDecimal("5.7"), "mg"),
									Substances.MIDAZOLAM.getName(),
									new Amount(new BigDecimal( 5), "mg"))))));


			public static final DetailedProduct ANAPEN = new DetailedProduct(4,
					"Anapen 300 µg kohlpharma Injektionslösung",
					List.of("10000004", "10000005"),
					List.of(new Drug("Injektionslsg.", "Solution for injection",
							new Amount(new BigDecimal("0.3"), "ml"),
							List.of(new ActiveIngredient(Substances.EPINEPHRINE.getName(),
									new Amount(new BigDecimal("300"), "ug"))))));
		}

		public static final Product ASPIRIN = ofDetailed(Detailed.ASPIRIN);
		public static final Product DORMICUM_15 = ofDetailed(Detailed.DORMICUM_15);
		public static final Product DORMICUM_5 = ofDetailed(Detailed.DORMICUM_5);
		public static final Product ANAPEN = ofDetailed(Detailed.ANAPEN);

		private static Product ofDetailed(DetailedProduct product) {
			return new Product(product.getId(), product.getName());
		}
	}

	public static final Substance SAMPLE_SUBSTANCE_1 = Substances.ACETYLSALICYLIC_ACID;
	public static final Substance SAMPLE_SUBSTANCE_2 = Substances.MIDAZOLAM;
	public static final Substance SAMPLE_SUBSTANCE_3 = Substances.MIDAZOLAM_HYDROCHLORIDE;
	public static final Product SAMPLE_PRODUCT_1 = Products.ASPIRIN;
	public static final Product SAMPLE_PRODUCT_2 = Products.DORMICUM_15;
	public static final Product SAMPLE_PRODUCT_3 = Products.DORMICUM_5;

	public static final BaseProvider<String> PRODUCTS_AND_SUBSTANCES_PROVIDER = BaseProvider.ofIdentifiers(List.of(
			new MappedIdentifier<>(SAMPLE_SUBSTANCE_1.getName(), SAMPLE_SUBSTANCE_1),
			new MappedIdentifier<>(SAMPLE_SUBSTANCE_2.getName(), SAMPLE_SUBSTANCE_2),
			new MappedIdentifier<>(SAMPLE_SUBSTANCE_3.getName(), SAMPLE_SUBSTANCE_3),
			new MappedIdentifier<>(SAMPLE_PRODUCT_1.getName(), SAMPLE_PRODUCT_1),
			new MappedIdentifier<>(SAMPLE_PRODUCT_2.getName(), SAMPLE_PRODUCT_2),
			new MappedIdentifier<>(SAMPLE_PRODUCT_3.getName(), SAMPLE_PRODUCT_3)
	));

	public static final SearchQuery SAMPLE_SEARCH_QUERY = new SearchQuery(
			"Aspirin",
			"Acetylsalicylsäure",
			List.of(Dosage.of(10, "mg")),
			List.of(new Amount(BigDecimal.ONE, "ml")));

	public static final DbAmount SAMPLE_DB_AMOUNT_1 = new DbAmount(new BigDecimal(5), "ml");
	public static final DbAmount SAMPLE_DB_AMOUNT_2 = new DbAmount(new BigDecimal("7.5"), "g");
	public static final DbAmount SAMPLE_DB_AMOUNT_3 = new DbAmount(BigDecimal.ONE, null);

	public static final DbDosage SAMPLE_DB_DOSAGE_1 = new DbDosage(BigDecimal.ONE, "mg");
	public static final DbDosage SAMPLE_DB_DOSAGE_2 = new DbDosage(new BigDecimal("1.4"), new BigDecimal("1.6"), "mg");

	public static final DbDosage SAMPLE_DB_DOSAGE_3 = new DbDosage(new BigDecimal(500), "ug");

}
