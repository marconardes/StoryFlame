package br.com.marconardes.storyflame.swing.viewmodel;

import br.com.marconardes.storyflame.swing.model.Chapter;
import br.com.marconardes.storyflame.swing.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProjectViewModelTest {

    private ProjectViewModel viewModel;
    private Path tempProjectsFilePath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // Create a temporary file for projects.json in a temporary directory
        tempProjectsFilePath = tempDir.resolve("projects.json");
        // Initialize ViewModel with the path to the temporary projects file
        viewModel = new ProjectViewModel(tempProjectsFilePath.toString());
    }

    @Test
    void testUpdateChapterContent() {
        // Create a new project
        viewModel.createProject("Test Project for Content Update");
        Project createdProject = viewModel.getProjects().get(0);
        assertNotNull(createdProject, "Created project should not be null");

        // Add a chapter to this project
        Chapter testChapter = viewModel.addChapter(createdProject, "Test Chapter");
        assertNotNull(testChapter, "Test chapter should not be null");
        assertNotNull(testChapter.getId(), "Test chapter ID should not be null");

        // Define new content
        String newContent = "This is the updated chapter content.";

        // Call the method under test
        viewModel.updateChapterContent(createdProject.getId(), testChapter.getId(), newContent);

        // Retrieve the project and chapter again to verify the update
        // Find the project from the list of projects
        Project updatedProject = viewModel.getProjects().stream()
                .filter(p -> p.getId().equals(createdProject.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(updatedProject, "Updated project should not be null");

        Optional<Chapter> updatedChapterOpt = updatedProject.getChapters().stream()
                .filter(c -> c.getId().equals(testChapter.getId()))
                .findFirst();

        assertTrue(updatedChapterOpt.isPresent(), "Updated chapter should be present in the project");
        assertEquals(newContent, updatedChapterOpt.get().getContent(), "Chapter content should be updated");

        // Also check if the selected project in ViewModel reflects the change if it was selected
        viewModel.selectProject(updatedProject); // Ensure the project is selected
        Project selectedProjectInVm = viewModel.getSelectedProject();
        assertNotNull(selectedProjectInVm, "Selected project in ViewModel should not be null");

        Optional<Chapter> chapterFromViewModelOpt = selectedProjectInVm.getChapters().stream()
            .filter(c -> c.getId().equals(testChapter.getId()))
            .findFirst();
        assertTrue(chapterFromViewModelOpt.isPresent(), "Chapter should be found in ViewModel's selected project");
        assertEquals(newContent, chapterFromViewModelOpt.get().getContent(), "Chapter content in ViewModel's selected project should be updated");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up the temporary projects.json file
        // This is important to ensure tests are isolated and don't interfere with each other
        // or leave artifacts in the user's home directory.
        // Note: @TempDir should handle deletion of the tempDir and its contents,
        // but explicit deletion can be added if issues arise or for non-@TempDir setups.
        // For now, relying on @TempDir for cleanup.
        // System.out.println("Temp projects file was: " + tempProjectsFilePath);
        // if (tempProjectsFilePath != null && Files.exists(tempProjectsFilePath)) {
        //     Files.deleteIfExists(tempProjectsFilePath);
        // }
        // Also, reset ViewModel state if necessary, e.g. clear projects list
        // viewModel.getProjects().clear();
        // viewModel.setSelectedProject(null);
        // viewModel.saveProjects(); // This would write an empty list to the temp file
    }
}
