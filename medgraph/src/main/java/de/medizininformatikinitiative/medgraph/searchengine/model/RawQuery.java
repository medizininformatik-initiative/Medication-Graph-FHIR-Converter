package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;

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
}
