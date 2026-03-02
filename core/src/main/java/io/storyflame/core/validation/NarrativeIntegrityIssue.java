package io.storyflame.core.validation;

public record NarrativeIntegrityIssue(
        String chapterId,
        String chapterTitle,
        String sceneId,
        String sceneTitle,
        String missingCharacterId
) {
    public String message() {
        return "Cena '" + sceneTitle + "' referencia personagem inexistente: " + missingCharacterId;
    }
}
