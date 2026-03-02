package io.storyflame.core.search;

public record SearchMatch(
        SearchTarget target,
        int chapterIndex,
        int sceneIndex,
        String title,
        String excerpt
) {
}
