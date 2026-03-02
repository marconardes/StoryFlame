package io.storyflame.core.validation;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NarrativeIntegrityValidator {
    private NarrativeIntegrityValidator() {
    }

    public static List<NarrativeIntegrityIssue> findBrokenPointOfViewReferences(Project project) {
        if (project == null) {
            return List.of();
        }

        Set<String> characterIds = new HashSet<>();
        for (Character character : project.getCharacters()) {
            if (character.getId() != null && !character.getId().isBlank()) {
                characterIds.add(character.getId());
            }
        }

        List<NarrativeIntegrityIssue> issues = new ArrayList<>();
        for (Chapter chapter : project.getChapters()) {
            for (Scene scene : chapter.getScenes()) {
                String povId = scene.getPointOfViewCharacterId();
                if (povId == null || povId.isBlank() || characterIds.contains(povId)) {
                    continue;
                }
                issues.add(new NarrativeIntegrityIssue(
                        chapter.getId(),
                        chapter.getTitle(),
                        scene.getId(),
                        scene.getTitle(),
                        povId
                ));
            }
        }
        return issues;
    }
}
