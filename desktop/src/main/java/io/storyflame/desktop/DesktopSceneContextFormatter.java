package io.storyflame.desktop;

import java.util.List;

final class DesktopSceneContextFormatter {
    private static final int MAX_SYNOPSIS_LENGTH = 180;

    private DesktopSceneContextFormatter() {
    }

    static String synopsisText(String synopsis) {
        String normalized = normalize(synopsis, "Sem sinopse para esta cena.");
        if (normalized.length() > MAX_SYNOPSIS_LENGTH) {
            normalized = normalized.substring(0, MAX_SYNOPSIS_LENGTH - 1).trim() + "…";
        }
        return "<html><b>Sinopse</b><br><span style='color:#6f6250;'>" + escape(normalized) + "</span></html>";
    }

    static String charactersText(List<String> characters) {
        if (characters == null || characters.isEmpty()) {
            return "<html><b>Personagens em foco</b><br><span style='color:#6f6250;'>Sem personagem ligado.</span></html>";
        }
        return "<html><b>Personagens em foco</b><br><span style='color:#6f6250;'>"
                + escape(String.join(", ", characters))
                + "</span></html>";
    }

    static String tagsText(String summary) {
        String normalized = normalize(summary, "nenhuma");
        return "<html><b>Tags da cena</b><br><span style='color:#6f6250;'>" + escape(normalized) + "</span></html>";
    }

    static String integrityText(String integrityLabel) {
        return "<html><b>Integridade</b><br><span style='color:#6f6250;'>"
                + escape(normalize(integrityLabel, "Sem alertas."))
                + "</span></html>";
    }

    private static String normalize(String value, String fallback) {
        String normalized = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        return normalized.isBlank() ? fallback : normalized;
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
