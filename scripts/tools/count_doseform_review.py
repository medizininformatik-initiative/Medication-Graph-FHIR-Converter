#!/usr/bin/env python3
"""
Utility script to count "ok" entries and mappings in the dose form review CSV.
"""

import csv
from pathlib import Path
import os
import sys

# Navigate to project root (assuming script is in scripts/tools/)
script_dir = Path(__file__).parent
project_root = script_dir.parent.parent
os.chdir(project_root)

# Path to the review CSV file
csv_path = Path("Reviews_Editha_LucyBA - Review2_DoseFormMapping.csv")

ok_count = 0
mapping_count = 0

with csv_path.open(newline="", encoding="utf-8") as f:
    reader = csv.reader(f)
    header = next(reader)  # erste Zeile mit Spaltennamen

    # Spaltenindizes ermitteln (robust, falls sich etwas verschiebt)
    edqm_col = header.index("EDQM")
    rxnorm_col = header.index("RxNorm Mapping")
    bewertung_col = header.index("Bewertung")

    for row in reader:
        if not row or len(row) <= max(rxnorm_col, bewertung_col):
            continue

        rxnorm_mapping = row[rxnorm_col].strip()
        bewertung = row[bewertung_col].strip().lower()

        # Zähle Mappings (jede Zeile mit einem RxNorm-Eintrag)
        if rxnorm_mapping:
            mapping_count += 1

        # Zähle "ok"-Einträge
        if bewertung == "ok":
            ok_count += 1

print(f"Anzahl Mappings (EDQM → RxNorm): {mapping_count}")
print(f"Anzahl 'ok'-Bewertungen:         {ok_count}")