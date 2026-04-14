package io.storyflame.app.project;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.storage.ProjectArchiveInspection;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectBackupService;
import io.storyflame.core.storage.ProjectStoragePaths;
import io.storyflame.core.tags.CharacterTagProfileSynchronizer;
import io.storyflame.core.validation.ProjectValidationResult;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class ProjectApplicationService {
    private final ProjectArchiveStore store;
    private final ProjectBackupService backupService;

    public ProjectApplicationService(ProjectArchiveStore store, ProjectBackupService backupService) {
        this.store = Objects.requireNonNull(store);
        this.backupService = Objects.requireNonNull(backupService);
    }

    public Path baseDirectory() {
        return store.getBaseDirectory();
    }

    public List<Path> listProjects() {
        return store.listProjects();
    }

    public boolean deleteProject(Path path) {
        Objects.requireNonNull(path);
        Path normalized = path.toAbsolutePath().normalize();
        Path baseDirectory = store.getBaseDirectory().toAbsolutePath().normalize();
        if (!normalized.startsWith(baseDirectory)) {
            throw new IllegalArgumentException("Projeto fora do diretorio base.");
        }
        if (!normalized.getFileName().toString().endsWith(ProjectStoragePaths.ARCHIVE_EXTENSION)) {
            throw new IllegalArgumentException("Arquivo invalido.");
        }
        try {
            return Files.deleteIfExists(normalized);
        } catch (Exception exception) {
            throw new UncheckedIOException("Nao foi possivel excluir projeto.", new java.io.IOException(exception));
        }
    }

    public LoadedProject createProject(String title, String author) {
        Project project = store.createProject(title, author);
        ensureEditorStructure(project);
        CharacterTagProfileSynchronizer.synchronize(project);
        Path path = resolveNewProjectPath(project);
        store.save(project, path);
        return new LoadedProject(project, path);
    }

    public LoadedProject openProject(Path path) {
        Objects.requireNonNull(path);
        Project project = store.open(path);
        ensureEditorStructure(project);
        return new LoadedProject(project, path);
    }

    public ProjectOperationResult createProject(CreateProjectRequest request) {
        Objects.requireNonNull(request);
        LoadedProject loadedProject = createProject(request.title(), request.author());
        return new ProjectOperationResult(
                loadedProject.project(),
                loadedProject.path(),
                ProjectValidationDto.from(validateForSave(loadedProject.project())),
                "Projeto criado."
        );
    }

    public ProjectOperationResult openProject(OpenProjectRequest request) {
        Objects.requireNonNull(request);
        LoadedProject loadedProject = openProject(request.path());
        return new ProjectOperationResult(
                loadedProject.project(),
                loadedProject.path(),
                ProjectValidationDto.from(validateForSave(loadedProject.project())),
                "Projeto aberto."
        );
    }

    public ProjectOperationResult saveProject(SaveProjectRequest request) {
        Objects.requireNonNull(request);
        SaveProjectResult saveResult = saveProject(request.project(), request.path());
        return new ProjectOperationResult(
                request.project(),
                saveResult.path(),
                ProjectValidationDto.from(validateForSave(request.project())),
                "Projeto salvo."
        );
    }

    public ExportArchiveResult exportProjectArchive(Project project, Path targetPath) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(targetPath);
        Path normalizedTargetPath = normalizeArchivePath(targetPath);
        store.exportArchive(project, normalizedTargetPath);
        return new ExportArchiveResult(normalizedTargetPath);
    }

    public InspectArchiveResult inspectProjectArchive(Path sourcePath) {
        Objects.requireNonNull(sourcePath);
        return new InspectArchiveResult(safeInspect(sourcePath));
    }

    public ImportArchiveResult importProjectArchive(Path sourcePath) {
        Objects.requireNonNull(sourcePath);
        ProjectArchiveInspection inspection = safeInspect(sourcePath);
        if (!inspection.valid()) {
            return new ImportArchiveResult(inspection, null);
        }
        Path importedPath = store.importArchive(sourcePath);
        return new ImportArchiveResult(inspection, openProject(importedPath));
    }

    public ProjectValidationResult validateForSave(Project project) {
        Objects.requireNonNull(project);
        return store.validateForSave(project);
    }

    public SaveProjectResult saveProject(Project project, Path previousPath) {
        Objects.requireNonNull(project);
        Path targetPath = resolveSavePath(previousPath, project);
        store.save(project, targetPath);
        BackupOutcome backupOutcome = createProjectBackup(targetPath, project);
        boolean retainedPreviousArchive = retainPreviousArchiveIfNeeded(previousPath, targetPath);
        return new SaveProjectResult(
                targetPath,
                backupOutcome.path(),
                backupOutcome.failed(),
                retainedPreviousArchive
        );
    }

    private BackupOutcome createProjectBackup(Path archivePath, Project project) {
        try {
            return new BackupOutcome(backupService.createBackup(archivePath, project), false);
        } catch (Exception exception) {
            return new BackupOutcome(null, true);
        }
    }

    private Path resolveSavePath(Path previousPath, Project project) {
        if (previousPath == null) {
            return ProjectStoragePaths.suggestedArchivePath(store.getBaseDirectory(), project);
        }
        return ProjectStoragePaths.resolveManagedArchivePath(store.getBaseDirectory(), previousPath, project);
    }

    private Path normalizeArchivePath(Path targetPath) {
        String fileName = targetPath.getFileName().toString();
        if (fileName.endsWith(ProjectStoragePaths.ARCHIVE_EXTENSION)) {
            return targetPath;
        }
        return targetPath.resolveSibling(fileName + ProjectStoragePaths.ARCHIVE_EXTENSION);
    }

    private ProjectArchiveInspection safeInspect(Path sourcePath) {
        try {
            return store.inspect(sourcePath);
        } catch (UncheckedIOException | IllegalStateException exception) {
            String issue = exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "Nao foi possivel verificar o arquivo."
                    : exception.getMessage();
            return new ProjectArchiveInspection(false, false, 0, List.of(issue));
        }
    }

    private boolean retainPreviousArchiveIfNeeded(Path previousPath, Path targetPath) {
        if (previousPath == null || previousPath.equals(targetPath)) {
            return false;
        }
        try {
            Files.deleteIfExists(previousPath);
            return false;
        } catch (Exception exception) {
            return true;
        }
    }

    private void ensureEditorStructure(Project project) {
        if (project.getChapters().isEmpty()) {
            project.getChapters().add(new Chapter(null, "Capitulo 1", List.of(new Scene(null, "Cena 1", "", null))));
        }
        for (Chapter chapter : project.getChapters()) {
            if (chapter.getScenes().isEmpty()) {
                chapter.getScenes().add(new Scene(null, "Cena 1", "", null));
            }
        }
    }

    private Path resolveNewProjectPath(Project project) {
        Path suggestedPath = ProjectStoragePaths.suggestedArchivePath(store.getBaseDirectory(), project);
        if (!Files.exists(suggestedPath)) {
            return suggestedPath;
        }
        String fileName = suggestedPath.getFileName().toString();
        String stem = fileName.substring(0, fileName.length() - ProjectStoragePaths.ARCHIVE_EXTENSION.length());
        for (int index = 2; index < 10_000; index++) {
            Path candidate = suggestedPath.resolveSibling(stem + "-" + index + ProjectStoragePaths.ARCHIVE_EXTENSION);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Nao foi possivel gerar um nome unico para o novo projeto.");
    }

    public record LoadedProject(Project project, Path path) {
    }

    public record SaveProjectResult(Path path, Path backupPath, boolean backupFailed, boolean retainedPreviousArchive) {
    }

    public record ExportArchiveResult(Path path) {
    }

    public record InspectArchiveResult(ProjectArchiveInspection inspection) {
    }

    public record ImportArchiveResult(ProjectArchiveInspection inspection, LoadedProject loadedProject) {
    }

    private record BackupOutcome(Path path, boolean failed) {
    }
}
