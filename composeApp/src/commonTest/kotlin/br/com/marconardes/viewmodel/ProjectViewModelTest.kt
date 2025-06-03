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

    @Test
    fun `updateChapterTitle should change title and save`() = runTest {
        viewModel.createProject("Project For Title Update")
        var selectedProject = viewModel.selectedProject.first()!!
        viewModel.addChapter(selectedProject, "Original Title")

        selectedProject = viewModel.selectedProject.first()!! // Refresh selectedProject after addChapter
        val chapterToUpdate = selectedProject.chapters.first()
        val newTitle = "Updated Chapter Title"

        viewModel.updateChapterTitle(selectedProject, chapterToUpdate.id, newTitle)

        val updatedSelectedProject = viewModel.selectedProject.first()!!
        val updatedChapter = updatedSelectedProject.chapters.find { it.id == chapterToUpdate.id }
        assertNotNull(updatedChapter, "Chapter should still exist")
        assertEquals(newTitle, updatedChapter.title, "Chapter title should be updated")

        assertNotNull(mockSavedJsonDataForTest, "saveProjectsToFile should have been called")
        val savedProjects = Json.decodeFromString<List<Project>>(mockSavedJsonDataForTest!!)
        val projectInSave = savedProjects.find { it.id == selectedProject.id }!!
        assertEquals(newTitle, projectInSave.chapters.find { it.id == chapterToUpdate.id }?.title, "Saved chapter title incorrect")
    }

    @Test
    fun `moveChapter should reorder chapters and save`() = runTest {
        viewModel.createProject("Project For Reordering")
        var selectedProject = viewModel.selectedProject.first()!!

        // Add 3 chapters: C0 (order 0), C1 (order 1), C2 (order 2)
        viewModel.addChapter(selectedProject, "Chapter Alpha") // order 0
        selectedProject = viewModel.selectedProject.first()!!
        viewModel.addChapter(selectedProject, "Chapter Beta")  // order 1
        selectedProject = viewModel.selectedProject.first()!!
        viewModel.addChapter(selectedProject, "Chapter Gamma") // order 2
        selectedProject = viewModel.selectedProject.first()!!


        val chaptersInitial = selectedProject.chapters.sortedBy { it.order }
        val chapterAlphaId = chaptersInitial.find { it.title == "Chapter Alpha" }!!.id // Initially order 0
        val chapterBetaId = chaptersInitial.find { it.title == "Chapter Beta" }!!.id    // Initially order 1
        val chapterGammaId = chaptersInitial.find { it.title == "Chapter Gamma" }!!.id // Initially order 2

        // Move Chapter Alpha (order 0) down to order 1
        viewModel.moveChapter(selectedProject, chapterAlphaId, moveUp = false)
        var chaptersAfterMoveDown = viewModel.selectedProject.first()!!.chapters.sortedBy { it.order }

        assertEquals("Chapter Beta", chaptersAfterMoveDown[0].title, "Beta should be order 0 after Alpha moved down")
        assertEquals(0, chaptersAfterMoveDown[0].order, "Beta order check")
        assertEquals("Chapter Alpha", chaptersAfterMoveDown[1].title, "Alpha should be order 1 after move down")
        assertEquals(1, chaptersAfterMoveDown[1].order, "Alpha order check")
        assertEquals("Chapter Gamma", chaptersAfterMoveDown[2].title, "Gamma should remain order 2")
        assertEquals(2, chaptersAfterMoveDown[2].order, "Gamma order check")
        assertNotNull(mockSavedJsonDataForTest, "Save should be called after moving chapter down")
        var savedProjects = Json.decodeFromString<List<Project>>(mockSavedJsonDataForTest!!)
        var projectInSave = savedProjects.find { it.id == selectedProject.id }!!
        assertEquals(0, projectInSave.chapters.find { it.id == chapterBetaId }!!.order)
        assertEquals(1, projectInSave.chapters.find { it.id == chapterAlphaId }!!.order)

        // Reset mock save before next action for clean check
        mockSavedJsonDataForTest = null

        // Move Chapter Alpha (now at order 1) up to order 0
        selectedProject = viewModel.selectedProject.first()!! // Refresh selected project instance
        viewModel.moveChapter(selectedProject, chapterAlphaId, moveUp = true)
        var chaptersAfterMoveUp = viewModel.selectedProject.first()!!.chapters.sortedBy { it.order }

        assertEquals("Chapter Alpha", chaptersAfterMoveUp[0].title, "Alpha should be order 0 after move up")
        assertEquals(0, chaptersAfterMoveUp[0].order, "Alpha order check after move up")
        assertEquals("Chapter Beta", chaptersAfterMoveUp[1].title, "Beta should be order 1 after Alpha moved up")
        assertEquals(1, chaptersAfterMoveUp[1].order, "Beta order check after move up")
        assertNotNull(mockSavedJsonDataForTest, "Save should be called after moving chapter up")
        savedProjects = Json.decodeFromString<List<Project>>(mockSavedJsonDataForTest!!)
        projectInSave = savedProjects.find { it.id == selectedProject.id }!!
        assertEquals(0, projectInSave.chapters.find { it.id == chapterAlphaId }!!.order)
        assertEquals(1, projectInSave.chapters.find { it.id == chapterBetaId }!!.order)
    }

    @Test
    fun `updateChapterSummary should change summary and save`() = runTest {
        // Setup: Ensure a clean slate for this test if relying on @BeforeTest for mock data reset
        // @BeforeTest should handle mockLoadedJsonDataForTest = null

        viewModel.createProject("Test Project for Summary")
        var project = viewModel.projects.first().first { it.name == "Test Project for Summary" }
        viewModel.addChapter(project, "Chapter for Summary")

        project = viewModel.projects.first().first { it.name == "Test Project for Summary" } // Refresh project instance
        val chapter = project.chapters.first()
        viewModel.selectProject(project) // Ensure project is selected

        val newSummary = "This is the updated chapter summary."
        viewModel.updateChapterSummary(project, chapter.id, newSummary)

        val updatedProject = viewModel.projects.first().find { it.id == project.id }
        assertNotNull(updatedProject, "Project should exist after update")
        val updatedChapter = updatedProject.chapters.find { it.id == chapter.id }
        assertNotNull(updatedChapter, "Chapter should exist after update")
        assertEquals(newSummary, updatedChapter.summary, "Chapter summary should be updated in ViewModel state")

        assertNotNull(mockSavedJsonDataForTest, "Save function was not called after updating summary")
        assertTrue(
            mockSavedJsonDataForTest!!.contains(newSummary),
            "Saved JSON does not contain the new summary. JSON: $mockSavedJsonDataForTest"
        )
        assertTrue(
            mockSavedJsonDataForTest!!.contains(chapter.id),
            "Saved JSON does not reference the correct chapter ID for summary update. JSON: $mockSavedJsonDataForTest"
        )
        // More specific JSON check:
        val savedProjects = Json.decodeFromString<List<Project>>(mockSavedJsonDataForTest!!)
        val savedProject = savedProjects.find { it.id == project.id }
        assertNotNull(savedProject, "Saved project not found in JSON")
        val savedChapter = savedProject.chapters.find { it.id == chapter.id }
        assertNotNull(savedChapter, "Saved chapter not found in JSON")
        assertEquals(newSummary, savedChapter.summary, "New summary not correctly saved in JSON")
    }
}
