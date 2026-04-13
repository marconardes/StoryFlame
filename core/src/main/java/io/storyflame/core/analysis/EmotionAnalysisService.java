package io.storyflame.core.analysis;

import io.storyflame.core.model.Project;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public final class EmotionAnalysisService {
    private final Chunker chunker;
    private final FastTextEmotionEngine engine;
    private final EmotionAggregator aggregator;

    public EmotionAnalysisService() {
        this.chunker = new Chunker();
        this.engine = new FastTextEmotionEngine();
        this.aggregator = new EmotionAggregator();
    }

    public EmotionAnalysisReport analyze(Project project) {
        EmotionCache cache = project.getEmotionCache();
        List<EmotionChunkAnalysis> analyses = new ArrayList<>();
        for (ChunkSegment segment : chunker.split(project)) {
            String contentHash = hash(segment.text());
            EmotionChunkAnalysis cached = cache.find(segment.sceneId(), contentHash);
            if (cached != null) {
                analyses.add(cached);
                continue;
            }
            EmotionInference inference = engine.infer(segment.text());
            EmotionChunkAnalysis analysis = new EmotionChunkAnalysis(
                    segment.chapterId(),
                    segment.chapterTitle(),
                    segment.sceneId(),
                    segment.sceneTitle(),
                    excerpt(segment.text()),
                    segment.wordCount(),
                    segment.chunkIndex(),
                    inference.sentiment(),
                    inference.dominantEmotion(),
                    inference.scores()
            );
            cache.put(segment.sceneId(), contentHash, analysis);
            analyses.add(analysis);
        }
        EmotionAnalysisReport report = aggregator.aggregate(analyses);
        project.setEmotionAnalysis(report);
        return report;
    }

    private String excerpt(String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 140 ? normalized : normalized.substring(0, 140) + "...";
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash emotion chunk", exception);
        }
    }
}
