package de.medizininformatikinitiative.medgraph.searchengine;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;

/**
 * Provides and/or generates lots of sample entities.
 *
 * @author Markus Budeus
 */
public class TestFactory {

	public static final Substance SAMPLE_SUBSTANCE_1 = new Substance(107, "Acetylsalicylsäure");
	public static final Substance SAMPLE_SUBSTANCE_2 = new Substance(88, "Midazolam");
	public static final Substance SAMPLE_SUBSTANCE_3 = new Substance(65, "Ibuprofen");
	public static final Product SAMPLE_PRODUCT_1 = new Product(31, "Trental® 400 mg, Retardtabletten");
	public static final Product SAMPLE_PRODUCT_2 = new Product(107, "ASS-ratiopharm® 500 mg, Tabletten");
	public static final Product SAMPLE_PRODUCT_3 = new Product(222, "Lasix® 20 mg Injektionslösung");

}
