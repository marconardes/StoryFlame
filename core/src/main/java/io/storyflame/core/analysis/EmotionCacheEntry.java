package io.storyflame.core.analysis;

public record EmotionCacheEntry(
        String sceneId,
        String contentHash,
        EmotionChunkAnalysis analysis
) {
}
