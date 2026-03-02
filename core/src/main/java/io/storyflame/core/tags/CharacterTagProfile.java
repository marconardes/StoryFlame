package io.storyflame.core.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CharacterTagProfile {
    private String characterId;
    private String prefix;
    private final List<String> preferredTagIds;

    public CharacterTagProfile() {
        this("", "", List.of());
    }

    public CharacterTagProfile(String characterId, String prefix, List<String> preferredTagIds) {
        this.characterId = Objects.requireNonNullElse(characterId, "");
        this.prefix = Objects.requireNonNullElse(prefix, "");
        this.preferredTagIds = new ArrayList<>(Objects.requireNonNullElse(preferredTagIds, List.of()));
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = Objects.requireNonNullElse(characterId, "");
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = Objects.requireNonNullElse(prefix, "");
    }

    public List<String> getPreferredTagIds() {
        return preferredTagIds;
    }
}
