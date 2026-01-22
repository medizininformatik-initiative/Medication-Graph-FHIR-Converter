#!/usr/bin/env python3

import matplotlib.pyplot as plt
from matplotlib.patches import Patch

# Top 20 EDQM Dose Forms (from ./analyze_dose_form_mapping.sh)
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
    "Ointment",

]

counts = [
    15730, 9577, 9021, 6269, 2668, 2436, 1701, 1115, 1068, 865,
    843, 592, 563, 542, 521, 497, 452, 444, 413, 407
]

# Color mapping: Green: #4CAF50, Blue: #2196F3, Red: #F44336
colors = [
    "#4CAF50",  # Bar 1 - green
    "#2196F3",  # Bar 2 - blue
    "#4CAF50",  # Bar 3 - green
    "#4CAF50",  # Bar 4 - green
    "#2196F3",  # Bar 5 - blue
    "#2196F3",  # Bar 6 - blue
    "#4CAF50",  # Bar 7 - green
    "#2196F3",  # Bar 8 - blue
    "#2196F3",  # Bar 9 - blue
    "#2196F3",  # Bar 10 - blue
    "#4CAF50",  # Bar 11 - green
    "#2196F3",  # Bar 12 - blue
    "#4CAF50",  # Bar 13 - green
    "#2196F3",  # Bar 14 - blue
    "#2196F3",  # Bar 15 - blue
    "#2196F3",  # Bar 16 - blue
    "#F44336",  # Bar 17 - red
    "#2196F3",  # Bar 18 - blue
    "#4CAF50",  # Bar 19 - green
    "#4CAF50"   # Bar 20 - green
]

# Create plot
plt.figure(figsize=(18, 10))
bars = plt.bar(dose_forms, counts, color=colors)

# Add absolute numbers above bars
for bar, count in zip(bars, counts):
    height = bar.get_height()
    plt.text(
        bar.get_x() + bar.get_width() / 2,
        height,
        f"{count:,}",
        ha="center",
        va="bottom",
        fontsize=12,
        fontweight="bold"
    )

plt.xticks(rotation=75, ha="right", fontsize=12)
plt.yticks(fontsize=12)
plt.ylabel("Number of drugs", fontsize=16, fontweight="bold", labelpad=15)
plt.xlabel("Dose Form", fontsize=16, fontweight="bold")

plt.grid(axis="y", alpha=0.3, linestyle="--")

# Legend
# Green: both mapping approaches have a dose form mapping
# Blue: only the proposed mapping approach could find a mapping
# Red: neither of the two approaches could find a mapping
legend_elements = [
    Patch(facecolor="#4CAF50", label="Mapped (both approaches)"),
    Patch(facecolor="#2196F3", label="Mapped (proposed only)"),
    Patch(facecolor="#F44336", label="Not mapped")
]
plt.legend(handles=legend_elements, loc="upper right", fontsize=14)

plt.tight_layout(rect=[0.05, 0.4, 1, 1], pad=1.5)
plt.subplots_adjust(bottom=0.45)

# Save figure
output_file = "edqm_dose_form_coverage_proposed.png"
plt.savefig(output_file, dpi=300, bbox_inches="tight")
print(f"Plot saved as: {output_file}")

plt.show()