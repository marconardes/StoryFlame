package io.storyflame.app.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProjectEditorApplicationServiceTest {
    private final ProjectEditorApplicationService service = new ProjectEditorApplicationService();

    @Test
    void updateProjectMetadataUpdatesTitleAndAuthor() {
        Project project = Project.blank("Antes", "Autor A");

        service.updateProjectMetadata(project, "Depois", "Autor B");

        assertEquals("Depois", project.getTitle());
        assertEquals("Autor B", project.getAuthor());
    }

    @Test
    void updateChapterTitleTouchesProject() {
        Project project = Project.blank("Projeto", "Autor");
        Chapter chapter = new Chapter(null, "Capitulo A", java.util.List.of());
        project.getChapters().add(chapter);
        Instant previousUpdate = project.getUpdatedAt();

        service.updateChapterTitle(project, chapter, "Capitulo B");

        assertEquals("Capitulo B", chapter.getTitle());
        assertTrue(!project.getUpdatedAt().isBefore(previousUpdate));
    }

    @Test
    void updateSceneDraftUpdatesAllEditableFields() {
        Project project = Project.blank("Projeto", "Autor");
        Scene scene = new Scene(null, "Cena A", "Resumo A", "Texto A", null);

        service.updateSceneDraft(project, scene, "Cena B", "Resumo B", "Texto B");

        assertEquals("Cena B", scene.getTitle());
        assertEquals("Resumo B", scene.getSynopsis());
        assertEquals("Texto B", scene.getContent());
    }
}
