#!/bin/bash

# Script zum Ausführen des FHIR-Exports mit Matching-Algorithmus
# 
# Verwendung:
#   ./run_fhir_export.sh [output_dir] [database-uri] [database-user] [database-password] [limit-products] [log-file]
#
# Parameter:
#   output_dir        - Verzeichnis für den FHIR-Export (Standard: /tmp/fhir-out1)
#   database-uri      - Neo4j Datenbank URI (optional, verwendet Standard-Konfiguration wenn nicht angegeben)
#   database-user     - Neo4j Benutzername (optional, verwendet Standard-Konfiguration wenn nicht angegeben)
#   database-password - Neo4j Passwort (optional, verwendet Standard-Konfiguration wenn nicht angegeben)
#   limit-products    - Maximale Anzahl zu exportierender Produkte (optional, wenn nicht angegeben: ALLE Produkte)
#   log-file          - Name der Log-Datei (optional, Standard: ./logs/match.txt)
#
# Beispiele:
#   ./run_fhir_export.sh                                    # Alle Produkte, Standard-Datenbankverbindung
#   ./run_fhir_export.sh /tmp/fhir-out1                    # Alle Produkte, Standard-Datenbankverbindung
#   ./run_fhir_export.sh /tmp/fhir-out1 "" "" "" 100       # Nur 100 Produkte
#   ./run_fhir_export.sh /tmp/fhir-out1 bolt://localhost:7687 neo4j "passwort"  # Alle Produkte mit expliziter DB-Verbindung
#   ./run_fhir_export.sh /tmp/fhir-out1 "" "" "" 100 ~/Desktop/test.txt  # Mit benutzerdefinierter Log-Datei

# Wechsle zum Projekt-Root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/../.."

# Setze Java 21 für Gradle (der Code wurde mit Java 21 kompiliert)
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Parameter mit Standardwerten
OUTPUT_DIR="${1:-/tmp/fhir-out1}"
DB_URI="${2:-}"
DB_USER="${3:-}"
DB_PASSWORD="${4:-}"
LIMIT_PRODUCTS="${5:-}"
LOG_FILE_PARAM="${6:-}"

# Standard Output-Datei für Logs
if [ -n "$LOG_FILE_PARAM" ]; then
    LOG_FILE="$LOG_FILE_PARAM"
else
    LOG_FILE="./logs/match.txt"
fi

echo "Starte FHIR-Export mit Matching-Algorithmus..."
echo "  Output-Verzeichnis: $OUTPUT_DIR"
if [ -n "$DB_URI" ] && [ -n "$DB_USER" ] && [ -n "$DB_PASSWORD" ]; then
    echo "  Datenbank URI: $DB_URI"
    echo "  Benutzer: $DB_USER"
    echo "  Verwende angegebene Datenbankverbindung"
elif [ -n "$DB_URI" ] || [ -n "$DB_USER" ] || [ -n "$DB_PASSWORD" ]; then
    echo "FEHLER: Wenn Datenbankparameter angegeben werden, müssen alle drei angegeben werden (URI, User, Password)"
    echo "Verwendung: ./run_fhir_export.sh [output_dir] [database-uri] [database-user] [database-password] [limit-products]"
    exit 1
else
    echo "  Verwende Standard-Datenbankverbindung (aus Neo4j Desktop Konfiguration)"
fi
if [ -n "$LIMIT_PRODUCTS" ]; then
    echo "  Produkt-Limit: $LIMIT_PRODUCTS"
else
    echo "  Produkt-Limit: KEIN LIMIT (alle Produkte)"
fi
echo "  Log-Datei: $LOG_FILE"
echo ""

# Baue den Gradle-Befehl zusammen
GRADLE_CMD="./gradlew :medgraph:run --args=\"export \\\"$OUTPUT_DIR\\\""

# Füge Datenbankverbindungsparameter hinzu, falls alle drei angegeben wurden
if [ -n "$DB_URI" ] && [ -n "$DB_USER" ] && [ -n "$DB_PASSWORD" ]; then
    GRADLE_CMD="$GRADLE_CMD -r $DB_URI -u $DB_USER -p $DB_PASSWORD"
fi

# Füge Produkt-Limit nur hinzu, wenn angegeben
if [ -n "$LIMIT_PRODUCTS" ]; then
    GRADLE_CMD="$GRADLE_CMD -lp $LIMIT_PRODUCTS"
fi

GRADLE_CMD="$GRADLE_CMD\""

# Führe den Export aus und leite Ausgabe in Log-Datei um
echo "Führe Export aus..."
mkdir -p "$(dirname "$LOG_FILE")"
eval $GRADLE_CMD > "$LOG_FILE" 2>&1

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✓ Export erfolgreich abgeschlossen!"
    echo "  Ergebnisse: $OUTPUT_DIR"
    echo "  Log-Datei: $LOG_FILE"
else
    echo ""
    echo "✗ Export fehlgeschlagen (Exit-Code: $EXIT_CODE)"
    echo "  Bitte Log-Datei prüfen: $LOG_FILE"
    exit $EXIT_CODE
fi

