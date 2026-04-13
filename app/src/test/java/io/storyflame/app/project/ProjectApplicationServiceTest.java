package io.storyflame.app.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Project;
import io.storyflame.core.storage.ProjectArchiveInspection;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectBackupService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectApplicationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void createProjectInitializesEditorStructureAndPersistsArchive() {
        ProjectApplicationService service = newService();

        ProjectApplicationService.LoadedProject loadedProject = service.createProject("Novo Projeto", "Autor");

        assertNotNull(loadedProject.project());
        assertTrue(Files.exists(loadedProject.path()));
        assertEquals(1, loadedProject.project().getChapters().size());
        assertEquals(1, loadedProject.project().getChapters().get(0).getScenes().size());
        assertFalse(loadedProject.project().getNarrativeTags().isEmpty());
    }

    @Test
    void saveProjectRenamesManagedArchiveWhenTitleChanges() {
        ProjectApplicationService service = newService();
        ProjectApplicationService.LoadedProject loadedProject = service.createProject("Projeto Inicial", "Autor");
        Project project = loadedProject.project();
        Path previousPath = loadedProject.path();
        project.setTitle("Projeto Renomeado");

        ProjectApplicationService.SaveProjectResult result = service.saveProject(project, previousPath);

        assertEquals("projeto-renomeado.storyflame", result.path().getFileName().toString());
        assertTrue(Files.exists(result.path()));
    }

    @Test
    void openProjectRestoresSavedProject() {
        ProjectApplicationService service = newService();
        ProjectApplicationService.LoadedProject created = service.createProject("Projeto Aberto", "Autor");

        ProjectApplicationService.LoadedProject reopened = service.openProject(created.path());

        assertEquals(created.path(), reopened.path());
        assertEquals("Projeto Aberto", reopened.project().getTitle());
        assertFalse(reopened.project().getChapters().isEmpty());
    }

    @Test
    void dtoContractsExposeValidationAndMessage() {
        ProjectApplicationService service = newService();

        ProjectOperationResult result = service.createProject(new CreateProjectRequest("DTO Projeto", "Iris"));

        assertEquals("DTO Projeto", result.project().getTitle());
        assertEquals("SAVE_ARCHIVE", result.validation().operation());
        assertTrue(result.message().startsWith("Projeto criado em "));
    }

    @Test
    void exportProjectArchiveAddsExtensionWhenMissing() {
        ProjectApplicationService service = newService();
        Project project = service.createProject("Projeto Exportado", "Autor").project();

        ProjectApplicationService.ExportArchiveResult result =
                service.exportProjectArchive(project, tempDir.resolve("exportado"));

        assertEquals("exportado.storyflame", result.path().getFileName().toString());
        assertTrue(Files.exists(result.path()));
    }

    @Test
    void inspectProjectArchiveReportsValidArchive() {
        ProjectApplicationService service = newService();
        Path archivePath = service.createProject("Projeto Inspecionado", "Autor").path();

        ProjectApplicationService.InspectArchiveResult result = service.inspectProjectArchive(archivePath);

        assertTrue(result.inspection().valid());
    }

    @Test
    void importProjectArchiveReturnsInspectionAndLoadedProject() {
        ProjectApplicationService sourceService = newService();
        Path sourceArchive = sourceService.createProject("Projeto Importado", "Autor").path();

        ProjectApplicationService targetService = new ProjectApplicationService(
                new ProjectArchiveStore(tempDir.resolve("other-projects")),
                new ProjectBackupService(tempDir.resolve("other-backups"), 5, Duration.ZERO)
        );

        ProjectApplicationService.ImportArchiveResult result = targetService.importProjectArchive(sourceArchive);

        assertTrue(result.inspection().valid());
        assertNotNull(result.loadedProject());
        assertEquals("Projeto Importado", result.loadedProject().project().getTitle());
        assertTrue(Files.exists(result.loadedProject().path()));
    }

    @Test
    void importProjectArchiveReturnsInvalidInspectionForMalformedArchive() throws Exception {
        ProjectApplicationService service = newService();
        Path invalidArchive = tempDir.resolve("invalid.storyflame");
        Files.writeString(invalidArchive, "nao e zip");

        ProjectApplicationService.ImportArchiveResult result = service.importProjectArchive(invalidArchive);

        ProjectArchiveInspection inspection = result.inspection();
        assertFalse(inspection.valid());
        assertEquals(null, result.loadedProject());
    }

    private ProjectApplicationService newService() {
        Path projectsDirectory = tempDir.resolve("projects");
        Path backupsDirectory = tempDir.resolve("backups");
        return new ProjectApplicationService(
                new ProjectArchiveStore(projectsDirectory),
                new ProjectBackupService(backupsDirectory, 5, Duration.ZERO)
        );
    }
}
