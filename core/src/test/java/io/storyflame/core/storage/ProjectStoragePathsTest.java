package io.storyflame.core.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.storyflame.core.model.Project;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ProjectStoragePathsTest {
    @Test
    void resolvesManagedArchivePathUsingCurrentTitle() {
        Path baseDirectory = Path.of("/tmp/storyflame-projects");
        Project project = Project.blank("Novo Nome", "Marco");
        Path currentPath = baseDirectory.resolve("nome-antigo.storyflame");

        Path resolvedPath = ProjectStoragePaths.resolveManagedArchivePath(baseDirectory, currentPath, project);

        assertEquals(baseDirectory.resolve("novo-nome.storyflame"), resolvedPath);
    }

    @Test
    void keepsCustomArchivePathOutsideManagedDirectory() {
        Path baseDirectory = Path.of("/tmp/storyflame-projects");
        Project project = Project.blank("Novo Nome", "Marco");
        Path currentPath = Path.of("/tmp/exports/meu-arquivo.storyflame");

        Path resolvedPath = ProjectStoragePaths.resolveManagedArchivePath(baseDirectory, currentPath, project);

        assertEquals(currentPath, resolvedPath);
    }
}
