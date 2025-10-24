plugins {
    id("java")
    kotlin("jvm")
}

group = "de.medizininformatikinitiative.medgraph"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {

    val hapiVersion = "8.0.0"
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:${hapiVersion}")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:${hapiVersion}")

    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.neo4j.driver:neo4j-java-driver:6.0.0")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("com.opencsv:opencsv:5.9") // CSV Library
    implementation("commons-cli:commons-cli:1.3.1") // Command Line Parser

    testImplementation(project(":testFixtures")) // Reference to test fixtures
    testImplementation(platform("org.junit:junit-bom:5.10.0")) // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter") // JUnit 5
    testImplementation("org.mockito:mockito-core:5.12.0") // Mocking
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}