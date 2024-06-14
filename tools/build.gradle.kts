plugins {
    id("java")
}

group = "de.medizininformatikinitiative"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":medgraph"))
    implementation("org.neo4j.driver:neo4j-java-driver:5.19.0") // Neo4j driver
    implementation("com.opencsv:opencsv:5.9") // CSV Library
    implementation("com.google.code.gson:gson:2.10.1") // JSON Library

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.12.0") // Mocking
}

tasks.test {
    useJUnitPlatform()
}