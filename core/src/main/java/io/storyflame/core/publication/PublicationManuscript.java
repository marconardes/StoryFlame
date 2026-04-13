package io.storyflame.core.publication;

import java.util.List;

public record PublicationManuscript(
        String title,
        String author,
        List<PublicationChapter> chapters
) {
}
