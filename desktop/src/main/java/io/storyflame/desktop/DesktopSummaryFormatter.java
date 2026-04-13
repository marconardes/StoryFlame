package io.storyflame.desktop;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.search.SearchMatch;
import io.storyflame.core.tags.TagLibraryValidator;
import io.storyflame.core.tags.TemplateExpansionMode;
import io.storyflame.core.tags.TemplateExpansionResult;
import io.storyflame.core.text.WordCount;
import io.storyflame.core.validation.NarrativeIntegrityValidator;
import java.nio.file.Path;
import java.util.List;

final class DesktopSummaryFormatter {
    private DesktopSummaryFormatter() {
    }

    static String format(
            Project project,
            Path currentPath,
            Chapter selectedChapter,
            Scene selectedScene,
            List<SearchMatch> searchMatches,
            TemplateExpansionMode mode
    ) {
        TemplateExpansionResult expansionResult = DesktopProjectInsights.currentSceneExpansion(project, selectedScene, mode);
        return """
                Arquivo: %s
                Projeto: %s
                Autor: %s
                Capitulo atual: %s
                Cena atual: %s
                POV da cena: %s
                Tags na cena: %s
                Modo de expansao: %s
                Tags expandidas: %s
                Tags invalidas: %s
                Inconsistencias de tags: %d
                Capitulos: %d
                Cenas no capitulo: %d
                Personagens: %d
                Resultados de busca: %d
                Referencias quebradas: %d
                Palavras na cena: %d
                Atualizado em: %s

                Este preview e calculado a partir da cena atual.
                O texto original do editor nao e alterado.

                Preview:
                %s
                """.formatted(
                currentPath,
                project.getTitle(),
                project.getAuthor(),
                selectedChapter == null ? "-" : selectedChapter.getTitle(),
                selectedScene == null ? "-" : selectedScene.getTitle(),
                DesktopProjectInsights.displayPointOfViewName(project, selectedScene),
                DesktopProjectInsights.formatCurrentSceneTagSummary(project, selectedScene),
                mode == TemplateExpansionMode.DRAFT ? "rascunho" : "render",
                expansionResult.expandedTagIds().isEmpty() ? "-" : String.join(", ", expansionResult.expandedTagIds()),
                expansionResult.invalidTagIds().isEmpty() ? "-" : String.join(", ", expansionResult.invalidTagIds()),
                TagLibraryValidator.validate(project).size(),
                project.getChapters().size(),
                selectedChapter == null ? 0 : selectedChapter.getScenes().size(),
                project.getCharacters().size(),
                searchMatches.size(),
                NarrativeIntegrityValidator.findBrokenPointOfViewReferences(project).size(),
                selectedScene == null ? 0 : WordCount.count(selectedScene.getContent()),
                project.getUpdatedAt(),
                expansionResult.text()
        );
    }
}
