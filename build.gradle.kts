import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
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
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("file.encoding", "UTF-8")
        systemProperty("sun.jnu.encoding", "UTF-8")
    }
}

tasks.register("buildWeek1") {
    group = "build"
    description = "Builds the StoryFlame Week 1 deliverables."
    dependsOn(":core:build")
    dependsOn(":app:build")
    dependsOn(":desktop:build")
    dependsOn(":android:assembleDebug")
}
