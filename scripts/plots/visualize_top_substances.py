#!/usr/bin/env python3
"""
Visualisiert die Top 20 Substanzen SCD Coverage Analyse im Vergleich.

Vergleicht zwei JSON-Dateien (Proposed Approach vs. Custom Mapping) und erstellt ein
Balkendiagramm mit der Coverage für jede Substanz, sortiert nach Rang.

Verwendet verschiedene Farbcodes für die beiden Ansätze.
"""

import json
import matplotlib.pyplot as plt
from matplotlib.patches import Patch
import sys
import os
import numpy as np

# --------------------------------------------------
# INPUT
# --------------------------------------------------
if len(sys.argv) < 3:
    print("Usage: python visualize_top_substances.py <paper_mapping.json> <proposed_mapping.json>")
    print("Example: python visualize_top_substances.py ../../output/analysis/top_substances_analysis.json ../../output/analysis/top_substances_analysis_custom_mapping.json")
    sys.exit(1)

json_file_paper = sys.argv[1]
json_file_proposed = sys.argv[2]

for f in [json_file_paper, json_file_proposed]:
    if not os.path.exists(f):
        print(f"Fehler: JSON-Datei nicht gefunden: {f}")
        sys.exit(1)

# Lade Daten
with open(json_file_paper, 'r') as f:
    data_paper = json.load(f)

with open(json_file_proposed, 'r') as f:
    data_proposed = json.load(f)

# Extrahiere Daten
substances_paper = data_paper.get("substances", [])
substances_proposed = data_proposed.get("substances", [])
summary_paper = data_paper.get("summary", {})
summary_proposed = data_proposed.get("summary", {})

# Mapping von deutschen Namen (aus JSON) zu englischen Namen (für Plot)
GERMAN_TO_ENGLISH = {
    "Ibuprofen": "Ibuprofen",
    "Metamizol-Natrium": "Metamizole sodium",
    "Pantoprazol": "Pantoprazole",
    "Levothyroxin-Natrium": "Levothyroxine sodium",
    "Ramipril": "Ramipril",
    "Bisoprolol": "Bisoprolol",
    "Candesartan": "Candesartan",
    "Amlodipin": "Amlodipine",
    "Atorvastatin": "Atorvastatin",
    "Metoprolol": "Metoprolol",
    "Torasemid": "Torsemide",
    "Amoxicillin": "Amoxicillin",
    "Salbutamol": "Albuterol",
    "Simvastatin": "Simvastatin",
    "Prednisolon": "Prednisolone",
    "Metformin": "Metformin",
    "Diclofenac": "Diclofenac",
    "Acetylsalicylsäure": "Acetylsalicylic acid",
    "Colecalciferol": "Cholecaliferol",
    "Beta-Lactamase-Inhibitoren": "Beta-lactamase inhibitors"
}

# Erstelle Dictionaries für schnellen Zugriff auf Substanzen nach Name (deutsche Namen)
substances_dict_paper = {s.get("name", ""): s for s in substances_paper}
substances_dict_proposed = {s.get("name", ""): s for s in substances_proposed}

# Erwartete Reihenfolge der Top 20 Substanzen nach Rang
expected_substances_english = [
    "Ibuprofen",
    "Metamizole sodium",
    "Pantoprazole",
    "Levothyroxine sodium",
    "Ramipril",
    "Bisoprolol",
    "Candesartan",
    "Amlodipine",
    "Atorvastatin",
    "Metoprolol",
    "Torsemide",
    "Amoxicillin",
    "Albuterol",
    "Simvastatin",
    "Prednisolone",
    "Metformin",
    "Diclofenac",
    "Acetylsalicylic acid",
    "Cholecaliferol",
    "Beta-lactamase inhibitors"
]

# Erstelle Reverse-Mapping (englisch -> deutsch) für Datenabfrage
ENGLISH_TO_GERMAN = {v: k for k, v in GERMAN_TO_ENGLISH.items()}

# Farben für die beiden Ansätze basierend auf Coverage
# Grün: ≥ 70%, Orange: 30% bis < 70%, Rot: < 30%
COLORS = {
    "proposed_green": "#7DCEA0",      # Helleres Grün für Proposed (≥ 70%)
    "custom_green": "#27AE60",        # Dunkleres Grün für Custom (≥ 70%)
    "proposed_orange": "#FFA500",     # Klares Orange für Proposed (30% bis < 70%)
    "custom_orange": "#FF8C00",       # Dunkleres Orange für Custom (30% bis < 70%)
    "proposed_red": "#FF6B6B",        # Klares Hellrot für Proposed (< 30%)
    "custom_red": "#DC143C"           # Klares Dunkelrot für Custom (< 30%)
}

def get_color_paper(coverage):
    """Bestimmt die Farbe für Paper Mapping basierend auf Coverage."""
    if coverage >= 70:
        return COLORS["custom_green"]
    elif coverage >= 30 and coverage < 70:
        return COLORS["custom_orange"]
    else:
        return COLORS["custom_red"]

def get_color_proposed(coverage):
    """Bestimmt die Farbe für Proposed Mapping basierend auf Coverage."""
    if coverage >= 70:
        return COLORS["proposed_green"]
    elif coverage >= 30 and coverage < 70:
        return COLORS["proposed_orange"]
    else:
        return COLORS["proposed_red"]

# Bereite Daten für das Diagramm vor - verwende die Rang-Reihenfolge (NICHT nach Coverage sortieren!)
substance_data = []
for rank, english_name in enumerate(expected_substances_english, 1):
    # Finde deutschen Namen für diese englische Substanz
    german_name = ENGLISH_TO_GERMAN.get(english_name, english_name)
    
    # Paper Mapping Daten
    if german_name in substances_dict_paper:
        substance_paper = substances_dict_paper[german_name]
        found_in_neo4j_paper = substance_paper.get("foundInNeo4j", False)
        coverage_paper = substance_paper.get("coverage", 0) if found_in_neo4j_paper else 0
        total_paper = substance_paper.get("totalDrugs", 0) if found_in_neo4j_paper else 0
        matched_paper = substance_paper.get("matchedDrugs", 0) if found_in_neo4j_paper else 0
    else:
        found_in_neo4j_paper = False
        coverage_paper = 0
        total_paper = 0
        matched_paper = 0
    
    # Proposed Mapping Daten
    if german_name in substances_dict_proposed:
        substance_proposed = substances_dict_proposed[german_name]
        found_in_neo4j_proposed = substance_proposed.get("foundInNeo4j", False)
        coverage_proposed = substance_proposed.get("coverage", 0) if found_in_neo4j_proposed else 0
        total_proposed = substance_proposed.get("totalDrugs", 0) if found_in_neo4j_proposed else 0
        matched_proposed = substance_proposed.get("matchedDrugs", 0) if found_in_neo4j_proposed else 0
    else:
        found_in_neo4j_proposed = False
        coverage_proposed = 0
        total_proposed = 0
        matched_proposed = 0
    
    substance_data.append({
        "name": english_name,  # Verwende englischen Namen für Plot
        "rank": rank,
        "foundInNeo4j_paper": found_in_neo4j_paper,
        "coverage_paper": coverage_paper,
        "total_paper": total_paper,
        "matched_paper": matched_paper,
        "foundInNeo4j_proposed": found_in_neo4j_proposed,
        "coverage_proposed": coverage_proposed,
        "total_proposed": total_proposed,
        "matched_proposed": matched_proposed
    })

# Extrahiere Listen für das Diagramm (in Rang-Reihenfolge)
substance_names = []
coverage_paper_values = []
coverage_proposed_values = []
total_drugs_paper = []
total_drugs_proposed = []
matched_drugs_paper = []
matched_drugs_proposed = []

for data_item in substance_data:
    name = data_item["name"]
    substance_names.append(name)
    coverage_paper_values.append(data_item["coverage_paper"])
    coverage_proposed_values.append(data_item["coverage_proposed"])
    total_drugs_paper.append(data_item["total_paper"])
    total_drugs_proposed.append(data_item["total_proposed"])
    matched_drugs_paper.append(data_item["matched_paper"])
    matched_drugs_proposed.append(data_item["matched_proposed"])

# Erstelle das Diagramm (höher für mehr Abstand zwischen Balken)
fig, ax = plt.subplots(figsize=(16, 12))

# Y-Positionen für die Balken (größerer Abstand zwischen den Substanzen)
y_pos = np.arange(len(substance_names)) * 1.2
bar_height = 0.45

# Bestimme Farben für jeden Balken basierend auf Coverage
colors_paper = [get_color_paper(cov) for cov in coverage_paper_values]
colors_proposed = [get_color_proposed(cov) for cov in coverage_proposed_values]

# Erstelle horizontale Balken nebeneinander mit individuellen Farben
bars_paper = []
bars_proposed = []
for i in range(len(substance_names)):
    bar_paper_bar = ax.barh(y_pos[i] - bar_height/2, coverage_paper_values[i], bar_height,
                             color=colors_paper[i], left=0)
    bar_prop = ax.barh(y_pos[i] + bar_height/2, coverage_proposed_values[i], bar_height,
                       color=colors_proposed[i], left=0)
    bars_paper.append(bar_paper_bar[0])
    bars_proposed.append(bar_prop[0])

# Füge Coverage-Werte und absolute Zahlen zu jedem Balken hinzu
for i, (bar_paper_bar, bar_prop, cov_paper, cov_prop, total_paper, total_prop, 
        matched_paper, matched_prop, name) in enumerate(
    zip(bars_paper, bars_proposed, coverage_paper_values, coverage_proposed_values,
        total_drugs_paper, total_drugs_proposed, matched_drugs_paper, matched_drugs_proposed,
        substance_names)
):
    # Paper Mapping
    if total_paper > 0:
        width_paper = bar_paper_bar.get_width()
        label_text_paper = f'{cov_paper:.1f}% ({matched_paper}/{total_paper})'
        ax.text(
            width_paper + 1,
            bar_paper_bar.get_y() + bar_paper_bar.get_height() / 2,
            label_text_paper,
            ha='left',
            va='center',
            fontsize=11,
            fontweight='bold'
        )
    
    # Proposed Mapping
    if total_prop > 0:
        width_prop = bar_prop.get_width()
        label_text_prop = f'{cov_prop:.1f}% ({matched_prop}/{total_prop})'
        ax.text(
            width_prop + 1,
            bar_prop.get_y() + bar_prop.get_height() / 2,
            label_text_prop,
            ha='left',
            va='center',
            fontsize=11,
            fontweight='bold'
        )

# Y-Achse: Substanznamen (in Rang-Reihenfolge)
# Formatierung für mehrzeilige Labels
display_names = [name.replace("Beta-lactamase inhibitors", "Beta-lactamase-\ninhibitors") 
                 for name in substance_names]
ax.set_yticks(y_pos)
ax.set_yticklabels(display_names, fontsize=12)
ax.invert_yaxis()  # Rang 1 oben
ax.tick_params(axis='y', labelsize=12)

# X-Achse: Coverage in Prozent
ax.set_xlabel("SCD Coverage (%)", fontsize=14, fontweight='bold')
ax.tick_params(axis='x', labelsize=11)
max_coverage = max(max(coverage_paper_values) if coverage_paper_values else 0,
                   max(coverage_proposed_values) if coverage_proposed_values else 0)
ax.set_xlim(0, max(max_coverage * 1.15, 10))

# Titel entfernt - Variablen werden noch für Zusammenfassung benötigt
overall_coverage_paper = summary_paper.get("overallCoverage", 0)
overall_coverage_proposed = summary_proposed.get("overallCoverage", 0)
total_drugs_all = summary_paper.get("totalDrugs", 0)
total_matched_paper = summary_paper.get("totalMatched", 0)
total_matched_proposed = summary_proposed.get("totalMatched", 0)

# Grid
ax.grid(axis='x', alpha=0.3, linestyle='--')

# Legende
legend_elements = [
    Patch(facecolor=COLORS["custom_green"], label='Paper Mapping (≥ 70%)'),
    Patch(facecolor=COLORS["proposed_green"], label='Proposed Mapping (≥ 70%)'),
    Patch(facecolor=COLORS["custom_orange"], label='Paper Mapping (30% bis < 70%)'),
    Patch(facecolor=COLORS["proposed_orange"], label='Proposed Mapping (30% bis < 70%)'),
    Patch(facecolor=COLORS["custom_red"], label='Paper Mapping (< 30%)'),
    Patch(facecolor=COLORS["proposed_red"], label='Proposed Mapping (< 30%)')
]
ax.legend(handles=legend_elements, loc='lower right', fontsize=12, framealpha=0.9)

plt.tight_layout()
# Füge minimales Padding links hinzu, damit lange Substanznamen vollständig sichtbar sind
plt.subplots_adjust(left=0.14)

# Speichere den Plot
output_file = 'top_substances_coverage_comparison.png'
plt.savefig(output_file, dpi=300, bbox_inches='tight')
print(f"Plot gespeichert als: {output_file}")

# Zeige Zusammenfassung
print("\n" + "="*70)
print("ZUSAMMENFASSUNG")
print("="*70)
print(f"Drugs insgesamt:                      {total_drugs_all:>10,}")
print(f"Drugs mit SCD Match (Paper):          {total_matched_paper:>10,} ({overall_coverage_paper:>6.1f}%)")
print(f"Drugs mit SCD Match (Proposed):       {total_matched_proposed:>10,} ({overall_coverage_proposed:>6.1f}%)")
print(f"Substanzen in Neo4j (Paper):          {summary_paper.get('substancesInNeo4j', 0):>10} / {summary_paper.get('totalSubstances', 0)}")
print(f"Substanzen in Neo4j (Proposed):       {summary_proposed.get('substancesInNeo4j', 0):>10} / {summary_proposed.get('totalSubstances', 0)}")
print("="*70)

# Zeige den Plot
plt.show()
