package io.storyflame.core.storage;

import java.util.List;

public record ProjectArchiveInspection(
        boolean valid,
        boolean requiresMigration,
        int detectedVersion,
        List<String> issues
) {
}
