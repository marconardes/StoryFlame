package io.storyflame.core.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import java.util.List;
import org.junit.jupiter.api.Test;

class TagLibraryValidatorTest {
    @Test
    void findsDuplicateTagIdsAndBrokenProfiles() {
        Project project = Project.blank("Livro", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", ""));
        project.getNarrativeTags().add(new NarrativeTag("lfp1", "Tag A", "", "a"));
        project.getNarrativeTags().add(new NarrativeTag("lfp1", "Tag B", "", "b"));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-404", "lia", List.of("lfp1", "missing")));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-1", "lia", List.of()));

        List<TagLibraryIssue> issues = TagLibraryValidator.validate(project);

        assertEquals(4, issues.size());
    }

    @Test
    void acceptsConsistentLibrary() {
        Project project = Project.blank("Livro", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", ""));
        project.getNarrativeTags().add(new NarrativeTag("lfp1", "Tag A", "", "a"));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-1", "lia", List.of("lfp1")));

        assertEquals(List.of(), TagLibraryValidator.validate(project));
    }
}
