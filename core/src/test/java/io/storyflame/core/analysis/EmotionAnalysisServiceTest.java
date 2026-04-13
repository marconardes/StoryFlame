package io.storyflame.core.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmotionAnalysisServiceTest {
    @Test
    void chunkerSplitsProjectIntoSegments() {
        Chunker chunker = new Chunker();
        List<ChunkSegment> chunks = chunker.split(sampleProject());

        assertEquals(2, chunks.size());
        assertTrue(chunks.get(0).text().contains("sorriu"));
    }

    @Test
    void analyzesProjectWithPtBrEmotionHeuristics() {
        EmotionAnalysisService service = new EmotionAnalysisService();
        EmotionAnalysisReport report = service.analyze(sampleProject());

        assertNotNull(report);
        assertEquals(2, report.chunkCount());
        assertNotNull(report.overallSentiment());
        assertNotNull(report.dominantEmotion());
        assertEquals(SentimentLabel.POSITIVE, report.chunks().get(0).sentiment());
        assertEquals(SentimentLabel.NEGATIVE, report.chunks().get(1).sentiment());
        assertNotNull(report.chunks().get(0).emotionScores());
    }

    @Test
    void reusesEmotionCacheAcrossRepeatedAnalysis() {
        EmotionAnalysisService service = new EmotionAnalysisService();
        Project project = sampleProject();

        EmotionAnalysisReport firstReport = service.analyze(project);
        int cacheSizeAfterFirstRun = project.getEmotionCache().getEntries().size();
        EmotionChunkAnalysis cachedFirstChunk = project.getEmotionCache().getEntries().get(0).analysis();

        EmotionAnalysisReport secondReport = service.analyze(project);

        assertEquals(2, cacheSizeAfterFirstRun);
        assertEquals(cacheSizeAfterFirstRun, project.getEmotionCache().getEntries().size());
        assertEquals(firstReport.chunkCount(), secondReport.chunkCount());
        assertEquals(firstReport.overallSentiment(), secondReport.overallSentiment());
        assertTrue(cachedFirstChunk == secondReport.chunks().get(0));
    }

    @Test
    void countsRepeatedEmotionCuesAsStrongerSignal() {
        EmotionAnalysisService service = new EmotionAnalysisService();
        Project project = Project.blank("Analise repetida", "Marco");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo 1",
                List.of(new Scene(
                        "scene-1",
                        "Cena 1",
                        "Ela sorriu, sorriu de novo e manteve a esperanca acesa com alivio.",
                        null
                ))
        ));

        EmotionAnalysisReport report = service.analyze(project);

        assertEquals(1, report.chunkCount());
        assertEquals(SentimentLabel.POSITIVE, report.overallSentiment());
        assertTrue(report.averageEmotionScores().getOrDefault(EmotionLabel.JOY, 0.0) >= 2.0);
    }

    private Project sampleProject() {
        Project project = Project.blank("Analise", "Marco");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo 1",
                List.of(
                        new Scene("scene-1", "Cena 1", "Ela sorriu com alivio e esperanca.", null),
                        new Scene("scene-2", "Cena 2", "O medo cresceu no silencio tenso e sombrio.", null)
                )
        ));
        return project;
    }
}
