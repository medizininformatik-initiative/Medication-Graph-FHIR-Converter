package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Raw Search query terms provided by the user.
 *
 * @author Markus Budeus
 */
public class RawQuery {

	/**
	 * Raw, unspecific query text provided by the user.
	 */
	@NotNull
	public final String query;
	/**
	 * Query text for product names.
	 */
	@NotNull
	public final String product;
	/**
	 * Query text for substance (i.e. active ingredient) names.
	 */
	@NotNull
	public final String substance;

	public RawQuery(@NotNull String query, @NotNull String product, @NotNull String substance) {
		this.query = query;
		this.product = product;
		this.substance = substance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RawQuery rawQuery = (RawQuery) o;
		return Objects.equals(query, rawQuery.query) && Objects.equals(product,
				rawQuery.product) && Objects.equals(substance, rawQuery.substance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(query, product, substance);
	}
}
