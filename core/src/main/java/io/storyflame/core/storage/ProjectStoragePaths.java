package io.storyflame.core.storage;

import io.storyflame.core.model.Project;
import java.nio.file.Path;
import java.text.Normalizer;

public final class ProjectStoragePaths {
    public static final String ARCHIVE_EXTENSION = ".storyflame";

    private ProjectStoragePaths() {
    }

    public static Path defaultDesktopProjectsDirectory() {
        return Path.of(System.getProperty("user.home"), ".storyflame", "projects");
    }

    public static Path suggestedArchivePath(Path baseDirectory, Project project) {
        String normalizedTitle = project.getTitle().isBlank() ? "untitled-project" : sanitize(project.getTitle());
        if (normalizedTitle.isBlank()) {
            normalizedTitle = "untitled-project";
        }
        return baseDirectory.resolve(normalizedTitle + ARCHIVE_EXTENSION);
    }

    public static Path resolveManagedArchivePath(Path baseDirectory, Path currentPath, Project project) {
        Path normalizedBaseDirectory = baseDirectory.toAbsolutePath().normalize();
        Path normalizedCurrentPath = currentPath.toAbsolutePath().normalize();
        if (!normalizedCurrentPath.startsWith(normalizedBaseDirectory)) {
            return currentPath;
        }
        return suggestedArchivePath(baseDirectory, project);
    }

    public static String sanitize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }
}
