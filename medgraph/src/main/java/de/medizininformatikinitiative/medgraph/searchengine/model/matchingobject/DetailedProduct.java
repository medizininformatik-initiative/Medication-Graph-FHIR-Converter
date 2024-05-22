package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.medizininformatikinitiative.medgraph.searchengine.model.Drug;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Product}-extension providing detailed information about a product.
 *
 * @author Markus Budeus
 */
public class DetailedProduct extends Product {

	/**
	 * The product's assigned PZNs ("Pharmazentralnummern").
	 */
	@NotNull
	private final List<String> pzn;
	/**
	 * The drugs this product consists of.
	 */
	@NotNull
	private final List<Drug> drugs;

	public DetailedProduct(long mmiId, String name, @NotNull List<String> pzn, @NotNull List<Drug> drugs) {
		super(mmiId, name);
		this.pzn = Collections.unmodifiableList(new ArrayList<>(pzn));
		this.drugs = Collections.unmodifiableList(new ArrayList<>(drugs));
	}

	/**
	 * Returns all known PZNs ("Pharmazentralnummern") for this product.
	 */
	@NotNull
	public List<String> getPzn() {
		return pzn;
	}

	/**
	 * Returns all drugs which are part of this product.
	 */
	@NotNull
	public List<Drug> getDrugs() {
		return drugs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		DetailedProduct that = (DetailedProduct) o;
		return Objects.equals(pzn, that.pzn) && Objects.equals(drugs, that.drugs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), pzn, drugs);
	}
}
