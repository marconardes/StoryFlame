import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerializationPlugin)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("com.benasher44:uuid:0.8.2")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
        // commonTest already has libs.kotlin.test which should provide necessary abstractions.
        // useJUnitPlatform() below will ensure JUnit 5 is used if available.
        // val desktopTest by getting { // Explicitly defining desktopTest dependencies block
        //     dependencies {
        //         // implementation(libs.kotlin.testJunit) // Removed again to avoid conflict
        //     }
        // }
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

compose.desktop {
    application {
        mainClass = "br.com.marconardes.storyflame.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "br.com.marconardes.storyflame"
            packageVersion = "1.0.0"
        }
    }
}
