package io.storyflame.core.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import java.util.List;
import org.junit.jupiter.api.Test;

class CharacterDirectoryTest {
    @Test
    void findsCharacterById() {
        Project project = sampleProject();

        Character result = CharacterDirectory.findById(project, "char-2");

        assertSame(project.getCharacters().get(1), result);
    }

    @Test
    void returnsNullWhenCharacterIdDoesNotExist() {
        Project project = sampleProject();

        assertNull(CharacterDirectory.findById(project, "char-404"));
    }

    @Test
    void returnsAllCharactersWhenQueryIsBlank() {
        Project project = sampleProject();

        assertEquals(project.getCharacters(), CharacterDirectory.search(project, "   "));
    }

    @Test
    void searchesByNameAndDescriptionIgnoringAccents() {
        Project project = sampleProject();

        List<Character> byName = CharacterDirectory.search(project, "noel");
        List<Character> byDescription = CharacterDirectory.search(project, "mecanica");

        assertEquals(List.of("Noel"), byName.stream().map(Character::getName).toList());
        assertEquals(List.of("Lia"), byDescription.stream().map(Character::getName).toList());
    }

    private Project sampleProject() {
        Project project = Project.blank("Livro", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", "Capita mecanica"));
        project.getCharacters().add(new Character("char-2", "Noel", "Piloto observador"));
        return project;
    }
}
