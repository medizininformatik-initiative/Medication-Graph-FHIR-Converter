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

// Dedicated run task for the QuickDoseFormMappingCheck CLI
tasks.register<JavaExec>("runQuickDoseFormCheck") {
    group = "application"
    description = "Runs the QuickDoseFormMappingCheck CLI"
    mainClass.set("de.medizininformatikinitiative.medgraph.commandline.QuickDoseFormMappingCheck")
    classpath = sourceSets["main"].runtimeClasspath
    // Allow passing args via --args="..."
    standardInput = System.`in`
}