package io.storyflame.desktop;

import io.storyflame.core.character.CharacterDirectory;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTagCatalog;
import io.storyflame.core.tags.NarrativeTagParser;
import io.storyflame.core.tags.ParsedNarrativeTag;
import io.storyflame.core.tags.TagLibraryIssue;
import io.storyflame.core.tags.TagLibraryValidator;
import io.storyflame.core.tags.TemplateExpansionEngine;
import io.storyflame.core.tags.TemplateExpansionMode;
import io.storyflame.core.tags.TemplateExpansionResult;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

final class DesktopProjectInsights {
    private DesktopProjectInsights() {
    }

    static String displayCharacterName(Project project, Scene selectedScene, Character character) {
        if (character == null) {
            return "Personagem";
        }
        int sceneCount = countScenesForCharacter(project, character);
        String suffix = sceneCount == 1 ? "1 cena" : sceneCount + " cenas";
        String povMarker = selectedScene != null && character.getId().equals(selectedScene.getPointOfViewCharacterId()) ? " | POV atual" : "";
        return displayTitle(character.getName(), "Personagem") + " | " + suffix + povMarker;
    }

    static String displayPointOfViewName(Project project, Scene selectedScene) {
        Character povCharacter = findCharacterById(project, selectedScene == null ? null : selectedScene.getPointOfViewCharacterId());
        if (povCharacter != null) {
            return displayCharacterName(project, selectedScene, povCharacter);
        }
        if (selectedScene != null && selectedScene.getPointOfViewCharacterId() != null && !selectedScene.getPointOfViewCharacterId().isBlank()) {
            return "personagem ausente";
        }
        return "sem personagem";
    }

    static Character findCharacterById(Project project, String characterId) {
        return CharacterDirectory.findById(project, characterId);
    }

    static int countScenesForCharacter(Project project, Character character) {
        if (project == null || character == null) {
            return 0;
        }
        int count = 0;
        for (Chapter chapter : project.getChapters()) {
            for (Scene scene : chapter.getScenes()) {
                if (character.getId().equals(scene.getPointOfViewCharacterId())) {
                    count++;
                }
            }
        }
        return count;
    }

    static boolean isSelectedCharacterPointOfView(Scene selectedScene, Character selectedCharacter) {
        return selectedCharacter != null
                && selectedScene != null
                && selectedCharacter.getId().equals(selectedScene.getPointOfViewCharacterId());
    }

    static NarrativeTagCatalog currentNarrativeTagCatalog(Project project) {
        if (project == null || project.getNarrativeTags().isEmpty()) {
            return NarrativeTagCatalog.defaultCatalog();
        }
        return new NarrativeTagCatalog(project.getNarrativeTags());
    }

    static List<ParsedNarrativeTag> currentSceneTags(Project project, Scene selectedScene) {
        if (selectedScene == null) {
            return List.of();
        }
        return NarrativeTagParser.parse(selectedScene.getContent(), currentNarrativeTagCatalog(project));
    }

    static TemplateExpansionResult currentSceneExpansion(Project project, Scene selectedScene, TemplateExpansionMode mode) {
        if (selectedScene == null) {
            return new TemplateExpansionResult("", List.of(), List.of());
        }
        return TemplateExpansionEngine.expand(selectedScene.getContent(), currentNarrativeTagCatalog(project), mode);
    }

    static String formatCurrentSceneTagSummary(Project project, Scene selectedScene) {
        List<ParsedNarrativeTag> parsedTags = currentSceneTags(project, selectedScene);
        if (parsedTags.isEmpty()) {
            return "nenhuma";
        }
        List<String> validTagIds = parsedTags.stream()
                .filter(ParsedNarrativeTag::valid)
                .map(ParsedNarrativeTag::tagId)
                .distinct()
                .toList();
        List<String> invalidTagIds = parsedTags.stream()
                .filter(tag -> !tag.valid())
                .map(ParsedNarrativeTag::tagId)
                .distinct()
                .toList();
        if (invalidTagIds.isEmpty()) {
            return String.join(", ", validTagIds);
        }
        if (validTagIds.isEmpty()) {
            return "invalidas: " + String.join(", ", invalidTagIds);
        }
        return "validas: " + String.join(", ", validTagIds) + " | invalidas: " + String.join(", ", invalidTagIds);
    }

    static List<String> currentSceneLinkedCharacterNames(Project project, Scene selectedScene) {
        if (project == null || selectedScene == null) {
            return List.of();
        }
        LinkedHashSet<String> names = new LinkedHashSet<>();
        Character povCharacter = findCharacterById(project, selectedScene.getPointOfViewCharacterId());
        if (povCharacter != null) {
            names.add(displayTitle(povCharacter.getName(), "Personagem"));
        }

        List<String> tagIds = currentSceneTags(project, selectedScene).stream()
                .filter(ParsedNarrativeTag::valid)
                .map(ParsedNarrativeTag::tagId)
                .distinct()
                .toList();
        if (tagIds.isEmpty() || project.getCharacterTagProfiles().isEmpty()) {
            return new ArrayList<>(names);
        }
        for (CharacterTagProfile profile : project.getCharacterTagProfiles()) {
            boolean matches = profile.getPreferredTagIds().stream().anyMatch(tagIds::contains);
            if (!matches) {
                continue;
            }
            Character character = findCharacterById(project, profile.getCharacterId());
            if (character != null) {
                names.add(displayTitle(character.getName(), "Personagem"));
            }
        }
        return new ArrayList<>(names);
    }

    static String formatProfileLabel(Project project, Scene selectedScene, CharacterTagProfile profile) {
        Character character = findCharacterById(project, profile.getCharacterId());
        String name = character == null ? profile.getCharacterId() : displayCharacterName(project, selectedScene, character);
        String prefix = profile.getPrefix() == null || profile.getPrefix().isBlank() ? "sem prefixo" : profile.getPrefix();
        return name + " | " + prefix;
    }

    static int countTagUsage(Project project, String tagId) {
        if (project == null || tagId == null || tagId.isBlank()) {
            return 0;
        }
        int count = 0;
        for (Chapter chapter : project.getChapters()) {
            for (Scene scene : chapter.getScenes()) {
                for (ParsedNarrativeTag parsedTag : NarrativeTagParser.parse(scene.getContent(), currentNarrativeTagCatalog(project))) {
                    if (parsedTag.tagId().equals(tagId)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    static boolean profileHasIssues(Project project, CharacterTagProfile profile) {
        if (profile == null || project == null) {
            return false;
        }
        for (TagLibraryIssue issue : TagLibraryValidator.validate(project)) {
            if (issue.message().contains(profile.getCharacterId()) || issue.message().contains(profile.getPrefix())) {
                return true;
            }
        }
        return false;
    }

    static String profileStatusText(Project project, CharacterTagProfile profile) {
        int tagCount = profile.getPreferredTagIds().size();
        String usage = tagCount == 1 ? "1 tag associada" : tagCount + " tags associadas";
        if (profileHasIssues(project, profile)) {
            return usage + " | revisar";
        }
        return usage;
    }

    static String formatCharacterTagSummary(CharacterTagProfile profile) {
        if (profile == null) {
            return "nenhuma";
        }
        List<String> tags = new java.util.ArrayList<>();
        if (profile.getPrefix() != null && !profile.getPrefix().isBlank()) {
            tags.add("{" + profile.getPrefix() + "}");
        }
        for (String tagId : profile.getPreferredTagIds()) {
            if (tagId != null && !tagId.isBlank()) {
                tags.add("{" + tagId + "}");
            }
        }
        return tags.isEmpty() ? "nenhuma" : String.join(", ", tags);
    }

    private static String displayTitle(String value, String fallbackPrefix) {
        if (value == null || value.isBlank()) {
            return fallbackPrefix;
        }
        return value;
    }
}
