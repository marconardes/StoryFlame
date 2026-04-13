package io.storyflame.core.analysis;

import java.util.Map;

public record EmotionInference(
        SentimentLabel sentiment,
        EmotionLabel dominantEmotion,
        Map<EmotionLabel, Double> scores
) {
}
