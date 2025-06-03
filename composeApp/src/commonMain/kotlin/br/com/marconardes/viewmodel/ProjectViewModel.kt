package br.com.marconardes.viewmodel

import br.com.marconardes.model.Chapter
import br.com.marconardes.model.Project
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Expected functions for platform-specific file I/O
expect fun saveProjectsToFile(jsonString: String)
expect fun loadProjectsFromFile(): String?

class ProjectViewModel {
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    // Exposes the chapters of the currently selected project
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedProjectChapters: StateFlow<List<Chapter>> = _selectedProject.flatMapLatest { project ->
        if (project != null) {
            // This assumes Project.chapters is a StateFlow or similar reactive type.
            // If Project.chapters is just a MutableList, we need to wrap it or update manually.
            // For simplicity, let's make it reflect the current list directly and update on project selection.
            // A more robust solution might involve making chapters within Project also a Flow.
            MutableStateFlow(project.chapters.toList()) // Emit a new list to trigger collection
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default), // Adjust scope as needed
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val json = Json { prettyPrint = true }
    private val projectsFileName = "projects.json" // Example filename

    init {
        loadProjects()
    }

    private fun loadProjects() {
        val projectsJson = loadProjectsFromFile()
        if (!projectsJson.isNullOrBlank()) {
            try {
                val loadedProjects = json.decodeFromString<List<Project>>(projectsJson)
                _projects.value = loadedProjects
                println("Successfully loaded projects from file.")
                if (loadedProjects.isNotEmpty()) {
                    selectProject(loadedProjects.first()) // Auto-select first project
                }
            } catch (e: Exception) {
                println("Error parsing projects from JSON: ${e.message}")
                _projects.value = emptyList() // Start with empty list if JSON is invalid
            }
        } else {
            println("No saved projects found or file content is empty. Starting with an empty list.")
            _projects.value = emptyList() // Start with empty list if no file or file is empty
            _selectedProject.value = null // Ensure no project is selected
        }
    }

    private fun saveProjects() {
        val projectsJson = json.encodeToString(_projects.value)
        saveProjectsToFile(projectsJson)
    }

    fun createProject(projectName: String) {
        val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        // Project is created with an empty chapters list by default (as per data class)
        val newProject = Project(name = projectName, creationDate = currentDateTime.toString())
        _projects.value = _projects.value + newProject
        if (_selectedProject.value == null && _projects.value.size == 1) {
            selectProject(newProject) // Auto-select if it's the first project
        }
        saveProjects()
    }

    fun selectProject(project: Project) {
        _selectedProject.value = project
        // The selectedProjectChapters flow will update automatically because it's based on _selectedProject
    }

    // Add functions to add, remove, or update chapters for the selected project here later

    fun addChapter(project: Project, chapterTitle: String) {
        val newOrder = project.chapters.size // Simple order based on current count
        val newChapter = Chapter(title = chapterTitle, order = newOrder)

        val updatedChapters = project.chapters.toMutableList().apply { add(newChapter) }
        val updatedProject = project.copy(chapters = updatedChapters)

        updateProjectInList(updatedProject)
        saveProjects()
    }

    fun updateChapterSummary(project: Project, chapterId: String, newSummary: String) {
        val updatedChapters = project.chapters.map {
            if (it.id == chapterId) it.copy(summary = newSummary) else it
        }.toMutableList()
        val updatedProject = project.copy(chapters = updatedChapters)

        updateProjectInList(updatedProject)
        saveProjects()
    }

    fun deleteChapter(project: Project, chapterId: String) {
        val updatedChapters = project.chapters.filterNot { it.id == chapterId }.toMutableList()
        // Re-evaluate order for remaining chapters if necessary, for now, just remove
        // For simplicity, we are not re-ordering other chapters now.
        // If order is critical and must be dense (0,1,2,...), then:
        // val finalChapters = updatedChapters.mapIndexed { index, chapter -> chapter.copy(order = index) }.toMutableList()
        // val updatedProject = project.copy(chapters = finalChapters)

        val updatedProject = project.copy(chapters = updatedChapters)
        updateProjectInList(updatedProject)
        saveProjects()
    }

    fun updateChapterTitle(project: Project, chapterId: String, newTitle: String) {
        val updatedChapters = project.chapters.map {
            if (it.id == chapterId) it.copy(title = newTitle) else it
        }.toMutableList()
        val updatedProject = project.copy(chapters = updatedChapters)

        updateProjectInList(updatedProject)
        saveProjects()
    }

    // Basic reorder: move chapter one position up or down.
    // More complex drag-and-drop would require more sophisticated state management.
    fun moveChapter(project: Project, chapterId: String, moveUp: Boolean) {
        val chapters = project.chapters.toMutableList()
        val chapterIndex = chapters.indexOfFirst { it.id == chapterId }

        if (chapterIndex == -1) return // Chapter not found

        val targetIndex = if (moveUp) chapterIndex - 1 else chapterIndex + 1

        if (targetIndex < 0 || targetIndex >= chapters.size) return // Cannot move further

        // Swap order properties
        val chapterToMove = chapters[chapterIndex]
        val otherChapter = chapters[targetIndex]

        chapters[chapterIndex] = otherChapter.copy(order = chapterToMove.order)
        chapters[targetIndex] = chapterToMove.copy(order = otherChapter.order)

        // Sort by new order to reflect change, then re-assign dense order
        val sortedChapters = chapters.sortedBy { it.order }.mapIndexed { index, chap -> chap.copy(order = index) }.toMutableList()

        val updatedProject = project.copy(chapters = sortedChapters)
        updateProjectInList(updatedProject)
        saveProjects()
    }


    private fun updateProjectInList(updatedProject: Project) {
        _projects.value = _projects.value.map {
            if (it.id == updatedProject.id) updatedProject else it
        }
        // If the updated project is the selected one, update _selectedProject to trigger recomposition
        if (_selectedProject.value?.id == updatedProject.id) {
            _selectedProject.value = updatedProject
        }
    }
}
