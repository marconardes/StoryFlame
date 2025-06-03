package br.com.marconardes.viewmodel

import br.com.marconardes.model.Chapter
import br.com.marconardes.model.Project
import kotlin.test.*

// Variables to be accessed by actual implementations in desktopTest
var mockSavedJsonDataForTest: String? = null
var mockLoadedJsonDataForTest: String? = "[]"

class ProjectViewModelTest {
    private lateinit var viewModel: ProjectViewModel

    @BeforeTest
    fun setup() {
        // Reset mocks before each test
        mockSavedJsonDataForTest = null
        // Default to loading an empty list of projects, or a specific state if needed for a test
        mockLoadedJsonDataForTest = "[]"
        viewModel = ProjectViewModel() // Reinitialize ViewModel to ensure clean state based on mocks
    }

    @Test
    fun `updateChapterContent should update chapter content and save projects`() {
        // 1. Create a project and add a chapter
        val initialProjectName = "Test Project"
        viewModel.createProject(initialProjectName)

        // Assuming createProject or subsequent operations might change selectedProject,
        // it's safer to fetch the project directly from the projects list if selection logic is complex.
        // For this test, we rely on the current behavior where the first project created might be auto-selected
        // or that addChapter operates on the passed project instance correctly.

        var project = viewModel.projects.value.find { it.name == initialProjectName }
        assertNotNull(project, "Project should be created and found in the list.")

        val initialChapterTitle = "Chapter 1"
        viewModel.addChapter(project!!, initialChapterTitle) // Use !! because we asserted not null

        // Refresh project instance from ViewModel state after adding chapter as ViewModel updates immutably
        project = viewModel.projects.value.find { it.id == project.id }
        assertNotNull(project, "Project should still exist after adding a chapter.")

        val chapter = project.chapters.find { it.title == initialChapterTitle }
        assertNotNull(chapter, "Chapter should be added to the project.")

        // 2. Update chapter content
        val newContent = "<p>This is the new chapter content.</p>"
        viewModel.updateChapterContent(project, chapter.id, newContent)

        // 3. Assertions
        // Fetch the latest project state from the ViewModel again
        val updatedProject = viewModel.projects.value.find { it.id == project.id }
        assertNotNull(updatedProject, "Updated project should not be null.")

        val updatedChapter = updatedProject.chapters.find { it.id == chapter.id }
        assertNotNull(updatedChapter, "Updated chapter should not be null.")
        assertEquals(newContent, updatedChapter.content, "Chapter content should be updated.")

        // Check if saveProjects was called (indirectly, by checking mockSavedJsonDataForTest)
        assertNotNull(mockSavedJsonDataForTest, "saveProjectsToFile should have been called.")
        assertTrue(mockSavedJsonDataForTest!!.contains(newContent), "Saved JSON should contain the new content: '$newContent'. Actual: ${mockSavedJsonDataForTest}")
        assertTrue(mockSavedJsonDataForTest!!.contains(chapter.id), "Saved JSON should contain the chapter ID. Actual: ${mockSavedJsonDataForTest}")
        assertTrue(mockSavedJsonDataForTest!!.contains(project.id), "Saved JSON should contain the project ID. Actual: ${mockSavedJsonDataForTest}")
    }

    @Test
    fun `createProject should add a new project and save`() {
        val projectName = "Brand New Project"
        viewModel.createProject(projectName)

        val project = viewModel.projects.value.find { it.name == projectName }
        assertNotNull(project, "Project should be created.")
        assertEquals(0, project.chapters.size, "New project should have no chapters.")

        assertNotNull(mockSavedJsonDataForTest, "saveProjectsToFile should have been called after creating a project.")
        assertTrue(mockSavedJsonDataForTest!!.contains(projectName), "Saved JSON should contain the new project's name.")
    }

    @Test
    fun `addChapter should add a chapter to a project and save`() {
        viewModel.createProject("Project For Chapters")
        var project = viewModel.projects.value.first() // Get the project instance
        val chapterTitle = "My First Chapter"

        viewModel.addChapter(project, chapterTitle)

        // Re-fetch the project to get its updated state
        val updatedProject = viewModel.projects.value.find { it.id == project.id }
        assertNotNull(updatedProject)
        assertEquals(1, updatedProject.chapters.size, "Chapter count should be 1.")
        assertEquals(chapterTitle, updatedProject.chapters.first().title, "Chapter title should match.")
        assertEquals(0, updatedProject.chapters.first().order, "First chapter order should be 0.")

        assertNotNull(mockSavedJsonDataForTest, "saveProjectsToFile should have been called after adding a chapter.")
        assertTrue(mockSavedJsonDataForTest!!.contains(chapterTitle), "Saved JSON should contain the new chapter's title.")
    }

    @Test
    fun `updateChapterSummary should update summary and save`() {
        viewModel.createProject("Project For Summary Test")
        var project = viewModel.projects.value.first()
        viewModel.addChapter(project, "Chapter For Summary")
        project = viewModel.projects.value.first() // Refresh project
        val chapter = project.chapters.first()
        val newSummary = "This is a test summary."

        viewModel.updateChapterSummary(project, chapter.id, newSummary)

        val updatedProject = viewModel.projects.value.find { it.id == project.id }
        assertNotNull(updatedProject)
        val updatedChapter = updatedProject.chapters.find { it.id == chapter.id }
        assertNotNull(updatedChapter)
        assertEquals(newSummary, updatedChapter.summary, "Chapter summary should be updated.")

        assertNotNull(mockSavedJsonDataForTest, "saveProjectsToFile should have been called.")
        assertTrue(mockSavedJsonDataForTest!!.contains(newSummary), "Saved JSON should contain the new summary.")
    }

     @Test
    fun `loadProjects should correctly parse projects and chapters`() {
        val project1Id = "proj-1"
        val chapter1Id = "chap-1-1"
        val chapter2Id = "chap-1-2"
        val project2Id = "proj-2"

        val initialJson = """
        [
            {
                "id": "$project1Id",
                "name": "Loaded Project 1",
                "creationDate": "2023-01-01T12:00:00",
                "chapters": [
                    {
                        "id": "$chapter1Id",
                        "title": "L Chapter 1",
                        "order": 0,
                        "content": "<p>Content 1</p>",
                        "summary": "Summary 1"
                    },
                    {
                        "id": "$chapter2Id",
                        "title": "L Chapter 2",
                        "order": 1,
                        "content": "<p>Content 2</p>",
                        "summary": "Summary 2"
                    }
                ]
            },
            {
                "id": "$project2Id",
                "name": "Loaded Project 2",
                "creationDate": "2023-01-02T14:00:00",
                "chapters": []
            }
        ]
        """.trimIndent()
        mockLoadedJsonDataForTest = initialJson
        // Re-initialize ViewModel to trigger loadProjects with the new mockLoadedJsonDataForTest
        viewModel = ProjectViewModel()

        assertEquals(2, viewModel.projects.value.size, "Should load 2 projects.")
        val loadedProject1 = viewModel.projects.value.find { it.id == project1Id }
        assertNotNull(loadedProject1, "Project 1 should be loaded.")
        assertEquals("Loaded Project 1", loadedProject1.name)
        assertEquals(2, loadedProject1.chapters.size, "Project 1 should have 2 chapters.")
        assertEquals("<p>Content 1</p>", loadedProject1.chapters.find { it.id == chapter1Id }?.content)
        assertEquals("Summary 2", loadedProject1.chapters.find { it.id == chapter2Id }?.summary)

        val loadedProject2 = viewModel.projects.value.find { it.id == project2Id }
        assertNotNull(loadedProject2, "Project 2 should be loaded.")
        assertTrue(loadedProject2.chapters.isEmpty(), "Project 2 should have no chapters.")

        // Also check if selectedProject is set (usually the first one)
         assertNotNull(viewModel.selectedProject.value, "A project should be selected after loading.")
         assertEquals(project1Id, viewModel.selectedProject.value?.id, "First project should be selected.")
    }
}
