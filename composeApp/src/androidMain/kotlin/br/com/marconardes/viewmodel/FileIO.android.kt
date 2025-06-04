package br.com.marconardes.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import java.io.File

// This is a simplified implementation. In a real app, you'd likely use dependency injection
// to get the Application context.
@SuppressLint("StaticFieldLeak")
object objectContextProvider {
    internal lateinit var applicationContext: Application

    fun getContext(): Application {
        if (!::applicationContext.isInitialized) {
            throw IllegalStateException("Application context not initialized")
        }
        return applicationContext
    }
}

actual fun saveProjectsToFile(jsonString: String) {
    try {
        val context = objectContextProvider.getContext()
        val file = File(context.filesDir, "projects.json")
        file.writeText(jsonString)
    } catch (e: IllegalStateException) {
        // Handle the case where context is not available, perhaps log an error or throw an exception
        println("Error: Application context not available for saving file. ${e.message}")
    } catch (e: java.io.IOException) {
        println("Error writing file: ${e.message}")
    }
}

actual fun loadProjectsFromFile(): String? {
    return try {
        val context = objectContextProvider.getContext()
        val file = File(context.filesDir, "projects.json")
        if (file.exists()) {
            file.readText()
        } else {
            null
        }
    } catch (e: IllegalStateException) {
        // Handle the case where context is not available
        println("Error: Application context not available for loading file. ${e.message}")
        null
    } catch (e: java.io.IOException) {
        println("Error reading file: ${e.message}")
        null
    } catch (e: Exception) {
        println("Error loading file: ${e.message}")
        null
    }
}
