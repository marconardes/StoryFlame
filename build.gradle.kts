import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

plugins {
    base
    id("com.android.application") version "8.5.2" apply false
}

group = "io.storyflame"
version = "0.1.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version

    pluginManager.withPlugin("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

tasks.register("buildWeek1") {
    group = "build"
    description = "Builds the StoryFlame Week 1 deliverables."
    dependsOn(":core:build")
    dependsOn(":desktop:build")
    dependsOn(":android:assembleDebug")
}
