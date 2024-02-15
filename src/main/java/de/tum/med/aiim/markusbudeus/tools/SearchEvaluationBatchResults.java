package de.tum.med.aiim.markusbudeus.tools;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.Matcher;
import de.tum.med.aiim.markusbudeus.matcher.model.FinalMatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.model.ResultSet;

public class SearchEvaluationBatchResults {

	final static String[] searchTerms = new String[]{
			"Lorzaar", "EK -", "Salmeterol 50µg", "Tamoxifen", "Pregabalin 25mg", "Atropin rectal", "Partusisten", "Epoprostenol-Perf", "Paracetamol", "atropin", "Betamethason", "Ringerlaktat", "Glucose 20% + 5IE Altinsulin", "Ciprofloxazin", "DiPIDOLOR", "Prednisolon", "Droperidol", "Enalapril", "Fluoxetin 20mg", "Starlix", "Tramal 500 mg", "proHance", "Amilorid", "Pyridostigmin", "Aromasin", "Glicose 12,5%", "Pantozol", "Perlinganit", "Aldactone", "Meto-Tablinen", "Hamatopan", "Arelix ACE", "Bondiol 0,25µg", "Metoprolol", "Novalgin 5g", "Pantoprazol (2)", "Sodium flourescein", "Bondiol 0,25mg", "Hydroxybuttersäure", "Vit. B", "disoprivan 1%", "Jodid 125µg", "Bisoprolol", "Diovan 160/12,5", "Kaliumjodid", "ICG", "MCP", "Insulin aspart", "Adumbran", "Fosterspray", "Lopirin", "NaCl 0,9%", "Carbamacepin", "Glukose", "Furosemid", "Aloevert", "Dimen", "Psyquil", "ipatropium", "Sultanol", "pk Merz", "5I.E.  Insulin", "Dopamin", "Madopar", "Omap", "Simeticon-Tropfen", "Epoprostenol (Flolan)", "Melperon", "Urapid", "Omeprazol", "Hydrokortison", "Torem", "Oxygesic", "Captopril", "Enahexal 20mg", "Insulin I", "Tazobactam", "Pentoxiphyllin", "fragmin P forte", "Metoprolol 100mg", "Dicofenac", "Spiropent", "Codeinphosphattropfen 2%", "Sufenta lokal", "Clo", "Simvabeta", "Fluticason 250µg", "midazolam-Saft", "Insulin", "Argatroban", "Sufenta 0,5 µg/ml", "Codeinphosphat  2 %", "Lyrica", "Blutgerinnungsfaktor IX  Immunine", "Inzolen-HK", "HCT", "certomycin", "Lercanidipin (Carmen)", "Approvel", "Rantitdin"
	};

	public static void main(String[] args) {
		DatabaseConnection.password = "aiim-experimentation".toCharArray();

		DatabaseConnection.runSession(session -> {
			Matcher matcher = new Matcher(session);

			for (String searchTerm : searchTerms) {
				ResultSet<FinalMatchingTarget> results = matcher.performMatching(searchTerm);
				System.out.println(searchTerm + " -> " + results.bestResult);
			}
		});
	}

}
