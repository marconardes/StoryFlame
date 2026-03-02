package io.storyflame.core.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NarrativeTagDetector {
    private static final Pattern TAG_PATTERN = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9_-]*)}");

    private NarrativeTagDetector() {
    }

    public static List<NarrativeTagMatch> detect(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<NarrativeTagMatch> matches = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(text);
        while (matcher.find()) {
            matches.add(new NarrativeTagMatch(
                    matcher.group(),
                    matcher.group(1).toLowerCase(),
                    matcher.start(),
                    matcher.end()
            ));
        }
        return matches;
    }
}
