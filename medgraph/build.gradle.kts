plugins {
    id("java")
    application
}

group = "de.medizininformatikinitiative.medgraph"
version = "1.0.2"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.neo4j.driver:neo4j-java-driver:5.19.0")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("com.opencsv:opencsv:5.9") // CSV Library
    implementation("com.google.code.gson:gson:2.10.1") // JSON Library
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1") 
    implementation("commons-cli:commons-cli:1.3.1") // Command Line Parser
    implementation("org.xerial:sqlite-jdbc:3.46.1.3") // SQLite JDBC for local RxNorm dump

    testImplementation(project(":testFixtures")) // Reference to test fixtures
    testImplementation(platform("org.junit:junit-bom:5.10.0")) // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter") // JUnit 5
    testImplementation("org.mockito:mockito-core:5.12.0") // Mocking
     testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    // CLI entrypoint that dispatches to utilities like "export" or "populate"
    mainClass.set("de.medizininformatikinitiative.medgraph.commandline.MedgraphCliLauncher")
}

// Configure run task to pass system properties
tasks.named<JavaExec>("run") {
    // Pass rxnorm.db.path from gradle property or environment variable
    val dbPath = project.findProperty("rxnorm.db.path") as String?
        ?: System.getenv("RXNORM_DB_PATH")
        ?: "/Users/lucy/Documents/Universität/Informatik/7.Semester/Bachlorthesis/Medication-Graph-FHIR-Converter-1/data/rxnorm/rxnorm.db"
    
    systemProperty("rxnorm.db.path", dbPath)
    
    // Pass product limit from gradle property or environment variable (only if explicitly set)
    val productLimit = project.findProperty("medgraph.export.productLimit") as String?
        ?: System.getenv("MEDGRAPH_EXPORT_PRODUCT_LIMIT")
    
    // Only set the property if it was explicitly provided (don't set a default)
    if (productLimit != null) {
        systemProperty("medgraph.export.productLimit", productLimit)
    }
}

// Dedicated run task for the QuickDoseFormMappingCheck CLI
tasks.register<JavaExec>("runQuickDoseFormCheck") {
    group = "application"
    description = "Runs the QuickDoseFormMappingCheck CLI"
    mainClass.set("de.medizininformatikinitiative.medgraph.commandline.QuickDoseFormMappingCheck")
    classpath = sourceSets["main"].runtimeClasspath
    // Allow passing args via --args="..."
    standardInput = System.`in`
}

// Dedicated run task for the AnalyzeDoseFormMapping tool
tasks.register<JavaExec>("runAnalyzeDoseFormMapping") {
    group = "application"
    description = "Runs the AnalyzeDoseFormMapping tool"
    mainClass.set("de.medizininformatikinitiative.medgraph.tools.AnalyzeDoseFormMapping")
    classpath = sourceSets["main"].runtimeClasspath
    // Pass rxnorm.db.path system property
    val dbPath = project.findProperty("rxnorm.db.path") as String?
        ?: System.getenv("RXNORM_DB_PATH")
        ?: "/Users/lucy/Documents/Universität/Informatik/7.Semester/Bachlorthesis/Medication-Graph-FHIR-Converter-1/data/rxnorm/rxnorm.db"
    systemProperty("rxnorm.db.path", dbPath)
}

// Dedicated run task for the AnalyzeScdMatchingFailures tool
tasks.register<JavaExec>("runAnalyzeScdMatchingFailures") {
    group = "application"
    description = "Runs the AnalyzeScdMatchingFailures tool"
    mainClass.set("de.medizininformatikinitiative.medgraph.tools.AnalyzeScdMatchingFailures")
    classpath = sourceSets["main"].runtimeClasspath
    // Pass rxnorm.db.path system property
    val dbPath = project.findProperty("rxnorm.db.path") as String?
        ?: System.getenv("RXNORM_DB_PATH")
        ?: "/Users/lucy/Documents/Universität/Informatik/7.Semester/Bachlorthesis/Medication-Graph-FHIR-Converter-1/data/rxnorm/rxnorm.db"
    systemProperty("rxnorm.db.path", dbPath)
}

tasks.register<JavaExec>("runAnalyzeDrugsWithRxCUIs") {
    group = "application"
    description = "Runs the AnalyzeDrugsWithRxCUIs CLI tool"
    mainClass.set("de.medizininformatikinitiative.medgraph.tools.AnalyzeDrugsWithRxCUIs")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    args = project.properties["args"]?.toString()?.split(" ") ?: listOf()
    // Pass rxnorm.db.path system property
    val dbPath = project.findProperty("rxnorm.db.path") as String?
        ?: System.getenv("RXNORM_DB_PATH")
        ?: "/Users/lucy/Documents/Universität/Informatik/7.Semester/Bachlorthesis/Medication-Graph-FHIR-Converter-1/data/rxnorm/rxnorm.db"
    systemProperty("rxnorm.db.path", dbPath)
}

tasks.register<JavaExec>("runPerformanceTestSCDMatching") {
    group = "application"
    description = "Runs the PerformanceTestSCDMatching tool"
    mainClass.set("de.medizininformatikinitiative.medgraph.tools.PerformanceTestSCDMatching")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    args = project.properties["args"]?.toString()?.split(" ") ?: listOf()
    // Pass rxnorm.db.path system property
    val dbPath = project.findProperty("rxnorm.db.path") as String?
        ?: System.getenv("RXNORM_DB_PATH")
        ?: "/Users/lucy/Documents/Universität/Informatik/7.Semester/Bachlorthesis/Medication-Graph-FHIR-Converter-1/data/rxnorm/rxnorm.db"
    systemProperty("rxnorm.db.path", dbPath)
}