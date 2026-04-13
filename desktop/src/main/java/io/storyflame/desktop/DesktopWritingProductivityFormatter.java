package io.storyflame.desktop;

import io.storyflame.core.tags.TemplateExpansionMode;

final class DesktopWritingProductivityFormatter {
    private DesktopWritingProductivityFormatter() {
    }

    static String defaultTagHint() {
        return "Ctrl+Espaco sugere tags | Enter/Tab inserem | Passe o mouse ou posicione o cursor sobre uma tag para preview";
    }

    static String favoriteCountLabel(int count) {
        return count + (count == 1 ? " favorita nas sugestoes" : " favoritas nas sugestoes");
    }

    static String recentCountLabel(int count) {
        return count + (count == 1 ? " recente nas sugestoes" : " recentes nas sugestoes");
    }

    static String modeToggleLabel(TemplateExpansionMode mode) {
        return mode == TemplateExpansionMode.RENDER ? "Render" : "Rascunho";
    }
}
