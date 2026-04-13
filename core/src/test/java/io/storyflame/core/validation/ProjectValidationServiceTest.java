package io.storyflame.core.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProjectValidationServiceTest {
    @Test
    void returnsWarningsForArchiveSave() {
        ProjectValidationResult result = ProjectValidationService.validate(invalidProject(), ProjectValidationOperation.SAVE_ARCHIVE);

        assertTrue(result.hasIssues());
        assertFalse(result.hasBlockingIssues());
        assertEquals(3, result.warningIssues().size());
        assertEquals(Set.of(
                "broken-point-of-view",
                "duplicate-tag-id",
                "missing-character"
        ), result.warningIssues().stream().map(ProjectValidationIssue::code).collect(java.util.stream.Collectors.toSet()));
        assertTrue(result.issues().stream().allMatch(issue -> issue.severity() == ProjectValidationSeverity.WARNING));
    }

    @Test
    void returnsBlockingIssuesForPublicationExport() {
        ProjectValidationResult result = ProjectValidationService.validate(invalidProject(), ProjectValidationOperation.EXPORT_PUBLICATION);

        assertTrue(result.hasBlockingIssues());
        assertEquals(1, result.blockingIssues().size());
        assertEquals("duplicate-tag-id", result.blockingIssues().get(0).code());
        assertEquals(ProjectValidationSeverity.BLOCKING, result.blockingIssues().get(0).severity());
        assertEquals(2, result.warningIssues().size());
        assertEquals(Set.of(
                "broken-point-of-view",
                "missing-character"
        ), result.warningIssues().stream().map(ProjectValidationIssue::code).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void returnsWarningsForArchiveExport() {
        ProjectValidationResult result = ProjectValidationService.validate(invalidProject(), ProjectValidationOperation.EXPORT_ARCHIVE);

        assertTrue(result.hasIssues());
        assertFalse(result.hasBlockingIssues());
        assertEquals(3, result.warningIssues().size());
        assertEquals(Set.of(
                "broken-point-of-view",
                "duplicate-tag-id",
                "missing-character"
        ), result.warningIssues().stream().map(ProjectValidationIssue::code).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void returnsNoIssuesForCleanProject() {
        ProjectValidationResult result = ProjectValidationService.validate(cleanProject(), ProjectValidationOperation.EXPORT_PUBLICATION);

        assertFalse(result.hasIssues());
        assertTrue(result.warningIssues().isEmpty());
        assertTrue(result.blockingIssues().isEmpty());
    }

    private Project invalidProject() {
        Project project = Project.blank("Livro", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", ""));
        project.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag A", "", "a"));
        project.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag B", "", "b"));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-404", "lia", List.of("tag-1")));
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Cena", "Texto", "char-404"))
        ));
        return project;
    }

    private Project cleanProject() {
        Project project = Project.blank("Livro limpo", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", ""));
        project.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag A", "", "a"));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-1", "lia", List.of("tag-1")));
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Cena", "Texto", "char-1"))
        ));
        return project;
    }
}
