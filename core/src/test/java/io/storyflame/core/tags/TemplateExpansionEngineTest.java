package io.storyflame.core.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateExpansionEngineTest {
    @Test
    void keepsDraftModeUntouched() {
        TemplateExpansionResult result = TemplateExpansionEngine.expand(
                "Ela {lfp1} e seguiu.",
                NarrativeTagCatalog.defaultCatalog(),
                TemplateExpansionMode.DRAFT
        );

        assertEquals("Ela {lfp1} e seguiu.", result.text());
    }

    @Test
    void expandsMultipleTagsInRenderMode() {
        TemplateExpansionResult result = TemplateExpansionEngine.expand(
                "Ela {lfp1}, respirou e {emo1}.",
                NarrativeTagCatalog.defaultCatalog(),
                TemplateExpansionMode.RENDER
        );

        assertEquals("Ela leu o rosto com precisão, respirou e sentiu a emoção crescer no peito.", result.text());
        assertEquals(2, result.expandedTagIds().size());
    }

    @Test
    void keepsInvalidTagsVisibleInRenderMode() {
        TemplateExpansionResult result = TemplateExpansionEngine.expand(
                "Teste {missing1}.",
                NarrativeTagCatalog.defaultCatalog(),
                TemplateExpansionMode.RENDER
        );

        assertEquals("Teste {missing1}.", result.text());
        assertTrue(result.invalidTagIds().contains("missing1"));
    }

    @Test
    void usesCatalogTemplateInsteadOfHardcodedSwitch() {
        NarrativeTagCatalog catalog = new NarrativeTagCatalog(List.of(
                new NarrativeTag("custom1", "Custom", "Descricao", "observou o silêncio ao redor")
        ));

        TemplateExpansionResult result = TemplateExpansionEngine.expand(
                "Ela {custom1}.",
                catalog,
                TemplateExpansionMode.RENDER
        );

        assertEquals("Ela observou o silêncio ao redor.", result.text());
    }

    @Test
    void fallsBackToLabelWhenTemplateIsMissing() {
        NarrativeTagCatalog catalog = new NarrativeTagCatalog(List.of(
                new NarrativeTag("custom2", "Foco Tenso", "Descricao", "")
        ));

        TemplateExpansionResult result = TemplateExpansionEngine.expand(
                "Clima {custom2}.",
                catalog,
                TemplateExpansionMode.RENDER
        );

        assertEquals("Clima foco tenso.", result.text());
    }
}
