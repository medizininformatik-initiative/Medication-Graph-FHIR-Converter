plugins {
    id("java")
}

group = "de.medizininformatikinitiative.medgraph"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":medgraph"))
    implementation("org.jetbrains:annotations:24.0.0")
    implementation(platform("org.junit:junit-bom:5.10.0"))
    implementation("org.junit.jupiter:junit-jupiter")
    implementation("org.mockito:mockito-core:5.12.0") // Mocking
    implementation("org.neo4j.test:neo4j-harness:5.19.0") // Neo4j local instance to test classes accessing the DB
}

tasks.test {
    useJUnitPlatform()
}