package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Markus Budeus
 */
public class QueryRefiner {

	public QueryRefiner() {
	}

	public void refine(RawQuery query) {


	}

	private void parseDosageInformation() {

	}

	private void parseDoseFormInformation() {

	}

	private void parseSubstances() {

	}


	private class LocalQueryRefiner {

		private final RawQuery query;
		private final StepwiseGeneralQueryParser globalQueryParser;

		private List<Dosage> dosages;
		private List<Amount> amounts;

		private SubstringUsageStatement globalQueryDosageUsageStatement;

		LocalQueryRefiner(RawQuery query) {
			this.query = query;

			if (query.query.isBlank()) {
				globalQueryParser = null;
			} else {
				globalQueryParser = new StepwiseGeneralQueryParser(query.query);
			}
		}

		void parseDosages(DosageQueryParser dosageQueryParser) {
			List<Dosage> dosages = new ArrayList<>();
			List<Amount> amounts = new ArrayList<>();
			if (!query.dosages.isBlank()) {
				DosageQueryParser.Result r = dosageQueryParser.parse(query.dosages);
				dosages.addAll(r.getDosages());
				amounts.addAll(r.getAmounts());
			}
			parseGlobal(query -> {
				DosageQueryParser.Result r = dosageQueryParser.parse(query);
				dosages.addAll(r.getDosages());
				amounts.addAll(r.getAmounts());
				return r.getUsageStatement();
			});
			this.dosages = dosages;
			this.amounts = amounts;
		}

		private SubstringUsageStatement parseGlobal(Function<String, SubstringUsageStatement> parser) {
			if (globalQueryParser != null) {
				return globalQueryParser.useRemainingQueryParts(parser);
			}
			return null;
		}


	}

}
