package br.com.marconardes.viewmodel

import java.io.File
import java.io.IOException

actual fun saveProjectsToFile(jsonString: String) {
    try {
        val file = File("projects.json")
        file.writeText(jsonString)
    } catch (e: IOException) {
        println("Error saving file on desktop: ${e.message}")
    }
}

actual fun loadProjectsFromFile(): String? {
    return try {
        val file = File("projects.json")
        if (file.exists()) {
            file.readText()
        } else {
            null
        }
    } catch (e: IOException) {
        println("Error loading file on desktop: ${e.message}")
        null
    }
}
