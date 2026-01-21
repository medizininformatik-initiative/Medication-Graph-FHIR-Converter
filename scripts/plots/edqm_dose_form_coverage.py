#!/usr/bin/env python3
"""

Zeigt die Anzahl Drugs pro EDQM Dose Form und ob ein Mapping existiert.

Verwendung:
    # Mit venv aktiviert:
    source venv/bin/activate
    python3 edqm_dose_form_coverage.py
    
    
"""

import matplotlib.pyplot as plt

# Daten: Top 20 EDQM Dose Forms
dose_forms = [
    "Granules",
    "Film-coated tablet",
    "Solution for injection",
    "Tablet",
    "Capsule, hard",
    "Oral drops, solution",
    "Prolonged-release tablet",
    "Concentrate for solution for infusion",
    "Solution for infusion",
    "Powder for solution for injection",
    "Suspension for injection",
    "Eye drops, solution",
    "Transdermal patch",
    "Cream",
    "Solution for injection/infusion",
    "Capsule, soft",
    "Herbal tea",
    "Prolonged-release capsule, hard",
    "Oral solution",
    "Ointment"
]

counts = [
    15730, 9577, 9021, 6269, 2668, 2436, 1701, 1115, 1068, 865,
    843, 592, 563, 542, 521, 497, 452, 444, 413, 407
]

# Coverage: 1 = gemappt, 0 = nicht gemappt
coverage = [
    1, 0, 1, 1, 0, 0, 1, 0, 0, 0,
    1, 0, 1, 0, 0, 0, 0, 0, 1, 1
]

# Farben: grün = gemappt, rot = nicht gemappt
colors = ["green" if c == 1 else "red" for c in coverage]

# Erstelle den Plot
plt.figure(figsize=(14, 7))
bars = plt.bar(dose_forms, counts, color=colors)

# Füge die absoluten Zahlen über jedem Balken hinzu
for i, (bar, count) in enumerate(zip(bars, counts)):
    height = bar.get_height()
    plt.text(
        bar.get_x() + bar.get_width() / 2., height,
        f'{count:,}',  # Formatierung mit Tausendertrennzeichen
        ha='center', va='bottom',
        fontsize=9,
        fontweight='bold'
    )

plt.xticks(rotation=75, ha="right")
plt.ylabel("Number of drugs", fontsize=12)
plt.title("Most Frequent EDQM Dose Forms by Number of Drugs and RxNorm Mapping Availability", fontsize=14, fontweight='bold')
plt.grid(axis='y', alpha=0.3, linestyle='--')

# Füge Legende hinzu
from matplotlib.patches import Patch
legend_elements = [
    Patch(facecolor='green', label='Mapped to RxNorm'),
    Patch(facecolor='red', label='Not mapped')
]
plt.legend(handles=legend_elements, loc='upper right')

plt.tight_layout()

# Speichere den Plot
output_file = 'edqm_dose_form_coverage.png'
plt.savefig(output_file, dpi=300, bbox_inches='tight')
print(f"Plot gespeichert als: {output_file}")

# Zeige den Plot an
plt.show()

