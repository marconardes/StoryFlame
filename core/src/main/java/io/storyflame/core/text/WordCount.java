package io.storyflame.core.text;

public final class WordCount {
    private WordCount() {
    }

    public static int count(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        String trimmed = value.trim();
        int count = 0;
        boolean insideWord = false;
        for (int index = 0; index < trimmed.length(); index++) {
            char current = trimmed.charAt(index);
            if (Character.isWhitespace(current)) {
                insideWord = false;
                continue;
            }
            if (!insideWord) {
                count++;
                insideWord = true;
            }
        }
        return count;
    }
}
