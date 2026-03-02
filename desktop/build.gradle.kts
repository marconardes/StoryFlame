plugins {
    application
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass.set("io.storyflame.desktop.StoryFlameDesktopApp")
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dsun.jnu.encoding=UTF-8"
    )
}
