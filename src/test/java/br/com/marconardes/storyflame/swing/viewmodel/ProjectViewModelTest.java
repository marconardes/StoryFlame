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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProjectViewModelTest {

    private ProjectViewModel viewModel;
    private Path tempProjectsFilePath;

    private String getTodayDateString() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

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
        Project createdProject = viewModel.getProjects().get(0); // Assumes createProject adds it to the list and test can get it
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

    @Test
    void testDailyWordCount_InitialProjectIsEmpty() {
        viewModel.createProject("Empty Project");
        Project project = viewModel.getProjects().stream()
                            .filter(p -> p.getName().equals("Empty Project"))
                            .findFirst()
                            .orElse(null);
        assertNotNull(project, "Project should be created");
        assertNotNull(project.getDailyWordCounts(), "DailyWordCounts map should not be null");
        assertTrue(project.getDailyWordCounts().isEmpty(), "DailyWordCounts map should be empty initially by Project model, " +
                                                           "or after createProject if stats were calculated for 0 words and then cleared if 0.");
        // Note: createProject itself doesn't call updateDailyWordCountStats.
        // The first call to update stats will be when a chapter content is modified or chapter added/deleted.
        // So, an initial project will indeed have an empty dailyWordCounts map.
    }

    @Test
    void testDailyWordCount_AfterContentUpdate() {
        String projectName = "WordCountContentProject";
        viewModel.createProject(projectName);
        Project project = viewModel.getProjects().stream().filter(p -> p.getName().equals(projectName)).findFirst().get();
        Chapter chapter = viewModel.addChapter(project, "Chapter 1");

        String today = getTodayDateString();

        // First update
        String content1 = "Palavra um dois tres"; // 3 words
        viewModel.updateChapterContent(project.getId(), chapter.getId(), content1);
        Project updatedProject1 = viewModel.getProjects().stream().filter(p -> p.getId().equals(project.getId())).findFirst().get();
        Map<String, Integer> counts1 = updatedProject1.getDailyWordCounts();
        assertNotNull(counts1, "Daily word counts map should not be null");
        assertEquals(1, counts1.size(), "Should have one entry for today");
        assertTrue(counts1.containsKey(today), "Should contain an entry for today");
        assertEquals(4, counts1.get(today), "Word count for today should be 4"); // Corrected expected: "Palavra um dois tres" is 4 words

        // Second update on the same day
        String content2 = "Um dois tres quatro cinco"; // 5 words
        viewModel.updateChapterContent(project.getId(), chapter.getId(), content2);
        Project updatedProject2 = viewModel.getProjects().stream().filter(p -> p.getId().equals(project.getId())).findFirst().get();
        Map<String, Integer> counts2 = updatedProject2.getDailyWordCounts();
        assertNotNull(counts2);
        assertEquals(1, counts2.size(), "Should still have one entry for today (overwritten)");
        assertTrue(counts2.containsKey(today));
        assertEquals(5, counts2.get(today), "Word count for today should be updated to 5");
    }

    @Test
    void testDailyWordCount_AfterAddingChapter() {
        String projectName = "WordCountAddChapterProject";
        viewModel.createProject(projectName);
        Project project = viewModel.getProjects().stream().filter(p -> p.getName().equals(projectName)).findFirst().get();
        String today = getTodayDateString();

        // Add first chapter and set its content
        Chapter chapter1 = viewModel.addChapter(project, "Chapter 1"); // addChapter calls updateDailyWordCountStats
        // At this point, chapter1 is empty, so word count for today is 0.
        Project projectAfterC1Add = viewModel.getProjects().stream().filter(p -> p.getId().equals(project.getId())).findFirst().get();
        assertEquals(0, projectAfterC1Add.getDailyWordCounts().getOrDefault(today, -1), "Word count should be 0 after adding empty chapter1");

        viewModel.updateChapterContent(project.getId(), chapter1.getId(), "Ola mundo"); // 2 words
        Project projectAfterC1Content = viewModel.getProjects().stream().filter(p -> p.getId().equals(project.getId())).findFirst().get();
        assertEquals(2, projectAfterC1Content.getDailyWordCounts().getOrDefault(today, -1), "Word count for today should be 2 after chapter1 content");

        // Add second chapter and set its content
        Chapter chapter2 = viewModel.addChapter(projectAfterC1Content, "Chapter 2"); // addChapter calls updateDailyWordCountStats
        // At this point, chapter2 is empty, total word count for today is still 2 (from chapter1).
        Project projectAfterC2Add = viewModel.getProjects().stream().filter(p -> p.getId().equals(project.getId())).findFirst().get();
        assertEquals(2, projectAfterC2Add.getDailyWordCounts().getOrDefault(today, -1), "Word count should be 2 after adding empty chapter2");

        viewModel.updateChapterContent(project.getId(), chapter2.getId(), "Mais tres palavras"); // 3 words
        Project projectAfterC2Content = viewModel.getProjects().stream().filter(p -> p.getId().equals(project.getId())).findFirst().get();
        assertEquals(2 + 3, projectAfterC2Content.getDailyWordCounts().getOrDefault(today, -1), "Total word count for today should be 5");
    }

    @Test
    void testDailyWordCount_AfterDeletingChapter() {
        String projectName = "WordCountDeleteChapterProject";
        viewModel.createProject(projectName);
        Project project = viewModel.getProjects().stream().filter(p -> p.getName().equals(projectName)).findFirst().get();
        String today = getTodayDateString();

        Chapter chapter1 = viewModel.addChapter(project, "Chapter 1");
        viewModel.updateChapterContent(project.getId(), chapter1.getId(), "Ola mundo"); // 2 words

        Chapter chapter2 = viewModel.addChapter(project, "Chapter 2");
        viewModel.updateChapterContent(project.getId(), chapter2.getId(), "Mais tres palavras"); // 3 words

        Project projectBeforeDelete = viewModel.getProjects().stream().filter(p -> p.getId().equals(project.getId())).findFirst().get();
        assertEquals(2 + 3, projectBeforeDelete.getDailyWordCounts().getOrDefault(today, -1), "Word count before delete should be 5");

        viewModel.deleteChapter(project.getId(), chapter2.getId()); // Deletes chapter2 (3 words)

        Project projectAfterDelete = viewModel.getProjects().stream().filter(p -> p.getId().equals(project.getId())).findFirst().get();
        assertNotNull(projectAfterDelete.getDailyWordCounts().get(today), "Daily count for today should exist");
        assertEquals(2, projectAfterDelete.getDailyWordCounts().get(today), "Word count after delete should be 2");
    }
}
