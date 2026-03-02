package io.storyflame.core.tags;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TagLibraryValidator {
    private TagLibraryValidator() {
    }

    public static List<TagLibraryIssue> validate(Project project) {
        if (project == null) {
            return List.of();
        }

        List<TagLibraryIssue> issues = new ArrayList<>();
        Set<String> tagIds = new HashSet<>();
        for (NarrativeTag tag : project.getNarrativeTags()) {
            if (!tagIds.add(tag.id())) {
                issues.add(new TagLibraryIssue("duplicate-tag-id", "Tag duplicada: " + tag.id()));
            }
        }

        Set<String> characterIds = new HashSet<>();
        for (Character character : project.getCharacters()) {
            characterIds.add(character.getId());
        }

        Set<String> prefixes = new HashSet<>();
        for (CharacterTagProfile profile : project.getCharacterTagProfiles()) {
            if (!characterIds.contains(profile.getCharacterId())) {
                issues.add(new TagLibraryIssue("missing-character", "Perfil aponta para personagem inexistente: " + profile.getCharacterId()));
            }
            String prefix = profile.getPrefix() == null ? "" : profile.getPrefix().trim().toLowerCase();
            if (!prefix.isBlank() && !prefixes.add(prefix)) {
                issues.add(new TagLibraryIssue("duplicate-prefix", "Prefixo duplicado: " + prefix));
            }
            for (String preferredTagId : profile.getPreferredTagIds()) {
                if (preferredTagId != null && !preferredTagId.isBlank() && !tagIds.contains(preferredTagId.toLowerCase())) {
                    issues.add(new TagLibraryIssue("missing-preferred-tag", "Perfil referencia tag inexistente: " + preferredTagId));
                }
            }
        }

        return issues;
    }
}
