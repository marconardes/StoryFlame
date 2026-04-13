package io.storyflame.app.project;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.util.List;
import java.util.Objects;

public final class ProjectStructureApplicationService {
    public StructureSelection addChapter(Project project) {
        Objects.requireNonNull(project);
        Chapter chapter = new Chapter(
                null,
                "Capitulo " + (project.getChapters().size() + 1),
                List.of(new Scene(null, "Cena 1", "", null))
        );
        project.getChapters().add(chapter);
        project.touch();
        return new StructureSelection(chapter, chapter.getScenes().get(0));
    }

    public StructureSelection removeChapter(Project project, Chapter selectedChapter) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedChapter);
        if (project.getChapters().size() <= 1) {
            throw new IllegalStateException("Nao e possivel excluir o unico capitulo.");
        }
        int removedIndex = project.getChapters().indexOf(selectedChapter);
        if (removedIndex < 0) {
            throw new IllegalStateException("O capitulo selecionado nao foi encontrado.");
        }
        project.getChapters().remove(removedIndex);
        int nextIndex = Math.max(0, removedIndex - 1);
        Chapter chapter = project.getChapters().get(nextIndex);
        ensureChapterHasScene(chapter);
        project.touch();
        return new StructureSelection(chapter, chapter.getScenes().get(0));
    }

    public StructureSelection addScene(Project project, Chapter selectedChapter) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedChapter);
        Scene scene = new Scene(null, "Cena " + (selectedChapter.getScenes().size() + 1), "", null);
        selectedChapter.getScenes().add(scene);
        project.touch();
        return new StructureSelection(selectedChapter, scene);
    }

    public StructureSelection removeScene(Project project, Chapter selectedChapter, Scene selectedScene) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedChapter);
        Objects.requireNonNull(selectedScene);
        if (selectedChapter.getScenes().size() <= 1) {
            throw new IllegalStateException("Nao e possivel excluir a unica cena deste capitulo.");
        }
        int removedIndex = selectedChapter.getScenes().indexOf(selectedScene);
        if (removedIndex < 0) {
            throw new IllegalStateException("A cena selecionada nao foi encontrada.");
        }
        selectedChapter.getScenes().remove(removedIndex);
        int nextIndex = Math.max(0, removedIndex - 1);
        project.touch();
        return new StructureSelection(selectedChapter, selectedChapter.getScenes().get(nextIndex));
    }

    public StructureSelection moveChapter(Project project, Chapter selectedChapter, int offset) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedChapter);
        int currentIndex = project.getChapters().indexOf(selectedChapter);
        if (currentIndex < 0) {
            throw new IllegalStateException("O capitulo selecionado nao foi encontrado.");
        }
        int nextIndex = currentIndex + offset;
        if (nextIndex < 0) {
            throw new IllegalStateException("Nao e possivel mover o primeiro capitulo para cima.");
        }
        if (nextIndex >= project.getChapters().size()) {
            throw new IllegalStateException("Nao e possivel mover o ultimo capitulo para baixo.");
        }
        project.getChapters().remove(currentIndex);
        project.getChapters().add(nextIndex, selectedChapter);
        ensureChapterHasScene(selectedChapter);
        project.touch();
        return new StructureSelection(selectedChapter, selectedChapter.getScenes().get(0));
    }

    public StructureSelection moveScene(Project project, Chapter selectedChapter, Scene selectedScene, int offset) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedChapter);
        Objects.requireNonNull(selectedScene);
        int currentIndex = selectedChapter.getScenes().indexOf(selectedScene);
        if (currentIndex < 0) {
            throw new IllegalStateException("A cena selecionada nao foi encontrada.");
        }
        int nextIndex = currentIndex + offset;
        if (nextIndex < 0) {
            throw new IllegalStateException("Nao e possivel mover a primeira cena para cima.");
        }
        if (nextIndex >= selectedChapter.getScenes().size()) {
            throw new IllegalStateException("Nao e possivel mover a ultima cena para baixo.");
        }
        selectedChapter.getScenes().remove(currentIndex);
        selectedChapter.getScenes().add(nextIndex, selectedScene);
        project.touch();
        return new StructureSelection(selectedChapter, selectedScene);
    }

    private void ensureChapterHasScene(Chapter chapter) {
        if (chapter.getScenes().isEmpty()) {
            chapter.getScenes().add(new Scene(null, "Cena 1", "", null));
        }
    }

    public record StructureSelection(Chapter chapter, Scene scene) {
    }
}
