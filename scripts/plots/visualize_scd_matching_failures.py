#!/usr/bin/env python3
"""
Visualisiert die SCD Matching Failure Statistiken im Vergleich.

Vergleicht zwei JSON-Dateien (Proposed Approach vs. Paper Based) und erstellt ein
Balkendiagramm mit den verschiedenen Fehlerklassen, unterschieden nach Early Rejections
und Candidate Validation Failures.

Verwendet zwei Blau- und zwei Orangentöne:
- Hellerer Blau/Orange für Proposed Approach
- Dunklerer Blau/Orange für Paper Based
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
    print("Usage: python visualize_scd_matching_failures.py <proposed_approach.json> <paper_based.json>")
    print("Example: python visualize_scd_matching_failures.py ../../medgraph/scd_matching_statistics.json ../../medgraph/scd_matching_statistics_paper_mapping.json")
    sys.exit(1)

json_file_proposed = sys.argv[1]
json_file_paper = sys.argv[2]

for f in [json_file_proposed, json_file_paper]:
    if not os.path.exists(f):
        print(f"Fehler: JSON-Datei nicht gefunden: {f}")
        sys.exit(1)

# Lade Daten
with open(json_file_proposed, 'r') as f:
    data_proposed = json.load(f)

with open(json_file_paper, 'r') as f:
    data_paper = json.load(f)

# Extrahiere Daten
total_products = data_proposed.get("totalProducts", 0)
total_drugs = data_proposed.get("totalDrugs", 0)
successful_matches_proposed = data_proposed.get("successfulMatches", 0)
successful_matches_paper = data_paper.get("successfulMatches", 0)

failures_proposed = data_proposed.get("failures", {})
failures_paper = data_paper.get("failures", {})
early_rejections_proposed = data_proposed.get("earlyRejections", {})
early_rejections_paper = data_paper.get("earlyRejections", {})
validation_failures_proposed = data_proposed.get("validationFailures", {})
validation_failures_paper = data_paper.get("validationFailures", {})

# Definiere Fehlerklassen in der gewünschten Reihenfolge
failure_categories = {
    # Early Rejections
    "NO_ACTIVE_INGREDIENTS": "No active ingredients",
    "NO_VALID_INGREDIENT_MATCHES": "No valid ingredient matches",
    "NO_RXNORM_DOSE_FORM_MAPPING": "No RxNorm dose form mapping",
    "NO_SCD_CANDIDATES": "No SCD candidates found",
    # Candidate Validation Failures
    "DOSE_FORM_MISMATCH": "Dose form mismatch",
    "INGREDIENTS_MISMATCH": "Ingredients mismatch",
    "STRENGTH_MISMATCH": "Strength mismatch",
    "SCORE_TOO_LOW": "Score too low"
}

# Farben
COLORS = {
    "early_proposed": "#5DADE2",      # Hellerer Blau für Early Rejections - Proposed
    "early_paper": "#1F77B4",         # Dunklerer Blau für Early Rejections - Paper
    "validation_proposed": "#F5B041",  # Hellerer Orange für Validation Failures - Proposed
    "validation_paper": "#E67E22"     # Dunklerer Orange für Validation Failures - Paper
}

# Bereite Daten für das Diagramm vor
failure_names = []
counts_proposed = []
counts_paper = []
colors_proposed = []
colors_paper = []
percentages_proposed = []
percentages_paper = []

for failure_key, failure_label in failure_categories.items():
    count_proposed = failures_proposed.get(failure_key, 0)
    count_paper = failures_paper.get(failure_key, 0)
    
    # Zeige Kategorie, wenn mindestens einer der Werte > 0 ist
    if count_proposed > 0 or count_paper > 0:
        failure_names.append(failure_label)
        counts_proposed.append(count_proposed)
        counts_paper.append(count_paper)
        
        percentage_proposed = (count_proposed / total_drugs) * 100
        percentage_paper = (count_paper / total_drugs) * 100
        percentages_proposed.append(percentage_proposed)
        percentages_paper.append(percentage_paper)
        
        # Bestimme Farbe basierend auf Fehlertyp
        if failure_key in early_rejections_proposed or failure_key in early_rejections_paper:
            colors_proposed.append(COLORS["early_proposed"])
            colors_paper.append(COLORS["early_paper"])
        elif failure_key in validation_failures_proposed or failure_key in validation_failures_paper:
            colors_proposed.append(COLORS["validation_proposed"])
            colors_paper.append(COLORS["validation_paper"])
        else:
            colors_proposed.append("#95A5A6")  # Grau als Fallback
            colors_paper.append("#95A5A6")

# Erstelle das Diagramm
fig, ax = plt.subplots(figsize=(16, 9))
x = np.arange(len(failure_names))
width = 0.35

# Erstelle Balken
bars_proposed = ax.bar(x - width/2, counts_proposed, width, 
                       color=colors_proposed, label='Proposed Approach')
bars_paper = ax.bar(x + width/2, counts_paper, width, 
                    color=colors_paper, label='Paper Based')

# Füge absolute Zahlen und Prozentwerte über jedem Balken hinzu
for i, (bar_prop, bar_paper, count_prop, count_paper, pct_prop, pct_paper) in enumerate(
    zip(bars_proposed, bars_paper, counts_proposed, counts_paper, 
        percentages_proposed, percentages_paper)
):
    # Proposed Approach
    if count_prop > 0:
        height_prop = bar_prop.get_height()
        ax.text(
            bar_prop.get_x() + bar_prop.get_width() / 2.,
            height_prop,
            f'{count_prop:,}\n({pct_prop:.1f}%)',
            ha='center',
            va='bottom',
            fontsize=9,
            fontweight='bold',
            linespacing=1.2
        )
    
    # Paper Based
    if count_paper > 0:
        height_paper = bar_paper.get_height()
        ax.text(
            bar_paper.get_x() + bar_paper.get_width() / 2.,
            height_paper,
            f'{count_paper:,}\n({pct_paper:.1f}%)',
            ha='center',
            va='bottom',
            fontsize=9,
            fontweight='bold',
            linespacing=1.2
        )

# Erstelle sekundäre Y-Achse für Prozentwerte
ax2 = ax.twinx()
max_percentage = max(max(percentages_proposed) if percentages_proposed else 0, 
                     max(percentages_paper) if percentages_paper else 0)
ax2.set_ylim(0, max_percentage * 1.1)
ax2.set_ylabel("Percentage of Total Drugs (%)", fontsize=12, color='gray')
ax2.tick_params(axis='y', labelcolor='gray')

# Haupt-Y-Achse für absolute Werte
ax.set_ylabel("Number of Drugs", fontsize=12, fontweight='bold')
ax.set_xlabel("Failure Category", fontsize=12, fontweight='bold')
ax.set_xticks(x)
ax.set_xticklabels(failure_names, rotation=45, ha='right')

ax.set_title(
    f"SCD Matching Failure Analysis - Comparison\n"
    f"Total Products: {total_products:,} | Total Drugs: {total_drugs:,}\n"
    f"Proposed Approach: {successful_matches_proposed:,} matches ({successful_matches_proposed/total_drugs*100:.1f}%) | "
    f"Paper Based: {successful_matches_paper:,} matches ({successful_matches_paper/total_drugs*100:.1f}%)",
    fontsize=14,
    fontweight='bold',
    pad=20
)
ax.grid(axis='y', alpha=0.3, linestyle='--')

# Legende
legend_elements = [
    Patch(facecolor=COLORS["early_proposed"], label='Early Rejections (Proposed)'),
    Patch(facecolor=COLORS["early_paper"], label='Early Rejections (Paper)'),
    Patch(facecolor=COLORS["validation_proposed"], label='Validation Failures (Proposed)'),
    Patch(facecolor=COLORS["validation_paper"], label='Validation Failures (Paper)')
]
ax.legend(handles=legend_elements, loc='upper right', fontsize=11, framealpha=0.9)

plt.tight_layout()

# Speichere den Plot
output_file = 'scd_matching_failures_comparison.png'
plt.savefig(output_file, dpi=300, bbox_inches='tight')
print(f"Plot gespeichert als: {output_file}")

# Zeige Zusammenfassung
print("\n" + "="*70)
print("ZUSAMMENFASSUNG")
print("="*70)
print(f"Anzahl Produkte insgesamt:              {total_products:>10,}")
print(f"Anzahl Drugs insgesamt:                 {total_drugs:>10,}")
print(f"Anzahl erfolgreiche Matches (Proposed): {successful_matches_proposed:>10,} ({successful_matches_proposed/total_drugs*100:>6.2f}%)")
print(f"Anzahl erfolgreiche Matches (Paper):    {successful_matches_paper:>10,} ({successful_matches_paper/total_drugs*100:>6.2f}%)")
print("="*70)

# Zeige den Plot
plt.show()
