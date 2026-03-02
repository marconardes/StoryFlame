package io.storyflame.core.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NarrativeTagParser {
    private NarrativeTagParser() {
    }

    public static List<ParsedNarrativeTag> parse(String text, NarrativeTagCatalog catalog) {
        Objects.requireNonNull(catalog, "catalog");
        List<ParsedNarrativeTag> parsedTags = new ArrayList<>();
        for (NarrativeTagMatch match : NarrativeTagDetector.detect(text)) {
            parsedTags.add(new ParsedNarrativeTag(
                    match.rawText(),
                    match.tagId(),
                    match.startIndex(),
                    match.endIndex(),
                    catalog.contains(match.tagId())
            ));
        }
        return parsedTags;
    }
}
