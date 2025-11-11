import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "21"
        }
        registerShadowJar()
    }
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(project(":medgraph"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Neo4j driver
            implementation("org.neo4j.driver:neo4j-java-driver:5.19.0")

            // Logging
            implementation("org.apache.logging.log4j:log4j-api:2.17.2")
            implementation("org.apache.logging.log4j:log4j-core:2.17.2")

            // Voyager - Navigation Framework
            val voyagerVersion = "1.0.0"
            // Navigator
            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            // Screen Model
            implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
            // Required by Voyager
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")
        }
        commonTest.dependencies {
            val junit_version = "5.10.2"
            implementation(project(":testFixtures")) // Test fixtures
            implementation("org.junit.jupiter:junit-jupiter-api:$junit_version") // JUnit 5
            implementation("org.junit.jupiter:junit-jupiter-params:$junit_version") // JUnit 5
            implementation("org.junit.jupiter:junit-jupiter-engine:$junit_version") // JUnit 5
            implementation("org.mockito:mockito-core:5.12.0") // Mocking
//            implementation("org.mockito.kotlin:mockito-kotlin:5.3.1") // Kotlin Mocking Support
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

val desktopVersion = "1.1.0"


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "de.medizininformatikinitiative.medgraph"
            packageVersion = desktopVersion
        }
    }
}

tasks.named<Test>("desktopTest") {
    useJUnitPlatform()
}

// Runnable JAR creation ----------------------------------------------------------------------------------------------
// Not my code (altough adapated by me)! Taken from https://stackoverflow.com/a/72519054
fun KotlinJvmTarget.registerShadowJar() {
    val targetName = name
    compilations.named("main") {
        tasks {
            val shadowJar = register<ShadowJar>("${targetName}ShadowJar") {
//                group = "build"
                from(output)
                configurations = listOf(runtimeDependencyFiles)
                archiveBaseName.set("medgraph")
                archiveAppendix.set(targetName)
                archiveClassifier.set("all")
                manifest {
                    attributes["Main-Class"] = "de.medizininformatikinitiative.medgraph.ui.desktop.MainKt"
                }
                mergeServiceFiles()
            }
            getByName("${targetName}Jar") {
                finalizedBy(shadowJar)
            }
        }
    }
}

val zipTarget = "desktop"
task<Zip>("${zipTarget}ReleaseZip") {
    dependsOn("${zipTarget}ShadowJar")
    from("build/libs") {
        include("*-${zipTarget}-all.jar")
        include("README.txt")
    }
    from("..").include("LICENSE.txt")
    from("src/${zipTarget}Main/resources").include("NOTICE.txt")
    archiveBaseName.set("MII DIZ FHIR Medication Tool")
    archiveVersion.set(desktopVersion)
}