plugins {
    `java-library`
    application
}

dependencies {
    api(project(":core"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("io.storyflame.app.electron.StoryFlameElectronBridgeServer")
    applicationName = "storyflame-bridge"
}

tasks.register<JavaExec>("runElectronBridge") {
    group = "application"
    description = "Runs the local Electron bridge for StoryFlame."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(application.mainClass)
}
