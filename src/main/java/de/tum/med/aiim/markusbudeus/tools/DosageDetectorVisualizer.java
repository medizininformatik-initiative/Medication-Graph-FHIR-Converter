package de.tum.med.aiim.markusbudeus.tools;

import de.tum.med.aiim.markusbudeus.matcher.tools.DosageDetector;

import java.util.List;

public class DosageDetectorVisualizer {

	private static final List<String> LIST = List.of(
			"Presinol® 125",
			"Salagen 5 mg docpharm Filmtabletten",
			"Sulfuricum acidum LM 18 Spagyros Dilution",
			"Damara® 75 Mikrogramm Filmtabletten",
			"Glechoma hederacea LM 21",
			"Kalium carbonicum D6 DHU Dilution",
			"Zepatier 50 mg/100 mg Orifarm Filmtabletten",
			"Cinnamomum C15 Globuli Ind-Fert",
			"Imeron® 350, 350 mg Iod/ml, Injektionslösung, Infusionslösung, 200 ml",
			"Calcium chloratum C10 Globuli Ind-Fert",
			"Cartilago Artic. Coxae Gl D30 Wala Ampullen",
			"Risperdal Consta 37,5 mg Abacus Pulver und Lösungsmittel zur Herstellung einer Depot-Injektionssuspension",
			"MIK Komplex Anthroposan Tropf.",
			"Fagopyrum C12 Globuli Ind-Fert",
			"Enalapril comp.-CT 10 mg/25 mg Tabletten",
			"milgamma® mono 150 überzog. Tbl.",
			"Levetiracetam Aurobindo 1000 mg Filmtabletten",
			"Manganum aceticum D6 DHU Dilution",
			"UV - Licht LM 06",
			"Endoxan 1 g kohlpharma",
			"Passiflora incarnata LM 24",
			"Cinchonium sulfuricum LM 17",
			"Senna LM 05",
			"Acidum salicylicum C30 DHU Globuli",
			"Farina tritici vulgaris LM 05",
			"Resochin Tabletten 250 mg ACA Filmtabletten",
			"Bevespi Aerosphere 7,2 Mikrogramm/5 Mikrogramm Eurim Druckgasinhalation, Suspension",
			"Aluminium metallicum Q28 Gudjons Lösung",
			"Ciclosporin dura® 25mg Weichkapseln",
			"Citrus vulgaris LM 07",
			"Vinca minor LM 22",
			"Alstonia scholaris LM 26",
			"Lamotrigin beta 50 mg Tabletten zur Herstellung einer Suspension zum Einnehmen",
			"Lophakomp® Procain 2ml",
			"Ammonium benzoicum LM 45",
			"Erysimum officinale LM 22",
			"Carbo animalis D6 DHU Tabletten",
			"Dronedaron AL 400 mg Filmtabletten",
			"Fraxinus ornus LM 13",
			"Mercurius solubilis H. Q29 Gudjons Lösung",
			"Calciumacetat-Nefro 950 mg, Filmtabletten",
			"Omnitrope 10 mg/1,5 ml kohlpharma Injektionslösung",
			"Unkrautpollen LM 17",
			"Azilect 1 mg Haemato-Pharm Tabletten",
			"Temozolomid ratiopharm 100 mg BB Farma Hartkapseln",
			"Lisinopril 5 - 1 A Pharma®, Tbl.",
			"Tonca LM 75",
			"Oxydendron arboreum LM 09",
			"Okoubaka LM 12 Spagyros Dilution",
			"Chamomilla D12 Globuli Pflüger Dosierspender",
			"Chirata indica LM 90",
			"Magnesium phos. D12 Anthroposan Dil.",
			"Strychninum phosphoricum LM 08",
			"Senna LM 75",
			"Veronica beccabunga LM 01",
			"Cuprum oxydatum nigrum C10 Globuli Ind-Fert",
			"Lamivudin Teva 100 mg Eurim Filmtabletten",
			"Ondansetron Hikma 4 mg/2 ml Injektionslösung",
			"Stachys betonica LM 04",
			"Spiraea ulmaria LM 04",
			"Plumbum jodatum LM 23",
			"Alumen D20 Globuli Ind-Fert",
			"Sambucus nigra LM XVIII Globuli Ind-Fert",
			"Mercurius vivus C30 Gudjons Globuli",
			"Picrinicum acidum Q2 Gudjons Lösung",
			"Funiculus umbilicalis Gl D8 Wala Ampullen",
			"Kalium chloricum LM 90",
			"Calendula D2 DHU Dilution",
			"Bisoprolol Accord 2,5 mg Filmtabletten",
			"Aluminium aceticum LM 08",
			"Podophyllinum LM 60",
			"Acara Trio 35 mg + 500 mg / 1.000 I.E. Filmtabletten + Granulat",
			"Salvia officinalis LM 02",
			"hysan® Schnupfenspray (ohne Konservierungsmittel)",
			"Cuprum aceticum LM 20",
			"CALCIGEN D 600 mg/400 I.E. Kautabletten",
			"Rosmarinus officinalis LM 15",
			"Echinacea C100 Globuli Ind-Fert",
			"Olibanum LM 08",
			"Aconitum lycoctonum LM 36",
			"Coffeinum LM 04",
			"Aluminium sulfuricum LM 22",
			"Spongia LM 19",
			"Olimel 5,7% E 2000 ml + Cernevit + Addel Trace Set",
			"Fluvastatin AbZ 20 mg Hartkapseln",
			"Retacrit 4000 I.E./0,4ml Gerke Injektionslösung in einer Fertigspr.",
			"Conium D7 Globuli Ind-Fert",
			"Ferrum muriaticum LM 06",
			"Pirfenidon Zentiva 267 mg Filmtabletten",
			"Cuprior 150 mg CC Pharma Filmtabletten",
			"Walkhoffs Jodoform Dentalpaste",
			"Helonias dioica C4 Globuli Ind-Fert",
			"Ammonium muriaticum C 200 Spagyros Dilution",
			"Lilium tigrinum LM XXIV Globuli Ind-Fert",
			"Spigelia marylandica LM 23",
			"Sulfonalum LM 10",
			"Sogoon® Filmtabletten",
			"Mirtazapin-ratiopharm® 30 mg Filmtabletten",
			"REQUIP 5 mg Filmtabletten",
			"Syzygium jambolanum LM 08",
			"Ergotamin LM 29");


	private static final String ANSI_GREEN = "\033[32m";
	private static final String ANSI_RESET = "\033[0m";

	public static void main(String[] args) {
		LIST.forEach(DosageDetectorVisualizer::detectAndPrintDosage);
	}

	public static void detectAndPrintDosage(String product) {
		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(product);

		StringBuilder result = new StringBuilder(product);

		int offset = 0;
		for (DosageDetector.DetectedDosage dosage : dosages) {
			result.insert(dosage.getStartIndex() + offset, ANSI_GREEN);
			offset += ANSI_GREEN.length();
			result.insert(dosage.getStartIndex() + dosage.getLength() + offset, ANSI_RESET);
			offset += ANSI_RESET.length();
		}

		System.out.println(result.toString());
	}

}
