package io.storyflame.core.tags;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import java.util.List;

public final class CharacterTagProfileSynchronizer {
    private static final String CHARACTER_TAG_MARKER_PREFIX = "__character_tag__:";

    private CharacterTagProfileSynchronizer() {
    }

    public static void synchronize(Project project) {
        if (project == null) {
            return;
        }
        ensureProfiles(project);
        removeOrphanOwnedTags(project);
        for (Character character : project.getCharacters()) {
            syncCharacterTag(project, character);
        }
    }

    public static CharacterTagProfile profileForCharacter(Project project, Character character) {
        if (project == null || character == null) {
            return null;
        }
        for (CharacterTagProfile profile : project.getCharacterTagProfiles()) {
            if (character.getId().equals(profile.getCharacterId())) {
                return profile;
            }
        }
        return null;
    }

    public static boolean isCharacterOwnedTag(NarrativeTag tag) {
        return tag != null && tag.description().startsWith(CHARACTER_TAG_MARKER_PREFIX);
    }

    public static String characterIdFromOwnedTag(NarrativeTag tag) {
        return isCharacterOwnedTag(tag) ? tag.description().substring(CHARACTER_TAG_MARKER_PREFIX.length()) : "";
    }

    private static void ensureProfiles(Project project) {
        for (Character character : project.getCharacters()) {
            boolean exists = project.getCharacterTagProfiles().stream()
                    .anyMatch(profile -> profile.getCharacterId().equals(character.getId()));
            if (!exists) {
                project.getCharacterTagProfiles().add(new CharacterTagProfile(character.getId(), "", List.of()));
            }
        }
        project.getCharacterTagProfiles().removeIf(profile ->
                project.getCharacters().stream().noneMatch(character -> character.getId().equals(profile.getCharacterId())));
    }

    private static void removeOrphanOwnedTags(Project project) {
        project.getNarrativeTags().removeIf(tag ->
                isCharacterOwnedTag(tag)
                        && project.getCharacters().stream()
                        .noneMatch(character -> character.getId().equals(characterIdFromOwnedTag(tag))));
    }

    private static void syncCharacterTag(Project project, Character character) {
        CharacterTagProfile profile = profileForCharacter(project, character);
        if (profile == null) {
            return;
        }
        NarrativeTag existingOwnedTag = findCharacterOwnedTag(project, character.getId());
        String desiredId = ensureUniqueCharacterTagId(project, suggestCharacterTagId(project, character), character.getId());
        String label = nonBlankOrFallback(character.getName(), "Personagem");
        NarrativeTag updatedTag = new NarrativeTag(desiredId, label, characterTagMarker(character.getId()), label);
        if (existingOwnedTag == null) {
            project.getNarrativeTags().add(updatedTag);
        } else {
            int tagIndex = project.getNarrativeTags().indexOf(existingOwnedTag);
            if (tagIndex >= 0) {
                project.getNarrativeTags().set(tagIndex, updatedTag);
            }
        }
        profile.setPrefix(desiredId);
    }

    private static NarrativeTag findCharacterOwnedTag(Project project, String characterId) {
        if (project == null || characterId == null || characterId.isBlank()) {
            return null;
        }
        for (NarrativeTag tag : project.getNarrativeTags()) {
            if (isCharacterOwnedTag(tag) && characterId.equals(characterIdFromOwnedTag(tag))) {
                return tag;
            }
        }
        return null;
    }

    private static String characterTagMarker(String characterId) {
        return CHARACTER_TAG_MARKER_PREFIX + characterId;
    }

    private static String suggestCharacterTagId(Project project, Character character) {
        if (character == null) {
            return "pers1";
        }
        String suggested = NarrativeTagIdPolicy.suggestFromText(character.getName());
        if (!suggested.isBlank()) {
            return suggested;
        }
        int characterIndex = Math.max(0, project.getCharacters().indexOf(character));
        return "pers" + (characterIndex + 1);
    }

    private static String ensureUniqueCharacterTagId(Project project, String baseId, String ownerCharacterId) {
        String candidate = NarrativeTagIdPolicy.normalizeExplicitId(baseId);
        if (candidate.isBlank()) {
            candidate = "pers1";
        }
        if (!NarrativeTagIdPolicy.isValid(candidate)) {
            candidate = NarrativeTagIdPolicy.suggestFromText(candidate);
        }
        if (candidate.isBlank()) {
            candidate = "pers1";
        }
        String uniqueCandidate = candidate;
        int suffix = 2;
        while (characterTagIdConflicts(project, uniqueCandidate, ownerCharacterId)) {
            uniqueCandidate = candidate.substring(0, 4) + suffix;
            suffix++;
        }
        return uniqueCandidate;
    }

    private static boolean characterTagIdConflicts(Project project, String candidate, String ownerCharacterId) {
        for (NarrativeTag tag : project.getNarrativeTags()) {
            if (!tag.id().equals(candidate)) {
                continue;
            }
            if (isCharacterOwnedTag(tag) && ownerCharacterId.equals(characterIdFromOwnedTag(tag))) {
                return false;
            }
            return true;
        }
        return false;
    }

    private static String nonBlankOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
