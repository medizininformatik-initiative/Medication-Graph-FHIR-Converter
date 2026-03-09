plugins {
    id("java")
}

group = "de.medizininformatikinitiative.medgraph"
version = "1-2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":medgraph"))
    implementation("org.neo4j.driver:neo4j-java-driver:5.19.0")
    implementation("org.jetbrains:annotations:26.0.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}