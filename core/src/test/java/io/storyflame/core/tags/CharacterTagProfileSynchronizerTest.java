package io.storyflame.core.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import java.util.List;
import org.junit.jupiter.api.Test;

class CharacterTagProfileSynchronizerTest {
    @Test
    void createsMissingProfilesAndOwnedTagsForCharacters() {
        Project project = Project.blank("Livro", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia Mora", ""));

        CharacterTagProfileSynchronizer.synchronize(project);

        assertEquals(1, project.getCharacterTagProfiles().size());
        CharacterTagProfile profile = project.getCharacterTagProfiles().get(0);
        assertEquals("char-1", profile.getCharacterId());
        assertNotNull(profile.getPrefix());
        assertFalse(profile.getPrefix().isBlank());
        assertEquals(1, project.getNarrativeTags().size());
        assertEquals(profile.getPrefix(), project.getNarrativeTags().get(0).id());
        assertEquals("Lia Mora", project.getNarrativeTags().get(0).label());
    }

    @Test
    void removesOrphanProfilesAndOwnedTags() {
        Project project = Project.blank("Livro", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", ""));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-404", "ghost", List.of("tag-a")));
        project.getNarrativeTags().add(new NarrativeTag("ghost", "Ghost", "__character_tag__:char-404", "Ghost"));

        CharacterTagProfileSynchronizer.synchronize(project);

        assertEquals(1, project.getCharacterTagProfiles().size());
        assertEquals("char-1", project.getCharacterTagProfiles().get(0).getCharacterId());
        assertEquals(1, project.getNarrativeTags().size());
        assertEquals(project.getCharacterTagProfiles().get(0).getPrefix(), project.getNarrativeTags().get(0).id());
    }
}
