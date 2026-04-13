package io.storyflame.core.publication;

import java.util.List;

public record PublicationChapter(
        String title,
        List<PublicationScene> scenes
) {
}
