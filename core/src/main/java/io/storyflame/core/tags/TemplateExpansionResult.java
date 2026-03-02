package io.storyflame.core.tags;

import java.util.List;

public record TemplateExpansionResult(
        String text,
        List<String> expandedTagIds,
        List<String> invalidTagIds
) {
}
