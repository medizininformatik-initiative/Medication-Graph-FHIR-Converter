import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    jvm("desktop")
    
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


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "de.medizininformatikinitiative.medgraph"
            packageVersion = "1.0.0"
        }
    }
}

tasks.named<Test>("desktopTest") {
    useJUnitPlatform()
}