package io.storyflame.core.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.util.List;
import org.junit.jupiter.api.Test;

class NarrativeIntegrityValidatorTest {
    @Test
    void returnsNoIssuesWhenAllPointOfViewReferencesExist() {
        Project project = Project.blank("Livro", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", "Capita"));
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Porto", "Texto", "char-1"))
        ));

        assertEquals(List.of(), NarrativeIntegrityValidator.findBrokenPointOfViewReferences(project));
    }

    @Test
    void reportsBrokenPointOfViewReference() {
        Project project = Project.blank("Livro", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", "Capita"));
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(
                        new Scene("scene-1", "Porto", "Texto", "char-1"),
                        new Scene("scene-2", "Torre", "Texto", "char-404")
                )
        ));

        List<NarrativeIntegrityIssue> issues = NarrativeIntegrityValidator.findBrokenPointOfViewReferences(project);

        assertEquals(1, issues.size());
        assertEquals("chapter-1", issues.get(0).chapterId());
        assertEquals("scene-2", issues.get(0).sceneId());
        assertEquals("char-404", issues.get(0).missingCharacterId());
    }

    @Test
    void ignoresBlankPointOfViewReference() {
        Project project = Project.blank("Livro", "Marco");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Porto", "Texto", "   "))
        ));

        assertEquals(List.of(), NarrativeIntegrityValidator.findBrokenPointOfViewReferences(project));
    }
}
