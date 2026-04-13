package io.storyflame.app.project;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.CharacterTagProfileSynchronizer;
import io.storyflame.core.tags.NarrativeTag;
import java.util.Objects;

public final class ProjectCharacterApplicationService {
    public Character updateCharacter(Project project, Character character, String name, String description) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(character);
        character.setName(name);
        character.setDescription(description);
        CharacterTagProfileSynchronizer.synchronize(project);
        project.touch();
        return character;
    }

    public Character createCharacter(Project project, String name, String description) {
        Objects.requireNonNull(project);
        Character character = new Character(null, name, description);
        project.getCharacters().add(character);
        CharacterTagProfileSynchronizer.synchronize(project);
        project.touch();
        return character;
    }

    public Character deleteCharacter(Project project, Character selectedCharacter) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedCharacter);
        int removedIndex = project.getCharacters().indexOf(selectedCharacter);
        if (removedIndex < 0) {
            return selectedCharacter;
        }
        project.getCharacters().remove(removedIndex);
        CharacterTagProfileSynchronizer.synchronize(project);
        project.touch();
        return project.getCharacters().isEmpty()
                ? null
                : project.getCharacters().get(Math.max(0, removedIndex - 1));
    }

    public CharacterTagProfile addTagToCharacter(Project project, Character selectedCharacter, NarrativeTag selectedTag) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedCharacter);
        Objects.requireNonNull(selectedTag);
        CharacterTagProfile profile = CharacterTagProfileSynchronizer.profileForCharacter(project, selectedCharacter);
        if (profile == null) {
            return null;
        }
        if (!profile.getPreferredTagIds().contains(selectedTag.id())) {
            profile.getPreferredTagIds().add(selectedTag.id());
            project.touch();
        }
        return profile;
    }

    public CharacterTagProfile removeTagFromCharacter(Project project, Character selectedCharacter, NarrativeTag selectedTag) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedCharacter);
        Objects.requireNonNull(selectedTag);
        CharacterTagProfile profile = CharacterTagProfileSynchronizer.profileForCharacter(project, selectedCharacter);
        if (profile == null) {
            return null;
        }
        profile.getPreferredTagIds().removeIf(tagId -> tagId.equals(selectedTag.id()));
        project.touch();
        return profile;
    }

    public void assignPointOfView(Scene selectedScene, Character selectedCharacter) {
        Objects.requireNonNull(selectedScene);
        Objects.requireNonNull(selectedCharacter);
        selectedScene.setPointOfViewCharacterId(selectedCharacter.getId());
    }

    public void clearPointOfView(Scene selectedScene) {
        Objects.requireNonNull(selectedScene);
        selectedScene.setPointOfViewCharacterId(null);
    }
}
