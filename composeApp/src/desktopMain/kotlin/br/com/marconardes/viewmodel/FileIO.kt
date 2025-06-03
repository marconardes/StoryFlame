package br.com.marconardes.viewmodel

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

private const val FILENAME = "projects.json"
private val APP_DIR_PATH = Paths.get(System.getProperty("user.home"), ".storyflame")
private val PROJECTS_FILE_PATH = APP_DIR_PATH.resolve(FILENAME)

actual fun saveProjectsToFile(jsonString: String) {
    try {
        if (Files.notExists(APP_DIR_PATH)) {
            Files.createDirectories(APP_DIR_PATH)
            println("Created application directory: $APP_DIR_PATH")
        }
        Files.writeString(PROJECTS_FILE_PATH, jsonString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        println("Projects saved to: $PROJECTS_FILE_PATH")
    } catch (e: IOException) {
        println("Error saving projects to file: ${e.message}")
        // Optionally, rethrow or handle more gracefully
    }
}

actual fun loadProjectsFromFile(): String? {
    try {
        if (Files.exists(PROJECTS_FILE_PATH)) {
            val content = Files.readString(PROJECTS_FILE_PATH)
            println("Projects loaded from: $PROJECTS_FILE_PATH")
            return content
        } else {
            println("Projects file not found: $PROJECTS_FILE_PATH")
            return null
        }
    } catch (e: IOException) {
        println("Error loading projects from file: ${e.message}")
        return null
    }
}
