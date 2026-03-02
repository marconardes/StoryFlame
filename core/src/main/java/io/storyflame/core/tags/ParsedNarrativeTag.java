package io.storyflame.core.tags;

public record ParsedNarrativeTag(
        String rawText,
        String tagId,
        int startIndex,
        int endIndex,
        boolean valid
) {
}
