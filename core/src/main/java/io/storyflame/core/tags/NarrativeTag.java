package io.storyflame.core.tags;

import java.util.Objects;

public record NarrativeTag(String id, String label, String description) {
    public NarrativeTag {
        id = normalizeId(id);
        label = Objects.requireNonNullElse(label, "");
        description = Objects.requireNonNullElse(description, "");
    }

    private static String normalizeId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Narrative tag id cannot be blank");
        }
        return value.trim().toLowerCase();
    }
}
