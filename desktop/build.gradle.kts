plugins {
    application
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-swing:3.17.1")
}

application {
    mainClass.set("io.storyflame.desktop.StoryFlameDesktopApp")
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dsun.jnu.encoding=UTF-8"
    )
}
