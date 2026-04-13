package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Project;
import io.storyflame.core.tags.TemplateExpansionMode;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopSummaryFormatterTest {
    @Test
    void explainsThatPreviewDoesNotMutateOriginalText() {
        Project project = Project.blank("Livro", "Marco");

        String summary = DesktopSummaryFormatter.format(
                project,
                Path.of("/tmp/livro.storyflame"),
                null,
                null,
                List.of(),
                TemplateExpansionMode.DRAFT
        );

        assertTrue(summary.contains("Este preview e calculado a partir da cena atual."));
        assertTrue(summary.contains("O texto original do editor nao e alterado."));
    }
}
