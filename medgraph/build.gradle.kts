plugins {
    id("java")
}

group = "de.medizininformatikinitiative"
version = "1.0-alpha01"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.neo4j.driver:neo4j-java-driver:5.11.0")
    implementation("org.apache.commons:commons-text:1.12.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.12.0")
}

tasks.test {
    useJUnitPlatform()
}