package io.storyflame.desktop;

import io.storyflame.core.validation.NarrativeIntegrityIssue;
import java.util.List;

final class DesktopNarrativeIntegrityFormatter {
    private DesktopNarrativeIntegrityFormatter() {
    }

    static String labelText(List<NarrativeIntegrityIssue> issues) {
        int count = issues == null ? 0 : issues.size();
        return switch (count) {
            case 0 -> "0 referencias quebradas no POV";
            case 1 -> "1 referencia quebrada no POV";
            default -> count + " referencias quebradas no POV";
        };
    }

    static String tooltipText(List<NarrativeIntegrityIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("<html>");
        for (int index = 0; index < issues.size(); index++) {
            NarrativeIntegrityIssue issue = issues.get(index);
            if (index > 0) {
                builder.append("<br>");
            }
            builder.append("Cena '")
                    .append(escape(issue.sceneTitle()))
                    .append("' em '")
                    .append(escape(issue.chapterTitle()))
                    .append("' usa um POV removido ou inexistente.");
        }
        builder.append("</html>");
        return builder.toString();
    }

    private static String escape(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
