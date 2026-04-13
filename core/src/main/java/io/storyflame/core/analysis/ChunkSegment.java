package io.storyflame.core.analysis;

public record ChunkSegment(
        String chapterId,
        String chapterTitle,
        String sceneId,
        String sceneTitle,
        String text,
        int wordCount,
        int chunkIndex
) {
}
