# SCD Matching Algorithm - Guide

The SCD Matching Algorithm is based on the original implementation by Markus Budeus, which we extended and adapted for our use case. This guide explains how to run the SCD Matching Algorithm. 

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Clone Repository and Setup](#clone-repository-and-setup)
3. [Database Configuration](#database-configuration)
4. [Prepare Database](#prepare-database)
5. [Run SCD Algorithm](#run-scd-algorithm)
6. [Use Analysis Tools](#use-analysis-tools)

---

## Prerequisites

Before you begin, make sure you have the following installed:

- **Java 21** (JDK)
  
- **Neo4j Database** (local or remote)
  - Download: [Neo4j Desktop](https://neo4j.com/download/) or [Neo4j Community Edition](https://neo4j.com/deployment-center/)
  - The database must be running and have access to MMI Pharmindex data
  
- **RxNorm SQLite Database**
  - Used for local RxNorm queries
  - Path: `data/rxnorm/rxnorm.db`
  
- **Gradle** 
- **Python 3** (for visualizations)

---

## Clone Repository and Setup

### 1. Clone Repository and Checkout Branch

```bash
git clone https://github.com/medizininformatik-initiative/Medication-Graph-FHIR-Converter.git
git checkout lucy-thesis
```

### 2. Build the Project

Before running the populate scripts, you need to build the JAR file:

```bash
./gradlew :composeApp:desktopShadowJar
```

This creates the `composeApp/build/libs/medgraph-desktop-all.jar` file that is required by the populate scripts.

**Note:** The export script (`run_fhir_export.sh`) uses Gradle directly and will build automatically if needed, so you don't need to rebuild before running it.

### 3. Understand Project Structure

```
.
├── medgraph/                    # Main project -> SCD implementation (Java)
├── scripts/                     # Shell scripts
│   ├── analysis/               # Analysis scripts
│   ├── export/                 # Export scripts (SCD Matching)
│   ├── populate/               # Database population
│   └── plots/                  # Visualizations
├── output/                     # Generated output files
│   ├── analysis/               # Analysis results (JSON)
│   └── performance/            # Performance tests
└── data/                       # Data (RxNorm DB, etc.)
```

### 4. Check Java Version

```bash
# Check your Java version
java -version

# Should show Java 21. If not, set JAVA_HOME:
export JAVA_HOME=/path/to/java21
```

---

## Database Configuration

### 1. Set Environment Variables

Create a `.env` file in the project root:

```bash
touch .env
nano .env  # or another editor
```

**Minimal contents of `.env` file:**

```bash
NEO4J_URI=bolt://localhost:7687
NEO4J_USER=neo4j
NEO4J_PASSWORD='your_password_here' 
```

**Important:** 
- The `.env` file is in `.gitignore`
- Each person uses their own credentials!
- For passwords with special characters (e.g., parentheses): Use single quotes

### 2. Load Environment Variables

```bash
# In the current shell session:
export $(grep -v '^#' .env | xargs)

# Or manually:
export NEO4J_URI=bolt://localhost:7687
export NEO4J_USER=neo4j
export NEO4J_PASSWORD='your_password'
```

### 3. RxNorm Database (optional)

If you use a local RxNorm SQLite database:

```bash
export RXNORM_DB_PATH=/absolute/path/to/rxnorm.db
```

---

## Prepare Database

Before running the SCD algorithm, the Neo4j database must be populated with the necessary data:

### 1. Build JAR File (if not already done)

The populate scripts require the JAR file. If you haven't built it yet:

```bash
./gradlew :composeApp:desktopShadowJar
```

### 2. Insert Dose Form Mappings

The SCD algorithm requires Dose Form Mappings (EDQM → RxNorm). You have two options (it takes few minutes):

**Option A: Proposed Mapping (recommended)**
```bash
./scripts/populate/populate_with_darreichungsformen.sh
```

**Option B: Literature/Paper-based Mapping**
```bash
./scripts/populate/populate_with_paper_mapping.sh
```

### 3. Test Database Connection (optional)

```bash
# Test the connection (optional)
neo4j-admin dbms info  # If Neo4j is installed locally
```

---

## Run SCD Algorithm

### Main Export with SCD Matching

The SCD algorithm is automatically executed during the FHIR export (this will also take few minutes):

```bash
./scripts/export/run_fhir_export.sh
```

**With Parameters:**

```bash
./scripts/export/run_fhir_export.sh \
  "output/fhir" \                    # Output directory
  "bolt://localhost:7687" \          # Neo4j URI (optional)
  "neo4j" \                          # Neo4j User (optional)
  "your_password" \                  # Neo4j Password (optional)
  1000 \                              # Limit: number of products (optional)
  "logs/fhir_export.log"             # Log file (optional, default: ./logs/match.txt)
```

**With Environment Variables (recommended):**

```bash
# Environment variables are already set (from .env)
./scripts/export/run_fhir_export.sh
```

### What Happens During Export?

1. **Loads products** from Neo4j
2. **Extracts drugs** from each product
3. **Performs SCD matching** for each drug:
   - Extracts active ingredients with RxCUIs
   - Normalizes strengths (UCUM)
   - Maps dose forms (EDQM → RxNorm)
   - Searches for SCD candidates in RxNorm
   - Validates and scores candidates
   - Selects best match
4. **Creates FHIR Medication Resources** with RxNorm references
5. **Saves results** in the output directory

---

## Use Analysis Tools

After running the SCD algorithm, you can perform various analyses:

### 1. SCD Matching Failure Analysis

Analyzes why drugs could not be matched:

```bash
./scripts/analysis/analyze_scd_matching_failures.sh "" "" "" "paper_mapping"
```

**Results:**
- JSON file: `output/analysis/scd_matching_statistics_paper_mapping.json`
- Visualization: Automatically generated (if Python script is available)

### 2. Top 20 Substances Coverage Analysis

Analyzes SCD coverage for the 20 most frequent substances:

```bash
./scripts/analysis/analyze_top_substances.sh "" "" "" "paper_mapping"
```

**Results:**
- JSON file: `output/analysis/top_substances_analysis_paper_mapping.json`
- Visualization: Automatically generated (comparison Paper vs. Proposed Mapping)

### 3. Drugs with RxCUIs Analysis

Analyzes how many drugs have RxCUIs:

```bash
./scripts/analysis/analyze_drugs_with_rxcuis.sh "" "" "" "paper_mapping"
```

**Results:**
- JSON file: `output/analysis/drugs_with_rxcuis_analysis_paper_mapping.json`

### 4. Dose Form Mapping Analysis

Analyzes the coverage of EDQM → RxNorm dose form mappings:

```bash
./scripts/analysis/analyze_dose_form_mapping.sh "" "" "" "paper_mapping"
```

**Results:**
- JSON file: `output/analysis/dose_form_mapping_analysis_paper_mapping.json`

### 5. Performance Test

Tests the performance of the SCD matching algorithm:

```bash
./scripts/performance/performance_test_scd_matching.sh "" "" "" 10 "paper_mapping"
```

**Results:**
- JSON file: `output/performance/scd_matching_performance_paper_mapping.json`

---

## Comparison: Paper Mapping vs. Proposed Mapping

To compare both mapping approaches:

### 1. Run Paper Mapping

```bash
# Insert dose form mapping
./scripts/populate/populate_with_paper_mapping.sh

# Run analyses
./scripts/analysis/analyze_scd_matching_failures.sh "" "" "" "paper_mapping"
./scripts/analysis/analyze_top_substances.sh "" "" "" "paper_mapping"
```

### 2. Run Proposed Mapping

```bash
# Insert dose form mapping
./scripts/populate/populate_with_darreichungsformen.sh

# Run analyses
./scripts/analysis/analyze_scd_matching_failures.sh "" "" "" "proposed_mapping"
./scripts/analysis/analyze_top_substances.sh "" "" "" "proposed_mapping"
```

### 3. Compare Visualizations

```bash
cd scripts/plots
source venv/bin/activate  # If venv exists
python3 visualize_top_substances.py \
  ../../output/analysis/top_substances_analysis_paper_mapping.json \
  ../../output/analysis/top_substances_analysis_proposed_mapping.json
```

---

## Additional Resources

- **scripts/README.md** - Overview of all available scripts
- **Wiki** - [Project Wiki](https://github.com/medizininformatik-initiative/Medication-Graph-FHIR-Converter/wiki)

---

## Example Workflow (Complete)

```bash
# 1. Clone repository and checkout branch
git clone https://github.com/your-username/your-repo.git
cd your-repo
git checkout lucy-thesis

# 2. Build the JAR file (required for populate scripts)
./gradlew :composeApp:desktopShadowJar

# 3. Configure environment variables
touch .env
nano .env  # Enter your credentials
export $(grep -v '^#' .env | xargs)

# 4. Prepare database (insert dose form mappings)
./scripts/populate/populate_with_darreichungsformen.sh

# 5. Run SCD algorithm (FHIR Export)
# Note: This uses Gradle directly and will build automatically if needed
./scripts/export/run_fhir_export.sh

# 6. Run analyses
./scripts/analysis/analyze_scd_matching_failures.sh "" "" "" "proposed_mapping"
./scripts/analysis/analyze_top_substances.sh "" "" "" "proposed_mapping"

# 7. View results
ls -lh output/analysis/
```

---

**Happy Matching:)** 🚀


