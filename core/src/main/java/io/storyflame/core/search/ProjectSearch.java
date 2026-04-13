package io.storyflame.core.search;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ProjectSearch {
    private ProjectSearch() {
    }

    public static List<SearchMatch> search(Project project, String query) {
        Objects.requireNonNull(project, "project");
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalizedQuery = normalize(query);
        List<SearchMatch> matches = new ArrayList<>();
        List<Chapter> chapters = project.getChapters();
        for (int chapterIndex = 0; chapterIndex < chapters.size(); chapterIndex++) {
            Chapter chapter = chapters.get(chapterIndex);
            if (contains(chapter.getTitle(), normalizedQuery)) {
                matches.add(new SearchMatch(
                        SearchTarget.CHAPTER,
                        chapterIndex,
                        -1,
                        chapter.getTitle(),
                        excerpt(chapter.getTitle(), normalizedQuery)
                ));
            }

            List<Scene> scenes = chapter.getScenes();
            for (int sceneIndex = 0; sceneIndex < scenes.size(); sceneIndex++) {
                Scene scene = scenes.get(sceneIndex);
                if (contains(scene.getTitle(), normalizedQuery)) {
                    matches.add(new SearchMatch(
                            SearchTarget.SCENE_TITLE,
                            chapterIndex,
                            sceneIndex,
                            scene.getTitle(),
                            excerpt(scene.getTitle(), normalizedQuery)
                    ));
                }
                if (contains(scene.getContent(), normalizedQuery)) {
                    matches.add(new SearchMatch(
                            SearchTarget.SCENE_CONTENT,
                            chapterIndex,
                            sceneIndex,
                            scene.getTitle(),
                            excerpt(scene.getContent(), normalizedQuery)
                    ));
                }
            }
        }
        return matches;
    }

    private static boolean contains(String value, String normalizedQuery) {
        return value != null && normalize(value).contains(normalizedQuery);
    }

    private static String excerpt(String value, String normalizedQuery) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalizedValue = normalize(value);
        int matchIndex = normalizedValue.indexOf(normalizedQuery);
        if (matchIndex < 0) {
            return value;
        }
        int start = Math.max(0, matchIndex - 24);
        int end = Math.min(value.length(), matchIndex + normalizedQuery.length() + 24);
        String excerpt = value.substring(start, end).trim();
        if (start > 0) {
            excerpt = "..." + excerpt;
        }
        if (end < value.length()) {
            excerpt = excerpt + "...";
        }
        return excerpt;
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }
}
