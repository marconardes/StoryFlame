package io.storyflame.core.archive;

import java.util.List;

public final class ProjectArchiveLayout {
    public static final String SPEC_VERSION = "1";
    public static final String MANIFEST_FILE = "manifest.json";
    public static final String PROJECT_FILE = "project.json";
    public static final String CHAPTERS_DIRECTORY = "chapters/";
    public static final String CHARACTERS_DIRECTORY = "characters/";
    public static final String ANALYSIS_DIRECTORY = "analysis/";
    public static final String ASSETS_DIRECTORY = "assets/";

    private ProjectArchiveLayout() {
    }

    public static List<String> requiredEntries() {
        return List.of(
                MANIFEST_FILE,
                PROJECT_FILE,
                CHAPTERS_DIRECTORY,
                CHARACTERS_DIRECTORY
        );
    }

    public static String chapterFile(String chapterId) {
        return CHAPTERS_DIRECTORY + chapterId + ".json";
    }

    public static String characterFile(String characterId) {
        return CHARACTERS_DIRECTORY + characterId + ".json";
    }

    public static String analysisFile(String name) {
        return ANALYSIS_DIRECTORY + name + ".json";
    }
}

