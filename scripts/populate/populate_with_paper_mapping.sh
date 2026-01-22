#!/bin/bash

# Script zum Ausführen des Populators mit EdqmRxNormDoseFormMappingLoader
# Verwendet edqm_rxnorm_dose_form_mapping.csv (paper-basiertes Mapping)

# Wechsle zum Projekt-Root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/../.."

# WICHTIG: Stelle sicher, dass die Environment Variable NICHT gesetzt ist
# oder explizit auf false gesetzt wird, um das paper-basierte Mapping zu verwenden
unset MEDGRAPH_USE_RXNORM_DARREICHUNGSFORMEN_MAPPING

# Setze Java 21 für Gradle
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Parameter
MMI_DIR="${1:-/Users/lucy/Documents/Universität/Informatik/7.Semester/Bachlorthesis/mmiPharmindexR3_20250815MAIN}"
NEO4J_IMPORT_DIR="${2:-/Users/lucy/neo4j/import}"
DB_URI="${3:-bolt://localhost:7687}"
DB_USER="${4:-neo4j}"
DB_PASSWORD="${5:-${NEO4J_PASSWORD:-}}"

# Prüfe, ob Passwort gesetzt ist
if [ -z "$DB_PASSWORD" ]; then
    echo "FEHLER: Kein Neo4j-Passwort angegeben!"
    echo "Bitte setze die Umgebungsvariable NEO4J_PASSWORD oder übergebe das Passwort als fünftes Argument."
    exit 1
fi

echo "=== Populator mit EdqmRxNormDoseFormMappingLoader (Paper-basiert) ==="
echo "  MMI-Verzeichnis: $MMI_DIR"
echo "  Neo4j Import-Verzeichnis: $NEO4J_IMPORT_DIR"
echo "  Datenbank URI: $DB_URI"
echo "  Benutzer: $DB_USER"
echo "  Mapping-Tabelle: edqm_rxnorm_dose_form_mapping.csv (paper-basiert)"
echo "  Log-Datei: ~/Desktop/populator_output.txt"
echo ""

# Führe den Populator aus

/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home/bin/java \
  -jar composeApp/build/libs/medgraph-desktop-all.jar \
  -r "$DB_URI" -u "$DB_USER" -p "$DB_PASSWORD" \
  populate "$MMI_DIR" "$NEO4J_IMPORT_DIR" \
  > ~/Desktop/populator_output.txt 2>&1

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✓ Populator erfolgreich abgeschlossen!"
    echo "  Log-Datei: ~/Desktop/populator_output.txt"
else
    echo ""
    echo "✗ Populator fehlgeschlagen (Exit-Code: $EXIT_CODE)"
    echo "  Bitte Log-Datei prüfen: ~/Desktop/populator_output.txt"
    exit $EXIT_CODE
fi

