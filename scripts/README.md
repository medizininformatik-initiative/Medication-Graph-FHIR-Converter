# Scripts Directory

This directory contains all analysis, export, and utility scripts organized by purpose.

## Directory Structure

- **`analysis/`** - Analysis scripts for SCD matching, drug analysis, and substance coverage
  - `analyze_scd_matching_failures.sh` - Analyzes SCD matching failures
  - `analyze_drugs_with_rxcuis.sh` - Analyzes drugs with RxCUIs
  - `analyze_top_substances.sh` - Analyzes top 20 substances SCD coverage
  - `analyze_dose_form_mapping.sh` - Analyzes EDQM dose form mapping

- **`export/`** - Export scripts
  - `run_fhir_export.sh` - Runs FHIR export with SCD matching

- **`populate/`** - Data population scripts
  - `populate_with_darreichungsformen.sh` - Populates Neo4j with our dose form mapping
  - `populate_with_paper_mapping.sh` - Populates Neo4j with literature-based dose form mapping

- **`performance/`** - Performance testing scripts
  - `performance_test_scd_matching.sh` - Performance test for SCD matching

- **`plots/`** - Python visualization scripts
  - `visualize_scd_matching_failures.py` - Visualizes SCD matching failures
  - `visualize_top_substances.py` - Visualizes top substances coverage

- **`tools/`** - Utility Python scripts
  - `count_doseform_review.py` - Counts "ok" entries and mappings in review CSV done by Editha Räuscher
  - `convert_excel_to_csv.py` - Converts EDQM-RX.xlsx to CSV format
  - `rxnav_fetch_dose_form.py` - Fetches dose form data from RxNav API

## Output Files

All generated JSON files are stored in:
- **`../output/analysis/`** - Analysis results (SCD matching statistics, drug analysis, etc.)
- **`../output/performance/`** - Performance test results

These directories are automatically created if they don't exist and are ignored by Git (see `.gitignore`).

## Usage

All scripts should be run from the project root directory. The scripts automatically navigate to the project root.

Example:
```bash
./scripts/analysis/analyze_scd_matching_failures.sh
```
