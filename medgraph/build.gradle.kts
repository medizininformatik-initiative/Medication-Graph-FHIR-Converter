plugins {
    id("java")
}

group = "de.medizininformatikinitiative"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.neo4j.driver:neo4j-java-driver:5.19.0")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("com.opencsv:opencsv:5.9") // CSV Library
    implementation("com.google.code.gson:gson:2.10.1") // JSON Library

    testImplementation(platform("org.junit:junit-bom:5.10.0")) // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter") // JUnit 5
    testImplementation("org.mockito:mockito-core:5.12.0") // Mocking
    testImplementation("org.neo4j.test:neo4j-harness:5.19.0") // Neo4j local instance to test classes accessing the DB
}

tasks.test {
    useJUnitPlatform()
}