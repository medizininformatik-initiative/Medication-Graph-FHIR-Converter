package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Result of a {@link QueryRefiner}, this object holds the generated search query and additional information on
 * how it was generated from the original query.
 *
 * @author Markus Budeus
 */
public class RefinedQuery {

	/**
	 * The generated search query.
	 */
	@NotNull
	private final SearchQuery searchQuery;
	/**
	 * Usage statement indicating which parts of the dosage search term from the raw query were successfully parsed.
	 * If said search term was blank in the raw query, this is null.
	 */
	@Nullable
	private final SubstringUsageStatement dosageUsageStatement;
	/**
	 * Usage statement indicating which parts of the general search term from the raw query were parsed into dosage
	 * information. If the general search term in the raw query was blank, this is null.
	 */
	@Nullable
	private final SubstringUsageStatement dosageGeneralSearchTermUsageStatement;
	/**
	 * Usage statement indicating which parts of the dose form search term from the raw query were successfully parsed.
	 * If said search term was blank in the raw query, this is null.
	 */
	@Nullable
	private final SubstringUsageStatement doseFormUsageStatement;
	/**
	 * Usage statement indicating which parts of the general search term from the raw query were parsed into dose form
	 * information. If the general search term in the raw query was blank, this is null.
	 */
	@Nullable
	private final SubstringUsageStatement doseFormGeneralSearchTermUsageStatement;

	public RefinedQuery(@NotNull SearchQuery searchQuery, @Nullable SubstringUsageStatement dosageUsageStatement,
	                     @Nullable SubstringUsageStatement dosageGeneralSearchTermUsageStatement,
	                     @Nullable SubstringUsageStatement doseFormUsageStatement,
	                     @Nullable SubstringUsageStatement doseFormGeneralSearchTermUsageStatement) {
		this.searchQuery = searchQuery;
		this.dosageUsageStatement = dosageUsageStatement;
		this.dosageGeneralSearchTermUsageStatement = dosageGeneralSearchTermUsageStatement;
		this.doseFormUsageStatement = doseFormUsageStatement;
		this.doseFormGeneralSearchTermUsageStatement = doseFormGeneralSearchTermUsageStatement;
	}

	public @NotNull SearchQuery getSearchQuery() {
		return searchQuery;
	}

	public @Nullable SubstringUsageStatement getDosageUsageStatement() {
		return dosageUsageStatement;
	}

	public @Nullable SubstringUsageStatement getDosageGeneralSearchTermUsageStatement() {
		return dosageGeneralSearchTermUsageStatement;
	}

	public @Nullable SubstringUsageStatement getDoseFormUsageStatement() {
		return doseFormUsageStatement;
	}

	public @Nullable SubstringUsageStatement getDoseFormGeneralSearchTermUsageStatement() {
		return doseFormGeneralSearchTermUsageStatement;
	}

}
