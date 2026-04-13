package io.storyflame.core.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateExpansionEngineTest {
    @Test
    void keepsDraftModeUntouched() {
        TemplateExpansionResult result = TemplateExpansionEngine.expand(
                "Ela {fala1} e seguiu.",
                NarrativeTagCatalog.defaultCatalog(),
                TemplateExpansionMode.DRAFT
        );

        assertEquals("Ela {fala1} e seguiu.", result.text());
    }

    @Test
    void expandsMultipleTagsInRenderMode() {
        TemplateExpansionResult result = TemplateExpansionEngine.expand(
                "Ela {fala1}, respirou e {suss1}.",
                NarrativeTagCatalog.defaultCatalog(),
                TemplateExpansionMode.RENDER
        );

        assertEquals("Ela disse, respirou e sussurrou.", result.text());
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

    @Test
    void preservesSpacingBeforeMultiplePunctuationMarks() {
        NarrativeTagCatalog catalog = new NarrativeTagCatalog(List.of(
                new NarrativeTag("alerta", "Alerta", "Descricao", "gritou"),
                new NarrativeTag("duvida", "Duvida", "Descricao", "hesitou"),
                new NarrativeTag("pausa", "Pausa", "Descricao", "respirou fundo"),
                new NarrativeTag("explica", "Explica", "Descricao", "explicou")
        ));

        TemplateExpansionResult result = TemplateExpansionEngine.expand(
                "Ela {alerta} ! Depois {duvida} ? Entao {pausa} ; e {explica} : tudo mudou.",
                catalog,
                TemplateExpansionMode.RENDER
        );

        assertEquals("Ela gritou! Depois hesitou? Entao respirou fundo; e explicou: tudo mudou.", result.text());
        assertEquals(List.of("alerta", "duvida", "pausa", "explica"), result.expandedTagIds());
    }
}
