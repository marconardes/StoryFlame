package io.storyflame.app.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProjectStructureApplicationServiceTest {
    private final ProjectStructureApplicationService service = new ProjectStructureApplicationService();

    @Test
    void addChapterReturnsNewSelection() {
        Project project = Project.blank("Projeto", "Autor");

        ProjectStructureApplicationService.StructureSelection selection = service.addChapter(project);

        assertEquals(1, project.getChapters().size());
        assertSame(selection.chapter(), project.getChapters().get(0));
        assertSame(selection.scene(), selection.chapter().getScenes().get(0));
    }

    @Test
    void removeChapterSelectsPreviousRemainingChapter() {
        Project project = Project.blank("Projeto", "Autor");
        Chapter first = new Chapter(null, "Capitulo 1", List.of(new Scene(null, "Cena 1", "", null)));
        Chapter second = new Chapter(null, "Capitulo 2", List.of(new Scene(null, "Cena 2", "", null)));
        project.getChapters().add(first);
        project.getChapters().add(second);

        ProjectStructureApplicationService.StructureSelection selection = service.removeChapter(project, second);

        assertEquals(1, project.getChapters().size());
        assertSame(first, selection.chapter());
        assertSame(first.getScenes().get(0), selection.scene());
    }

    @Test
    void removeOnlyChapterIsRejected() {
        Project project = Project.blank("Projeto", "Autor");
        Chapter only = new Chapter(null, "Capitulo 1", List.of(new Scene(null, "Cena 1", "", null)));
        project.getChapters().add(only);

        assertThrows(IllegalStateException.class, () -> service.removeChapter(project, only));
    }

    @Test
    void addSceneReturnsNewSceneSelection() {
        Project project = Project.blank("Projeto", "Autor");
        Chapter chapter = new Chapter(null, "Capitulo 1", java.util.List.of(new Scene(null, "Cena 1", "", null)));
        project.getChapters().add(chapter);

        ProjectStructureApplicationService.StructureSelection selection = service.addScene(project, chapter);

        assertEquals(2, chapter.getScenes().size());
        assertSame(chapter, selection.chapter());
        assertSame(chapter.getScenes().get(1), selection.scene());
    }

    @Test
    void removeSceneSelectsPreviousRemainingScene() {
        Project project = Project.blank("Projeto", "Autor");
        Scene first = new Scene(null, "Cena 1", "", null);
        Scene second = new Scene(null, "Cena 2", "", null);
        Chapter chapter = new Chapter(null, "Capitulo 1", java.util.List.of(first, second));
        project.getChapters().add(chapter);

        ProjectStructureApplicationService.StructureSelection selection = service.removeScene(project, chapter, second);

        assertEquals(1, chapter.getScenes().size());
        assertSame(chapter, selection.chapter());
        assertSame(first, selection.scene());
    }

    @Test
    void removeOnlySceneIsRejected() {
        Project project = Project.blank("Projeto", "Autor");
        Scene only = new Scene(null, "Cena 1", "", null);
        Chapter chapter = new Chapter(null, "Capitulo 1", java.util.List.of(only));
        project.getChapters().add(chapter);

        assertThrows(IllegalStateException.class, () -> service.removeScene(project, chapter, only));
    }

    @Test
    void moveChapterReordersAndKeepsSelection() {
        Project project = Project.blank("Projeto", "Autor");
        Chapter first = new Chapter(null, "Capitulo 1", List.of(new Scene(null, "Cena 1", "", null)));
        Chapter second = new Chapter(null, "Capitulo 2", List.of(new Scene(null, "Cena 2", "", null)));
        project.getChapters().add(first);
        project.getChapters().add(second);

        ProjectStructureApplicationService.StructureSelection selection = service.moveChapter(project, first, 1);

        assertSame(second, project.getChapters().get(0));
        assertSame(first, project.getChapters().get(1));
        assertSame(first, selection.chapter());
        assertSame(first.getScenes().get(0), selection.scene());
    }

    @Test
    void moveFirstChapterUpIsRejected() {
        Project project = Project.blank("Projeto", "Autor");
        Chapter first = new Chapter(null, "Capitulo 1", List.of(new Scene(null, "Cena 1", "", null)));
        Chapter second = new Chapter(null, "Capitulo 2", List.of(new Scene(null, "Cena 2", "", null)));
        project.getChapters().add(first);
        project.getChapters().add(second);

        assertThrows(IllegalStateException.class, () -> service.moveChapter(project, first, -1));
    }

    @Test
    void moveSceneReordersAndKeepsSelection() {
        Project project = Project.blank("Projeto", "Autor");
        Scene first = new Scene(null, "Cena 1", "", null);
        Scene second = new Scene(null, "Cena 2", "", null);
        Chapter chapter = new Chapter(null, "Capitulo 1", List.of(first, second));
        project.getChapters().add(chapter);

        ProjectStructureApplicationService.StructureSelection selection = service.moveScene(project, chapter, first, 1);

        assertSame(second, chapter.getScenes().get(0));
        assertSame(first, chapter.getScenes().get(1));
        assertSame(chapter, selection.chapter());
        assertSame(first, selection.scene());
    }

    @Test
    void moveFirstSceneUpIsRejected() {
        Project project = Project.blank("Projeto", "Autor");
        Scene first = new Scene(null, "Cena 1", "", null);
        Scene second = new Scene(null, "Cena 2", "", null);
        Chapter chapter = new Chapter(null, "Capitulo 1", List.of(first, second));
        project.getChapters().add(chapter);

        assertThrows(IllegalStateException.class, () -> service.moveScene(project, chapter, first, -1));
    }
}
