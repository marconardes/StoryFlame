package io.storyflame.app.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Project;
import io.storyflame.core.publication.PublicationExportService;
import io.storyflame.core.publication.PublicationFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectPublicationApplicationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void exportsPublicationTxtThroughApplicationContract() {
        Project project = Project.blank("Projeto Publicavel", "Autor");
        ProjectPublicationApplicationService service = newService();

        PublicationOperationResult result = service.export(project, PublicationFormat.TXT, tempDir.resolve("saida.txt"));

        assertEquals(tempDir.resolve("saida.txt"), result.path());
        assertTrue(Files.exists(result.path()));
        assertTrue(result.message().contains("Manuscrito publicado"));
    }

    private ProjectPublicationApplicationService newService() {
        return new ProjectPublicationApplicationService(new PublicationExportService());
    }
}
