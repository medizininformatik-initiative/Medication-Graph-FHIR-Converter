package de.tum.med.aiim.markusbudeus.tools;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.Matcher;
import de.tum.med.aiim.markusbudeus.matcher.model.FinalMatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.model.ResultSet;

public class SearchEvaluationBatchResults {

	final static String[] searchTerms = new String[]{
			"ACC",
			"Aspirin",
			"Aciclovir 400mg",
			"Actreonam",
			"Butylscapolaminiumbromid",
			"Celebrex 100mg",
			"Cimetidin acis",
			"Plavix",
			"Dexa",
			"Diazepam 10mg",
			"Adrenalin",
			"Fenofibrat retard",
			"Fungata 1 Hartkaps. N1",
			"Glucose 5%",
			"SANCUSO",
			"HCT",
			"Ibu",
			"Isoniacid",
			"KCl",
			"Magnesium",
			"MCP",
			"Midazolam 5mg",
			"NaCl",
			"OMEP",
			"Paracetamol 500mg",
			"Dipidolor",
			"Ramipil",
			"Rivarox Filmtbl",
			"Daxas",
			"Viagra",
			"Simva",
			"Thyamazol",
			"Vitamin B1",
			"Drometamol",
			"LIFOPAN Gas",
			"Teicoplanin Noridem",
			"Nitrolingual 1mg/ml Infusionslsg.",
			"Topiramat AL",
			"Chloralhydrat",
			"Darbepoetin alfa 100 Mikrogramm",
			"Sulfamethoxzaol 400mg Susp. zum Einnehmen",
			"Simdax 2,5 mg / ml Konzentrat",
			"Ezetimib 10 mg Tbl.",
			"Anidulafungin Pulver für Inf.-Lsg.",
			"Hypnomidate Injektionslsg.",
			"Marcoumar Gerke 3 mg Tabletten",
			"Haoperidol-neuraxpharm Tbl.",
			"Slennyto",
			"Iloprost",
			"Captohexal Tabletten",
			"Multovitan N",
			"Pregabalin beta Hartkapseln",
			"Famotidin",
			"Ranolazin",
			"GERNEBCIN 160 mg/2ml Lösung",
			"SmofKabifen Low Osmo",
			"Biofanal Salbe u. Vaginaltbl.",
			"TachoSil",
			"ENEAS® Tabletten",
			"Acetazolamid 250 mg Tbl",
			"Voraconizol Heumann",
			"EREMFAT",
			"Zanamivir Pulver zur Inhalation",
			"Tavro Tabletten",
			"Arilin",
			"Oxa-CT Tabletten",
			"Digitoxin 0.1mg",
			"Transexamsäure Carinopharm",
			"Valproinsaure 300 mg",
			"Posaconazol",
			"Prednisolon",
			"Molsidomin Heumann",
			"Torasemid",
			"ZUYVOXID",
			"Daptomycin Hikma",
			"Fidaxomicin",
			"Ciprofloxacin 250 mg/5 ml",
			"Isofluran",
			"Moxonidin",
			"Eptifibatid Injektionslösung",
			"Rifaximin Filmtabletten",
			"Ambrisentan",
			"Gabapentin",
			"Carbamazepin Heumann 600 mg Retardtabletten",
			"Simulect Pulver zur Herst. einer Inj.-/Inf.-Lsg.",
			"Desfluran 250ml",
			"Tardyferon-Fol",
			"Sempera Lösung zum Einnehmen",
			"Luminaletten",
			"Propofol MCT Fresenius",
			"Natriumbenzoat",
			"Artenolol",
			"Aminophyllin",
			"Inspra kohlpharma",
			"Caverject Impuls",
			"ROTOP-Adenosin",
			"Mycobutin Abacus Hartkapseln",
			"Plenadren",
			"Gilurytmal Injektionslösung",
			"Apikaban",
	};

	public static void main(String[] args) {
		DatabaseConnection.password = "REDACTED".toCharArray();

		DatabaseConnection.runSession(session -> {
			Matcher matcher = new Matcher(session);

			for (String searchTerm: searchTerms) {
				ResultSet<FinalMatchingTarget> results = matcher.performMatching(searchTerm);
				System.out.println(searchTerm +" -> " + results.bestResult);
			}
		});
	}

}
