package io.storyflame.core.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProjectArchiveLayoutTest {
    @Test
    void requiredEntriesContainProjectMetadata() {
        assertTrue(ProjectArchiveLayout.requiredEntries().contains(ProjectArchiveLayout.PROJECT_FILE));
        assertTrue(ProjectArchiveLayout.requiredEntries().contains(ProjectArchiveLayout.MANIFEST_FILE));
    }

    @Test
    void chapterFileUsesExpectedConvention() {
        assertEquals("chapters/ch-01.json", ProjectArchiveLayout.chapterFile("ch-01"));
    }
}

