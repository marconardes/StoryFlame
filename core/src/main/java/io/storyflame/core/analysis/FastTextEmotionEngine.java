package io.storyflame.core.analysis;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FastTextEmotionEngine {
    private static final String MODEL_RESOURCE = "/emotion_ptbr_lexicon.json";
    private final LexiconModel model;

    public FastTextEmotionEngine() {
        try (Reader reader = new InputStreamReader(
                FastTextEmotionEngine.class.getResourceAsStream(MODEL_RESOURCE),
                StandardCharsets.UTF_8
        )) {
            this.model = new Gson().fromJson(reader, LexiconModel.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load PT-BR emotion model", exception);
        }
    }

    public EmotionInference infer(String text) {
        Map<EmotionLabel, Double> scores = new EnumMap<>(EmotionLabel.class);
        for (EmotionLabel label : EmotionLabel.values()) {
            scores.put(label, 0.0);
        }
        String normalizedText = normalize(text);
        int positiveHits = 0;
        int negativeHits = 0;

        positiveHits += accumulate(scores, normalizedText, EmotionLabel.JOY, model.joy);
        positiveHits += accumulate(scores, normalizedText, EmotionLabel.CALM, model.calm);
        negativeHits += accumulate(scores, normalizedText, EmotionLabel.SADNESS, model.sadness);
        negativeHits += accumulate(scores, normalizedText, EmotionLabel.ANGER, model.anger);
        negativeHits += accumulate(scores, normalizedText, EmotionLabel.FEAR, model.fear);
        negativeHits += accumulate(scores, normalizedText, EmotionLabel.TENSION, model.tension);

        EmotionLabel dominantEmotion = dominantEmotion(scores);
        scores.putIfAbsent(EmotionLabel.NEUTRAL, dominantEmotion == EmotionLabel.NEUTRAL ? 1.0 : 0.0);
        SentimentLabel sentiment = sentiment(positiveHits, negativeHits);
        return new EmotionInference(sentiment, dominantEmotion, Map.copyOf(scores));
    }

    private int accumulate(Map<EmotionLabel, Double> scores, String text, EmotionLabel label, List<String> tokens) {
        int hits = 0;
        if (tokens == null) {
            return 0;
        }
        for (String token : tokens) {
            String normalizedToken = normalize(token);
            if (!normalizedToken.isBlank()) {
                hits += countMatches(text, normalizedToken);
            }
        }
        scores.put(label, (double) hits);
        return hits;
    }

    private int countMatches(String text, String token) {
        if (token.indexOf(' ') >= 0) {
            int hits = 0;
            int index = text.indexOf(token);
            while (index >= 0) {
                hits++;
                index = text.indexOf(token, index + token.length());
            }
            return hits;
        }
        Matcher matcher = Pattern.compile("\\b" + Pattern.quote(token) + "\\b").matcher(text);
        int hits = 0;
        while (matcher.find()) {
            hits++;
        }
        return hits;
    }

    private EmotionLabel dominantEmotion(Map<EmotionLabel, Double> scores) {
        EmotionLabel dominant = EmotionLabel.NEUTRAL;
        double dominantScore = 0.0;
        for (Map.Entry<EmotionLabel, Double> entry : scores.entrySet()) {
            if (entry.getKey() == EmotionLabel.NEUTRAL) {
                continue;
            }
            if (entry.getValue() > dominantScore) {
                dominant = entry.getKey();
                dominantScore = entry.getValue();
            }
        }
        if (dominantScore <= 0.0) {
            scores.put(EmotionLabel.NEUTRAL, 1.0);
            return EmotionLabel.NEUTRAL;
        }
        return dominant;
    }

    private SentimentLabel sentiment(int positiveHits, int negativeHits) {
        if (positiveHits == negativeHits) {
            return SentimentLabel.NEUTRAL;
        }
        return positiveHits > negativeHits ? SentimentLabel.POSITIVE : SentimentLabel.NEGATIVE;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private static final class LexiconModel {
        List<String> joy;
        List<String> sadness;
        List<String> anger;
        List<String> fear;
        List<String> tension;
        List<String> calm;
    }
}
