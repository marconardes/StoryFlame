package io.storyflame.core.tags;

import java.text.Normalizer;

public final class NarrativeTagIdPolicy {
    private NarrativeTagIdPolicy() {
    }

    public static boolean isValid(String value) {
        return value != null && value.matches("[a-z]{4}\\d+");
    }

    public static String normalizeExplicitId(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "");
        return normalized;
    }

    public static String suggestFromText(String value) {
        String letters = normalizeExplicitId(value).replaceAll("[^a-z]+", "");
        if (letters.isBlank()) {
            return "";
        }
        String base = letters.length() >= 4
                ? letters.substring(0, 4)
                : (letters + "xxxx").substring(0, 4);
        return base + "1";
    }
}
