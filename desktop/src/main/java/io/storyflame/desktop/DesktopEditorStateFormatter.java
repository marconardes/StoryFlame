package io.storyflame.desktop;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Scene;
import io.storyflame.core.text.WordCount;

final class DesktopEditorStateFormatter {
    private DesktopEditorStateFormatter() {
    }

    static String emptyEditorMessage() {
        return "Selecione ou crie uma cena para comecar a escrever.";
    }

    static String contextLabel(Chapter chapter, Scene scene) {
        if (chapter == null || scene == null) {
            return "Nenhuma cena selecionada. Use Estrutura para escolher ou criar uma cena.";
        }
        return displayTitle(chapter.getTitle(), "Capitulo") + " / " + displayTitle(scene.getTitle(), "Cena");
    }

    static String wordCountLabel(Scene scene, String currentText) {
        if (scene == null) {
            return "Cena atual: 0 palavras";
        }
        return "Cena atual: " + WordCount.count(currentText) + " palavras";
    }

    private static String displayTitle(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
