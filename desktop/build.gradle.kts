plugins {
    application
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass.set("io.storyflame.desktop.StoryFlameDesktopApp")
}

