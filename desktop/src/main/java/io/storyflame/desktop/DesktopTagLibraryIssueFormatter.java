package io.storyflame.desktop;

import io.storyflame.core.tags.TagLibraryIssue;
import java.util.List;

final class DesktopTagLibraryIssueFormatter {
    private DesktopTagLibraryIssueFormatter() {
    }

    static String labelText(List<TagLibraryIssue> issues) {
        int count = issues == null ? 0 : issues.size();
        return switch (count) {
            case 0 -> "Biblioteca sem inconsistencias";
            case 1 -> "1 inconsistencia na biblioteca";
            default -> count + " inconsistencias na biblioteca";
        };
    }

    static String tooltipText(List<TagLibraryIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("<html>");
        for (int index = 0; index < issues.size(); index++) {
            if (index > 0) {
                builder.append("<br>");
            }
            builder.append(escape(issues.get(index).message()));
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
