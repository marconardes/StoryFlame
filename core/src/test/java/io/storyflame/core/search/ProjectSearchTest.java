package io.storyflame.core.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProjectSearchTest {
    @Test
    void findsMatchesInChapterTitleSceneTitleAndSceneContent() {
        Project project = Project.blank("Livro", "Marco");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "A fuga",
                List.of(
                        new Scene("scene-1", "Porto", "A multidão correu para o portão.", null),
                        new Scene("scene-2", "A fuga continua", "Ninguém olhou para trás.", null)
                )
        ));

        List<SearchMatch> matches = ProjectSearch.search(project, "fuga");

        assertEquals(2, matches.size());
        assertEquals(SearchTarget.CHAPTER, matches.get(0).target());
        assertEquals(SearchTarget.SCENE_TITLE, matches.get(1).target());
    }

    @Test
    void findsMatchesInSceneContent() {
        Project project = Project.blank("Livro", "Marco");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Porto", "A guardiã encontrou o mapa secreto.", null))
        ));

        List<SearchMatch> matches = ProjectSearch.search(project, "mapa");

        assertEquals(1, matches.size());
        assertEquals(SearchTarget.SCENE_CONTENT, matches.get(0).target());
        assertEquals(0, matches.get(0).chapterIndex());
        assertEquals(0, matches.get(0).sceneIndex());
    }

    @Test
    void ignoresAccentsDuringSearch() {
        Project project = Project.blank("Livro", "Marco");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo de introducao",
                List.of(new Scene("scene-1", "Ação inicial", "O heroi chegou ao portão.", null))
        ));

        List<SearchMatch> chapterMatches = ProjectSearch.search(project, "capítulo");
        List<SearchMatch> sceneMatches = ProjectSearch.search(project, "acao");

        assertEquals(1, chapterMatches.size());
        assertEquals(SearchTarget.CHAPTER, chapterMatches.get(0).target());
        assertEquals(1, sceneMatches.size());
        assertEquals(SearchTarget.SCENE_TITLE, sceneMatches.get(0).target());
    }

    @Test
    void returnsNoMatchesForBlankQuery() {
        Project project = Project.blank("Livro", "Marco");

        assertEquals(List.of(), ProjectSearch.search(project, "   "));
    }
}
