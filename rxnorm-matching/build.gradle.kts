plugins {
    id("java")
}

group = "de.medizininformatikinitiative.medgraph"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":medgraph"))
    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}