package io.storyflame.core.analysis;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.text.WordCount;
import java.util.ArrayList;
import java.util.List;

public final class Chunker {
    private static final int MAX_WORDS_PER_CHUNK = 120;

    public List<ChunkSegment> split(Project project) {
        List<ChunkSegment> segments = new ArrayList<>();
        if (project == null) {
            return segments;
        }
        for (Chapter chapter : project.getChapters()) {
            for (Scene scene : chapter.getScenes()) {
                segments.addAll(splitScene(chapter, scene));
            }
        }
        return segments;
    }

    private List<ChunkSegment> splitScene(Chapter chapter, Scene scene) {
        List<ChunkSegment> segments = new ArrayList<>();
        String source = scene.getContent() == null ? "" : scene.getContent().trim();
        if (source.isBlank()) {
            return segments;
        }
        String[] paragraphs = source.split("\\R\\R+");
        int chunkIndex = 0;
        StringBuilder current = new StringBuilder();
        int currentWords = 0;
        for (String paragraph : paragraphs) {
            String normalized = paragraph.strip();
            if (normalized.isBlank()) {
                continue;
            }
            int paragraphWords = WordCount.count(normalized);
            if (currentWords > 0 && currentWords + paragraphWords > MAX_WORDS_PER_CHUNK) {
                segments.add(new ChunkSegment(
                        chapter.getId(),
                        chapter.getTitle(),
                        scene.getId(),
                        scene.getTitle(),
                        current.toString().strip(),
                        currentWords,
                        chunkIndex++
                ));
                current.setLength(0);
                currentWords = 0;
            }
            if (current.length() > 0) {
                current.append("\n\n");
            }
            current.append(normalized);
            currentWords += paragraphWords;
        }
        if (current.length() > 0) {
            segments.add(new ChunkSegment(
                    chapter.getId(),
                    chapter.getTitle(),
                    scene.getId(),
                    scene.getTitle(),
                    current.toString().strip(),
                    currentWords,
                    chunkIndex
            ));
        }
        return segments;
    }
}
