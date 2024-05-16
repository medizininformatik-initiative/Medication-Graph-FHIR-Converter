package de.medizininformatikinitiative.medgraph.searchengine;

import de.medizininformatikinitiative.medgraph.searchengine.db.DbAmount;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Provides and/or generates lots of sample entities.
 *
 * @author Markus Budeus
 */
public class TestFactory {

	public static class Products {
		public static final Product ASPIRIN = new Product(1, "Aspirin Complex Granulat-Sticks 500 mg/30 mg Granulat");
		public static final Product DORMICUM_15 = new Product(2, "Dormicum® 15 mg/3 ml Injektionslösung");
		public static final Product DORMICUM_5 = new Product(3, "Dormicum® V 5 mg/5 ml Injektionslösung");
		public static final Product ANAPEN = new Product(4, "Anapen 300 µg kohlpharma Injektionslösung");
	}

	public static class Substances {
		public static final Substance ACETYLSALICYLIC_ACID = new Substance(1, "Acetylsalicylsäure");
		public static final Substance MIDAZOLAM = new Substance(2, "Midazolam");
		public static final Substance MIDAZOLAM_HYDROCHLORIDE = new Substance(3, "Midazolam hydrochlorid");
		public static final Substance EPINEPHRINE = new Substance(4, "Epinephrin");
		public static final Substance WATER = new Substance(5, "Wasser für Injektionszwecke");
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
