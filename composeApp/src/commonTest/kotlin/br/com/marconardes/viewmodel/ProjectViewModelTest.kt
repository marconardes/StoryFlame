package br.com.marconardes.viewmodel

// These must be top-level to be accessible by TestFileIO.kt in the same package & source set (commonTest)
// and are modified by the test setup.
internal var mockSavedJsonDataForTest: String? = null
internal var mockLoadedJsonDataForTest: String? = null

import br.com.marconardes.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModelTest {

    private lateinit var viewModel: ProjectViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockSavedJsonDataForTest = null // Use new variable name
        mockLoadedJsonDataForTest = null // Use new variable name
        viewModel = ProjectViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createProject should add a new project and save`() = runTest {
        val initialProjectCount = viewModel.projects.first().size
        assertEquals(0, initialProjectCount, "Initially, project list should be empty if nothing loaded")

        viewModel.createProject("Test Project 1")
        val projectsAfterCreate = viewModel.projects.first()
        assertEquals(initialProjectCount + 1, projectsAfterCreate.size, "Project count should increase by 1")
        assertEquals("Test Project 1", projectsAfterCreate.last().name, "New project should have the correct name")
        assertTrue(projectsAfterCreate.last().chapters.isEmpty(), "New project should have empty chapters list")

        assertNotNull(mockSavedJsonDataForTest, "saveProjectsToFile should have been called")
        val savedProjects = Json.decodeFromString<List<Project>>(mockSavedJsonDataForTest!!)
        assertEquals(1, savedProjects.size, "Saved JSON should contain one project")
        assertEquals("Test Project 1", savedProjects.first().name, "Saved project should have correct name")
    }

    @Test
    fun `loadProjects should load from file if valid JSON exists`() = runTest {
        val sampleProject = Project(name = "Loaded Project", creationDate = "2023-01-01")
        val sampleProjects = listOf(sampleProject)
        mockLoadedJsonDataForTest = Json.encodeToString(sampleProjects) // Use new variable name

        viewModel = ProjectViewModel()

        val loadedProjects = viewModel.projects.first()
        assertEquals(1, loadedProjects.size, "Should load one project")
        assertEquals("Loaded Project", loadedProjects.first().name, "Loaded project name should match")
        assertEquals(sampleProject.id, viewModel.selectedProject.first()?.id, "First project should be auto-selected")
    }

    @Test
    fun `loadProjects should start with empty list if no file`() = runTest {
        mockLoadedJsonDataForTest = null // Use new variable name
        viewModel = ProjectViewModel()

        val projects = viewModel.projects.first()
        assertTrue(projects.isEmpty(), "Project list should be empty if no file")
        assertNull(viewModel.selectedProject.first(), "No project should be selected if list is empty")
    }

    @Test
    fun `loadProjects should start with empty list if json is invalid`() = runTest {
        mockLoadedJsonDataForTest = "invalid json" // Use new variable name
        viewModel = ProjectViewModel()

        val projects = viewModel.projects.first()
        assertTrue(projects.isEmpty(), "Project list should be empty if JSON is invalid")
        assertNull(viewModel.selectedProject.first(), "No project should be selected if JSON is invalid")
    }

    @Test
    fun `selectProject should update selectedProject and its chapters`() = runTest {
        val project1 = Project(name = "Project 1", creationDate = "d1")
        val project2 = Project(name = "Project 2", creationDate = "d2")
        mockLoadedJsonDataForTest = Json.encodeToString(listOf(project1, project2)) // Use new variable name
        viewModel = ProjectViewModel()

        assertEquals(project1.id, viewModel.selectedProject.first()?.id, "Initially project1 should be selected")

        viewModel.selectProject(project2)
        assertEquals(project2.id, viewModel.selectedProject.first()?.id, "Project 2 should now be selected")
    }

    @Test
    fun `addChapter should add a chapter to the selected project and save`() = runTest {
        viewModel.createProject("Project With Chapters")
        val selectedProject = viewModel.selectedProject.first()
        assertNotNull(selectedProject, "A project should be selected")

        val initialChapterCount = selectedProject.chapters.size
        viewModel.addChapter(selectedProject, "New Chapter 1")

        val updatedSelectedProject = viewModel.selectedProject.first()
        assertNotNull(updatedSelectedProject)
        assertEquals(initialChapterCount + 1, updatedSelectedProject.chapters.size, "Chapter count should increase")
        assertEquals("New Chapter 1", updatedSelectedProject.chapters.last().title, "New chapter title is incorrect")
        assertEquals(initialChapterCount, updatedSelectedProject.chapters.last().order, "New chapter order is incorrect")

        assertNotNull(mockSavedJsonDataForTest, "saveProjectsToFile should be called after adding a chapter")
        val savedProjects = Json.decodeFromString<List<Project>>(mockSavedJsonDataForTest!!)
        val projectInSave = savedProjects.find { it.id == selectedProject.id }
        assertNotNull(projectInSave, "Saved projects should contain the modified project")
        assertEquals(1, projectInSave.chapters.size, "Saved project should have one chapter")
        assertEquals("New Chapter 1", projectInSave.chapters.first().title, "Saved chapter title is incorrect")
    }

    @Test
    fun `deleteChapter should remove chapter from selected project and save`() = runTest {
        viewModel.createProject("Project For Deleting Chapters")
        val selectedProject = viewModel.selectedProject.first()!!
        viewModel.addChapter(selectedProject, "Chapter To Delete")

        val chapterToDelete = viewModel.selectedProject.first()!!.chapters.first()
        val initialChapterCount = viewModel.selectedProject.first()!!.chapters.size
        assertEquals(1, initialChapterCount, "Should have one chapter before delete")

        viewModel.deleteChapter(selectedProject, chapterToDelete.id)

        val updatedSelectedProject = viewModel.selectedProject.first()
        assertNotNull(updatedSelectedProject, "Selected project should still exist")
        assertEquals(0, updatedSelectedProject.chapters.size, "Chapter count should be zero after delete")

        assertNotNull(mockSavedJsonDataForTest, "saveProjectsToFile should be called after deleting chapter")
        val savedProjects = Json.decodeFromString<List<Project>>(mockSavedJsonDataForTest!!)
        val projectInSave = savedProjects.find { it.id == selectedProject.id }
        assertNotNull(projectInSave, "Saved projects should contain the modified project")
        assertTrue(projectInSave.chapters.isEmpty(), "Saved project should have no chapters")
    }
}
