#!/usr/bin/env python3
"""
Utility script to fetch dose form information from RxNav API.
"""

import requests
from pathlib import Path
import os
import sys

# Navigate to project root (assuming script is in scripts/tools/)
script_dir = Path(__file__).parent
project_root = script_dir.parent.parent
os.chdir(project_root)

resp = requests.get("https://rxnav.nlm.nih.gov/REST/rxcui/171212/properties?format=json").json()
dose_forms = [p["propValue"].lower() for p in resp["propConceptGroup"]["propConcept"]]
print(dose_forms[:20])


