package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.tags.TagLibraryIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopTagLibraryIssueFormatterTest {
    @Test
    void formatsIssueCountLabel() {
        assertEquals("Biblioteca sem inconsistencias", DesktopTagLibraryIssueFormatter.labelText(List.of()));
        assertEquals(
                "1 inconsistencia na biblioteca",
                DesktopTagLibraryIssueFormatter.labelText(List.of(issue("duplicate-tag-id", "Tag duplicada: lfp1")))
        );
        assertEquals(
                "2 inconsistencias na biblioteca",
                DesktopTagLibraryIssueFormatter.labelText(List.of(
                        issue("duplicate-tag-id", "Tag duplicada: lfp1"),
                        issue("missing-preferred-tag", "Perfil referencia tag inexistente: missing")
                ))
        );
    }

    @Test
    void omitsTooltipWhenThereAreNoIssues() {
        assertNull(DesktopTagLibraryIssueFormatter.tooltipText(List.of()));
    }

    @Test
    void formatsTooltipWithIssueMessages() {
        String tooltip = DesktopTagLibraryIssueFormatter.tooltipText(List.of(
                issue("duplicate-tag-id", "Tag duplicada: lfp1"),
                issue("missing-preferred-tag", "Perfil referencia tag inexistente: missing")
        ));

        assertTrue(tooltip.contains("Tag duplicada: lfp1"));
        assertTrue(tooltip.contains("Perfil referencia tag inexistente: missing"));
    }

    private TagLibraryIssue issue(String code, String message) {
        return new TagLibraryIssue(code, message);
    }
}
