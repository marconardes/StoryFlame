package io.storyflame.desktop;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Scene;

final class DesktopOutlineFormatter {
    private static final int MAX_SYNOPSIS_LENGTH = 96;

    private DesktopOutlineFormatter() {
    }

    static String chapterLabel(int index, Chapter chapter, String fallbackTitle) {
        String title = DesktopHtml.escape(normalize(chapter == null ? null : chapter.getTitle(), fallbackTitle));
        int sceneCount = chapter == null || chapter.getScenes() == null ? 0 : chapter.getScenes().size();
        String suffix = sceneCount == 1 ? "1 cena" : sceneCount + " cenas";
        return "<html><b>" + (index + 1) + ". " + title + "</b>"
                + "<br><span style='color:#756754; font-size:10px;'>" + suffix + "</span></html>";
    }

    static String sceneLabel(int index, Scene scene, String fallbackTitle, String pointOfViewName) {
        String title = DesktopHtml.escape(normalize(scene == null ? null : scene.getTitle(), fallbackTitle));
        String synopsis = DesktopHtml.escape(compactSynopsis(scene == null ? null : scene.getSynopsis()));
        String pointOfView = DesktopHtml.escape(pointOfViewLabel(pointOfViewName));
        return "<html><b>" + (index + 1) + ". " + title + "</b>"
                + "<br><span style='color:#6f6250;'>" + synopsis + "</span>"
                + "<br><span style='color:#8a7a66; font-size:10px;'>" + pointOfView + "</span></html>";
    }

    private static String compactSynopsis(String synopsis) {
        String normalized = normalize(synopsis, "Sem sinopse");
        if (normalized.length() <= MAX_SYNOPSIS_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_SYNOPSIS_LENGTH - 1).trim() + "…";
    }

    private static String pointOfViewLabel(String pointOfViewName) {
        String normalized = normalize(pointOfViewName, "");
        return normalized.isBlank() ? "Sem POV definido" : "POV: " + normalized;
    }

    private static String normalize(String value, String fallback) {
        String normalized = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        return normalized.isBlank() ? fallback : normalized;
    }

    private static final class DesktopHtml {
        private DesktopHtml() {
        }

        private static String escape(String value) {
            return value
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }
    }
}
