# Setup-Anleitung

## Erste Schritte (für neue Entwickler)

Wenn Sie dieses Repository zum ersten Mal klonen:

1. **Repository klonen:**
   ```bash
   git clone https://github.com/your-username/your-repo.git
   cd your-repo
   ```

2. **Umgebungsvariablen konfigurieren:**
   ```bash
   # Kopiere die Beispiel-Datei
   cp .env.example .env
   
   # Bearbeite .env und trage DEINE eigenen Neo4j-Credentials ein
   nano .env  # oder ein anderer Editor
   ```

3. **Wichtig:** Jede Person muss ihre **eigenen** Neo4j-Credentials verwenden. Das Passwort ist **nicht** im Repository enthalten und wird auch nicht automatisch geteilt.

## Datenbank-Konfiguration

Dieses Projekt benötigt eine Neo4j-Datenbankverbindung. **Aus Sicherheitsgründen sollten Passwörter niemals im Code gespeichert werden.**

**Jede Person verwendet ihre eigenen Credentials** - das Passwort wird nicht im Repository gespeichert!

### Umgebungsvariablen verwenden

Die empfohlene Methode ist die Verwendung von Umgebungsvariablen:

```bash
export NEO4J_URI=bolt://localhost:7687
export NEO4J_USER=neo4j
export NEO4J_PASSWORD=dein_passwort_hier
```

### .env Datei (optional)

Alternativ können Sie eine `.env` Datei im Projekt-Root erstellen:

```bash
# Kopiere die Beispiel-Datei
cp .env.example .env

# Bearbeite .env und füge deine Credentials ein
nano .env  # oder verwende einen anderen Editor
```

**WICHTIG:** Die `.env` Datei ist bereits in `.gitignore` eingetragen und wird **nicht** zu Git hinzugefügt.

### Shell-Skripte verwenden

Die Shell-Skripte prüfen automatisch Umgebungsvariablen. Falls keine gesetzt sind, können Sie die Parameter auch direkt übergeben:

```bash
./scripts/analysis/analyze_scd_matching_failures.sh "bolt://localhost:7687" "neo4j" "dein_passwort" "suffix"
```

### Java-Tools verwenden

Java-Tools prüfen zuerst Umgebungsvariablen, dann Kommandozeilen-Argumente:

```bash
# Mit Umgebungsvariablen
export NEO4J_PASSWORD=dein_passwort
./gradlew :medgraph:runAnalyzeScdMatchingFailures

# Oder mit Argumenten
./gradlew :medgraph:runAnalyzeScdMatchingFailures --args="bolt://localhost:7687 neo4j dein_passwort output.json"
```

## RxNorm Datenbank (optional)

Falls Sie eine lokale RxNorm SQLite-Datenbank verwenden:

```bash
export RXNORM_DB_PATH=/pfad/zur/rxnorm.db
```

## Sicherheitshinweise

- **NIEMALS** Passwörter in Git committen
- **Jede Person verwendet ihre eigenen Credentials** - teilen Sie Passwörter nicht über Git
- Die `.env` Datei ist in `.gitignore` und wird **nicht** zu Git hinzugefügt
- Verwenden Sie `.env` Dateien nur lokal (nicht in Git)
- Für Produktionsumgebungen: Verwenden Sie sichere Secrets-Management-Tools
- Die `.env.example` Datei enthält nur Platzhalter - niemals echte Passwörter dort eintragen

## Häufige Fragen

**Q: Bekomme ich automatisch das Passwort, wenn ich den Code pullen?**  
A: Nein! Das Passwort ist nicht im Repository. Jede Person muss ihre eigenen Neo4j-Credentials verwenden.

**Q: Woher bekomme ich die Neo4j-Credentials?**  
A: Sie müssen eine eigene Neo4j-Instanz einrichten oder die Credentials von Ihrem Team/Projektleiter erhalten (außerhalb von Git, z.B. über sichere Kanäle).

**Q: Was passiert, wenn ich keine .env Datei habe?**  
A: Die Skripte werden mit einer Fehlermeldung abbrechen und Sie auffordern, `NEO4J_PASSWORD` zu setzen.
