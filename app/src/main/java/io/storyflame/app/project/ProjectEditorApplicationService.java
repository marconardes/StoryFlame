package io.storyflame.app.project;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.util.Objects;

public final class ProjectEditorApplicationService {
    public void updateProjectMetadata(Project project, String title, String author) {
        Objects.requireNonNull(project);
        project.setTitle(title);
        project.setAuthor(author);
    }

    public void updateChapterTitle(Project project, Chapter chapter, String title) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(chapter);
        chapter.setTitle(title);
        project.touch();
    }

    public void updateSceneDraft(Project project, Scene scene, String title, String synopsis, String content) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(scene);
        scene.setTitle(title);
        scene.setSynopsis(synopsis);
        scene.setContent(content);
        project.touch();
    }

    public void updateSceneTitle(Project project, Scene scene, String title) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(scene);
        scene.setTitle(title);
        project.touch();
    }

    public void updateSceneSynopsis(Project project, Scene scene, String synopsis) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(scene);
        scene.setSynopsis(synopsis);
        project.touch();
    }

    public void updateSceneContent(Project project, Scene scene, String content) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(scene);
        scene.setContent(content);
        project.touch();
    }
}
