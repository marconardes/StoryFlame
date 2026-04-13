package io.storyflame.core.analysis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class EmotionAggregator {
    public EmotionAnalysisReport aggregate(List<EmotionChunkAnalysis> chunks) {
        List<EmotionChunkAnalysis> source = chunks == null ? List.of() : chunks;
        Map<EmotionLabel, Double> totals = new EnumMap<>(EmotionLabel.class);
        for (EmotionLabel label : EmotionLabel.values()) {
            totals.put(label, 0.0);
        }
        int positive = 0;
        int negative = 0;
        for (EmotionChunkAnalysis chunk : source) {
            if (chunk.sentiment() == SentimentLabel.POSITIVE) {
                positive++;
            } else if (chunk.sentiment() == SentimentLabel.NEGATIVE) {
                negative++;
            }
            for (Map.Entry<EmotionLabel, Double> entry : chunk.emotionScores().entrySet()) {
                totals.put(entry.getKey(), totals.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
            }
        }
        int divisor = Math.max(1, source.size());
        Map<EmotionLabel, Double> averages = new EnumMap<>(EmotionLabel.class);
        for (Map.Entry<EmotionLabel, Double> entry : totals.entrySet()) {
            averages.put(entry.getKey(), entry.getValue() / divisor);
        }
        SentimentLabel overallSentiment = positive == negative
                ? SentimentLabel.NEUTRAL
                : (positive > negative ? SentimentLabel.POSITIVE : SentimentLabel.NEGATIVE);
        EmotionLabel dominantEmotion = EmotionLabel.NEUTRAL;
        double dominantScore = 0.0;
        for (Map.Entry<EmotionLabel, Double> entry : averages.entrySet()) {
            if (entry.getValue() > dominantScore) {
                dominantEmotion = entry.getKey();
                dominantScore = entry.getValue();
            }
        }
        return new EmotionAnalysisReport(
                Instant.now(),
                source.size(),
                overallSentiment,
                dominantEmotion,
                Map.copyOf(averages),
                List.copyOf(new ArrayList<>(source))
        );
    }
}
