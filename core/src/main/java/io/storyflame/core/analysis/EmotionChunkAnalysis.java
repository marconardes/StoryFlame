package io.storyflame.core.analysis;

import java.util.Map;

public record EmotionChunkAnalysis(
        String chapterId,
        String chapterTitle,
        String sceneId,
        String sceneTitle,
        String excerpt,
        int wordCount,
        int chunkIndex,
        SentimentLabel sentiment,
        EmotionLabel dominantEmotion,
        Map<EmotionLabel, Double> emotionScores
) {
}
