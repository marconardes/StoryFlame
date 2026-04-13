package io.storyflame.desktop;

import io.storyflame.core.tags.ParsedNarrativeTag;
import java.util.List;
import java.util.stream.Collectors;

final class DesktopTagParseFormatter {
    private DesktopTagParseFormatter() {
    }

    static String labelText(List<ParsedNarrativeTag> parsedTags) {
        if (parsedTags == null || parsedTags.isEmpty()) {
            return "0 tags";
        }
        long invalidCount = parsedTags.stream().filter(tag -> !tag.valid()).count();
        if (invalidCount == 0) {
            return parsedTags.size() == 1 ? "1 tag valida na cena" : parsedTags.size() + " tags validas na cena";
        }
        return parsedTags.size() + " tags | " + invalidCount
                + (invalidCount == 1 ? " invalida na cena" : " invalidas na cena");
    }

    static String tooltipText(List<ParsedNarrativeTag> parsedTags) {
        if (parsedTags == null || parsedTags.isEmpty()) {
            return null;
        }
        String validTags = parsedTags.stream()
                .filter(ParsedNarrativeTag::valid)
                .map(tag -> "{" + tag.tagId() + "}")
                .distinct()
                .collect(Collectors.joining(", "));
        String invalidTags = parsedTags.stream()
                .filter(tag -> !tag.valid())
                .map(tag -> "{" + tag.tagId() + "}")
                .distinct()
                .collect(Collectors.joining(", "));
        if (invalidTags.isBlank()) {
            return "<html>Tags validas: " + escape(validTags) + "</html>";
        }
        if (validTags.isBlank()) {
            return "<html>Tags invalidas: " + escape(invalidTags) + "</html>";
        }
        return "<html>Tags validas: " + escape(validTags) + "<br>Tags invalidas: " + escape(invalidTags) + "</html>";
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
