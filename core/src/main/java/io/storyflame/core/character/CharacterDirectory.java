package io.storyflame.core.character;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public final class CharacterDirectory {
    private CharacterDirectory() {
    }

    public static Character findById(Project project, String characterId) {
        if (project == null || characterId == null || characterId.isBlank()) {
            return null;
        }
        for (Character character : project.getCharacters()) {
            if (characterId.equals(character.getId())) {
                return character;
            }
        }
        return null;
    }

    public static List<Character> search(Project project, String query) {
        if (project == null) {
            return List.of();
        }
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return new ArrayList<>(project.getCharacters());
        }
        List<Character> matches = new ArrayList<>();
        for (Character character : project.getCharacters()) {
            String haystack = normalize(character.getName() + "\n" + character.getDescription());
            if (haystack.contains(normalizedQuery)) {
                matches.add(character);
            }
        }
        return matches;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase();
    }
}
