package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.validation.NarrativeIntegrityIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopNarrativeIntegrityFormatterTest {
    @Test
    void formatsIntegrityLabelByIssueCount() {
        assertEquals("0 referencias quebradas no POV", DesktopNarrativeIntegrityFormatter.labelText(List.of()));
        assertEquals(
                "1 referencia quebrada no POV",
                DesktopNarrativeIntegrityFormatter.labelText(List.of(issue("Inicio", "Porto")))
        );
        assertEquals(
                "2 referencias quebradas no POV",
                DesktopNarrativeIntegrityFormatter.labelText(List.of(issue("Inicio", "Porto"), issue("Fim", "Torre")))
        );
    }

    @Test
    void omitsTooltipWhenThereAreNoIssues() {
        assertNull(DesktopNarrativeIntegrityFormatter.tooltipText(List.of()));
    }

    @Test
    void formatsTooltipWithoutExposingTechnicalIds() {
        String tooltip = DesktopNarrativeIntegrityFormatter.tooltipText(List.of(issue("Inicio", "Torre")));

        assertTrue(tooltip.contains("Cena 'Torre'"));
        assertTrue(tooltip.contains("POV removido ou inexistente."));
    }

    private NarrativeIntegrityIssue issue(String chapterTitle, String sceneTitle) {
        return new NarrativeIntegrityIssue("chapter-1", chapterTitle, "scene-1", sceneTitle, "char-404");
    }
}
