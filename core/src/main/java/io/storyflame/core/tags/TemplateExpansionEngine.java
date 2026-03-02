package io.storyflame.core.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TemplateExpansionEngine {
    private TemplateExpansionEngine() {
    }

    public static TemplateExpansionResult expand(String text, NarrativeTagCatalog catalog, TemplateExpansionMode mode) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(mode, "mode");
        String source = Objects.requireNonNullElse(text, "");
        if (mode == TemplateExpansionMode.DRAFT || source.isBlank()) {
            return new TemplateExpansionResult(source, List.of(), List.of());
        }

        List<ParsedNarrativeTag> parsedTags = NarrativeTagParser.parse(source, catalog);
        if (parsedTags.isEmpty()) {
            return new TemplateExpansionResult(source, List.of(), List.of());
        }

        StringBuilder rendered = new StringBuilder();
        List<String> expandedTagIds = new ArrayList<>();
        List<String> invalidTagIds = new ArrayList<>();
        int cursor = 0;
        for (ParsedNarrativeTag parsedTag : parsedTags) {
            rendered.append(source, cursor, parsedTag.startIndex());
            if (parsedTag.valid()) {
                rendered.append(expandTag(parsedTag.tagId(), catalog));
                expandedTagIds.add(parsedTag.tagId());
            } else {
                rendered.append(parsedTag.rawText());
                invalidTagIds.add(parsedTag.tagId());
            }
            cursor = parsedTag.endIndex();
        }
        rendered.append(source.substring(cursor));

        return new TemplateExpansionResult(
                normalizeSpacing(rendered.toString()),
                distinct(expandedTagIds),
                distinct(invalidTagIds)
        );
    }

    private static String expandTag(String tagId, NarrativeTagCatalog catalog) {
        NarrativeTag tag = catalog.resolve(tagId);
        String template = resolveTemplate(tag);
        return preserveTrailingPunctuationSpacing(template);
    }

    private static String resolveTemplate(NarrativeTag tag) {
        if (tag == null) {
            return "";
        }
        if (!tag.template().isBlank()) {
            return tag.template();
        }
        if (!tag.label().isBlank()) {
            return tag.label().toLowerCase();
        }
        return tag.id();
    }

    private static String preserveTrailingPunctuationSpacing(String replacement) {
        return replacement == null ? "" : replacement.strip();
    }

    private static String normalizeSpacing(String text) {
        return text
                .replace(" ,", ",")
                .replace(" .", ".")
                .replace(" !", "!")
                .replace(" ?", "?")
                .replace(" ;", ";")
                .replace(" :", ":");
    }

    private static List<String> distinct(List<String> values) {
        return values.stream().distinct().toList();
    }
}
