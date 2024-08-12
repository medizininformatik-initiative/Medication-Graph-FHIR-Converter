package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.util.List;
import java.util.Objects;

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

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		GraphProduct that = (GraphProduct) object;
		return mmiId == that.mmiId && Objects.equals(name, that.name) && Objects.equals(companyMmiId,
				that.companyMmiId) && Objects.equals(companyName, that.companyName) && Util.equalsIgnoreOrder(
				codes, that.codes) && Util.equalsIgnoreOrder(drugs, that.drugs) && Util.equalsIgnoreOrder(packages,
				that.packages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mmiId);
	}
}
