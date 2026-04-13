package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Scene;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopOutlineFormatterTest {
    @Test
    void buildsSceneOutlineWithSynopsisAndPointOfView() {
        Scene scene = new Scene("scene-1", "Chegada", "A equipe pisa no porto sob tensao e silencios.", "Texto completo.", "char-1");

        String label = DesktopOutlineFormatter.sceneLabel(0, scene, "Cena", "Lia");

        assertTrue(label.contains("1. Chegada"));
        assertTrue(label.contains("A equipe pisa no porto"));
        assertTrue(label.contains("POV: Lia"));
    }

    @Test
    void buildsChapterOutlineWithSceneCount() {
        Chapter chapter = new Chapter("chapter-1", "Abertura", List.of(
                new Scene("scene-1", "Cena 1", "Resumo", "Texto", null),
                new Scene("scene-2", "Cena 2", "Resumo", "Texto", null)
        ));

        String label = DesktopOutlineFormatter.chapterLabel(0, chapter, "Capitulo");

        assertTrue(label.contains("1. Abertura"));
        assertTrue(label.contains("2 cenas"));
    }
}
