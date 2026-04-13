package io.storyflame.app.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.CharacterTagProfileSynchronizer;
import io.storyflame.core.tags.NarrativeTag;
import org.junit.jupiter.api.Test;

class ProjectCharacterApplicationServiceTest {
    private final ProjectCharacterApplicationService service = new ProjectCharacterApplicationService();

    @Test
    void createCharacterAddsCharacterAndSynchronizesProfiles() {
        Project project = Project.blank("Projeto", "Autor");

        Character character = service.createCharacter(project, "Lia", "Desc");

        assertEquals(1, project.getCharacters().size());
        assertEquals("Lia", character.getName());
        assertNotNull(CharacterTagProfileSynchronizer.profileForCharacter(project, character));
    }

    @Test
    void updateCharacterChangesFields() {
        Project project = Project.blank("Projeto", "Autor");
        Character character = service.createCharacter(project, "Lia", "Desc");

        service.updateCharacter(project, character, "Mia", "Nova");

        assertEquals("Mia", character.getName());
        assertEquals("Nova", character.getDescription());
    }

    @Test
    void deleteCharacterReturnsPreviousSelection() {
        Project project = Project.blank("Projeto", "Autor");
        Character first = service.createCharacter(project, "A", "");
        Character second = service.createCharacter(project, "B", "");

        Character selected = service.deleteCharacter(project, second);

        assertEquals(1, project.getCharacters().size());
        assertEquals(first, selected);
    }

    @Test
    void addAndRemoveTagFromCharacterProfile() {
        Project project = Project.blank("Projeto", "Autor");
        Character character = service.createCharacter(project, "A", "");
        NarrativeTag tag = new NarrativeTag("test1", "Tag", "", "tpl");

        CharacterTagProfile profile = service.addTagToCharacter(project, character, tag);
        assertNotNull(profile);
        assertFalse(profile.getPreferredTagIds().isEmpty());

        profile = service.removeTagFromCharacter(project, character, tag);
        assertNotNull(profile);
        assertTrue(profile.getPreferredTagIds().isEmpty());
    }

    @Test
    void assignPointOfViewUsesCharacterId() {
        Scene scene = new Scene(null, "Cena", "", null);
        Character character = new Character(null, "A", "");

        service.assignPointOfView(scene, character);

        assertEquals(character.getId(), scene.getPointOfViewCharacterId());
    }

    @Test
    void clearPointOfViewRemovesCharacterId() {
        Scene scene = new Scene(null, "Cena", "", null);
        Character character = new Character(null, "A", "");
        service.assignPointOfView(scene, character);

        service.clearPointOfView(scene);

        assertNull(scene.getPointOfViewCharacterId());
    }

    private static void assertTrue(boolean value) {
        org.junit.jupiter.api.Assertions.assertTrue(value);
    }
}
