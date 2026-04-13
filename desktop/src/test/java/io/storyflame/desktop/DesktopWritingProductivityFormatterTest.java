package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.tags.TemplateExpansionMode;
import org.junit.jupiter.api.Test;

class DesktopWritingProductivityFormatterTest {
    @Test
    void formatsPersistentTagHint() {
        String hint = DesktopWritingProductivityFormatter.defaultTagHint();

        assertTrue(hint.contains("Ctrl+Espaco"));
        assertTrue(hint.contains("Enter/Tab"));
        assertTrue(hint.contains("preview"));
    }

    @Test
    void formatsProductivityCounters() {
        assertEquals("1 favorita nas sugestoes", DesktopWritingProductivityFormatter.favoriteCountLabel(1));
        assertEquals("2 favoritas nas sugestoes", DesktopWritingProductivityFormatter.favoriteCountLabel(2));
        assertEquals("1 recente nas sugestoes", DesktopWritingProductivityFormatter.recentCountLabel(1));
        assertEquals("3 recentes nas sugestoes", DesktopWritingProductivityFormatter.recentCountLabel(3));
    }

    @Test
    void formatsModeToggleLabel() {
        assertEquals("Rascunho", DesktopWritingProductivityFormatter.modeToggleLabel(TemplateExpansionMode.DRAFT));
        assertEquals("Render", DesktopWritingProductivityFormatter.modeToggleLabel(TemplateExpansionMode.RENDER));
    }
}
