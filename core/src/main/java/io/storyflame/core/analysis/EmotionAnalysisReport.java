package io.storyflame.core.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record EmotionAnalysisReport(
        Instant generatedAt,
        int chunkCount,
        SentimentLabel overallSentiment,
        EmotionLabel dominantEmotion,
        Map<EmotionLabel, Double> averageEmotionScores,
        List<EmotionChunkAnalysis> chunks
) {
}
