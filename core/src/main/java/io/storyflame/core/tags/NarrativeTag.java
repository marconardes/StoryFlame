package io.storyflame.core.tags;

import java.util.Objects;

public record NarrativeTag(String id, String label, String description, String template) {
    public NarrativeTag {
        id = normalizeId(id);
        label = Objects.requireNonNullElse(label, "");
        description = Objects.requireNonNullElse(description, "");
        template = Objects.requireNonNullElse(template, "");
    }

    private static String normalizeId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Narrative tag id cannot be blank");
        }
        return value.trim().toLowerCase();
    }
}
