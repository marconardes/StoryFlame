package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Scene;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopEditorStateFormatterTest {
    @Test
    void formatsEmptyEditorState() {
        assertEquals(
                "Selecione ou crie uma cena para comecar a escrever.",
                DesktopEditorStateFormatter.emptyEditorMessage()
        );
        assertEquals(
                "Nenhuma cena selecionada. Use Estrutura para escolher ou criar uma cena.",
                DesktopEditorStateFormatter.contextLabel(null, null)
        );
        assertEquals("Cena atual: 0 palavras", DesktopEditorStateFormatter.wordCountLabel(null, ""));
    }

    @Test
    void formatsActiveSceneState() {
        Chapter chapter = new Chapter("ch-1", "Capitulo 1", List.of());
        Scene scene = new Scene("sc-1", "Cena 1", "uma duas tres", null);

        assertEquals("Capitulo 1 / Cena 1", DesktopEditorStateFormatter.contextLabel(chapter, scene));
        assertEquals("Cena atual: 3 palavras", DesktopEditorStateFormatter.wordCountLabel(scene, scene.getContent()));
    }
}
