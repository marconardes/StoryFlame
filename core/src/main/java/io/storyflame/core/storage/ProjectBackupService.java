package io.storyflame.core.storage;

import io.storyflame.core.model.Project;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Objects;

public final class ProjectBackupService {
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);

    private final Path backupRootDirectory;
    private final int maxBackupsPerProject;
    private final Duration minimumInterval;

    public ProjectBackupService(Path backupRootDirectory, int maxBackupsPerProject, Duration minimumInterval) {
        this.backupRootDirectory = Objects.requireNonNull(backupRootDirectory);
        this.maxBackupsPerProject = Math.max(1, maxBackupsPerProject);
        this.minimumInterval = Objects.requireNonNull(minimumInterval);
    }

    public Path createBackup(Path archivePath, Project project) {
        Objects.requireNonNull(archivePath);
        Objects.requireNonNull(project);
        if (Files.notExists(archivePath)) {
            return null;
        }
        try {
            Path projectBackupDirectory = ProjectStoragePaths.backupDirectory(backupRootDirectory, project);
            Files.createDirectories(projectBackupDirectory);
            Instant now = Instant.now();
            if (hasRecentBackup(projectBackupDirectory, now)) {
                return null;
            }
            String fileName = BACKUP_TIMESTAMP.format(now)
                    + "-"
                    + ProjectStoragePaths.sanitize(project.getTitle().isBlank() ? "project" : project.getTitle())
                    + ProjectStoragePaths.ARCHIVE_EXTENSION;
            Path backupPath = projectBackupDirectory.resolve(fileName);
            Files.copy(archivePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            trimBackups(projectBackupDirectory);
            return backupPath;
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to create project backup for " + archivePath, exception);
        }
    }

    private boolean hasRecentBackup(Path projectBackupDirectory, Instant now) throws IOException {
        try (var stream = Files.list(projectBackupDirectory)) {
            Path latestBackup = stream
                    .filter(path -> path.getFileName().toString().endsWith(ProjectStoragePaths.ARCHIVE_EXTENSION))
                    .max(Comparator.comparing(this::lastModifiedTime))
                    .orElse(null);
            if (latestBackup == null) {
                return false;
            }
            Instant latestModified = lastModifiedTime(latestBackup);
            return latestModified.plus(minimumInterval).isAfter(now);
        }
    }

    private void trimBackups(Path projectBackupDirectory) throws IOException {
        try (var stream = Files.list(projectBackupDirectory)) {
            var backups = stream
                    .filter(path -> path.getFileName().toString().endsWith(ProjectStoragePaths.ARCHIVE_EXTENSION))
                    .sorted(Comparator.comparing(this::lastModifiedTime).reversed())
                    .toList();
            for (int index = maxBackupsPerProject; index < backups.size(); index++) {
                Files.deleteIfExists(backups.get(index));
            }
        }
    }

    private Instant lastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read last modified time for " + path, exception);
        }
    }
}
