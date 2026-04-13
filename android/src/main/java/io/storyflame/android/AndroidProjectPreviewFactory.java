package io.storyflame.android;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.util.List;

final class AndroidProjectPreviewFactory {
    private AndroidProjectPreviewFactory() {
    }

    static Project sampleProject() {
        Project project = Project.blank("StoryFlame Android", "Modo de apoio");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo 1",
                List.of(
                        new Scene("scene-1", "Cena 1", "Consulta leve do manuscrito no Android.", null),
                        new Scene("scene-2", "Cena 2", "O nucleo compartilhado continua portavel.", null)
                )
        ));
        return project;
    }
}
