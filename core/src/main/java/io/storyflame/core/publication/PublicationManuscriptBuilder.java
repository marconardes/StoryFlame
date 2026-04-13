package io.storyflame.core.publication;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.NarrativeTagCatalog;
import io.storyflame.core.tags.TemplateExpansionEngine;
import io.storyflame.core.tags.TemplateExpansionMode;
import java.util.ArrayList;
import java.util.List;

public final class PublicationManuscriptBuilder {
    private PublicationManuscriptBuilder() {
    }

    public static PublicationManuscript build(Project project) {
        NarrativeTagCatalog catalog = project.getNarrativeTags().isEmpty()
                ? NarrativeTagCatalog.defaultCatalog()
                : new NarrativeTagCatalog(project.getNarrativeTags());
        List<PublicationChapter> chapters = new ArrayList<>();
        for (Chapter chapter : project.getChapters()) {
            List<PublicationScene> scenes = new ArrayList<>();
            for (Scene scene : chapter.getScenes()) {
                String renderedContent = TemplateExpansionEngine.expand(
                        scene.getContent(),
                        catalog,
                        TemplateExpansionMode.RENDER
                ).text();
                scenes.add(new PublicationScene(scene.getTitle(), renderedContent));
            }
            chapters.add(new PublicationChapter(chapter.getTitle(), List.copyOf(scenes)));
        }
        return new PublicationManuscript(project.getTitle(), project.getAuthor(), List.copyOf(chapters));
    }
}
