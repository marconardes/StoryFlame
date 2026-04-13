package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.analysis.EmotionAnalysisReport;
import io.storyflame.core.analysis.EmotionChunkAnalysis;
import io.storyflame.core.analysis.EmotionLabel;
import io.storyflame.core.analysis.SentimentLabel;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DesktopEmotionReportFormatterTest {
    @Test
    void formatsReadableSummaryAndChunks() {
        EmotionAnalysisReport report = new EmotionAnalysisReport(
                Instant.parse("2026-03-28T10:15:30Z"),
                2,
                SentimentLabel.POSITIVE,
                EmotionLabel.JOY,
                Map.of(EmotionLabel.JOY, 0.81, EmotionLabel.CALM, 0.34),
                List.of(
                        new EmotionChunkAnalysis(
                                "ch-1",
                                "Capitulo 1",
                                "sc-1",
                                "Cena 1",
                                "A heroina enfim sorriu.",
                                120,
                                0,
                                SentimentLabel.POSITIVE,
                                EmotionLabel.JOY,
                                Map.of(EmotionLabel.JOY, 0.91)
                        )
                )
        );

        String formatted = DesktopEmotionReportFormatter.format(report);

        assertTrue(formatted.contains("Panorama emocional do manuscrito"));
        assertTrue(formatted.contains("Aviso: Leitura heuristica offline."));
        assertTrue(formatted.contains("Resumo geral: o manuscrito esta com clima predominantemente positivo, com maior presenca de alegria"));
        assertTrue(formatted.contains("Emocoes predominantes"));
        assertTrue(formatted.contains("Trechos avaliados: 2"));
        assertTrue(formatted.contains("Capitulo 1 / Cena 1 [trecho 1]"));
        assertTrue(formatted.contains("Clima do trecho: positivo | Emocao dominante do trecho: alegria"));
    }

    @Test
    void exposesStableLabelsAndStates() {
        assertEquals("Nenhuma leitura emocional foi gerada ainda.", DesktopEmotionReportFormatter.emptyState());
        assertEquals("Analisando o clima emocional do manuscrito...", DesktopEmotionReportFormatter.loadingState());
        assertEquals(
                "Leitura heuristica offline. Use como apoio, nao como avaliacao final do manuscrito.",
                DesktopEmotionReportFormatter.scopeNotice()
        );
        assertEquals(
                "Falha ao atualizar a leitura emocional.\nVerifique o status da operacao e tente novamente.",
                DesktopEmotionReportFormatter.failureState()
        );
        assertEquals("tristeza", DesktopEmotionReportFormatter.formatEmotionLabel(EmotionLabel.SADNESS));
        assertEquals("neutro", DesktopEmotionReportFormatter.formatSentimentLabel(SentimentLabel.NEUTRAL));
        assertEquals("Clima geral: positivo", DesktopEmotionReportFormatter.formatOverviewLabel(SentimentLabel.POSITIVE));
        assertEquals("Emocao mais presente: calma", DesktopEmotionReportFormatter.formatDominantEmotionOverview(EmotionLabel.CALM));
        assertEquals("3 trechos avaliados", DesktopEmotionReportFormatter.formatChunkCountLabel(3));
    }

    @Test
    void retainsPreviousReportWhenFailureOccursAfterExistingAnalysis() {
        EmotionAnalysisReport report = new EmotionAnalysisReport(
                Instant.parse("2026-03-28T10:15:30Z"),
                1,
                SentimentLabel.NEGATIVE,
                EmotionLabel.FEAR,
                Map.of(EmotionLabel.FEAR, 0.74),
                List.of(
                        new EmotionChunkAnalysis(
                                "ch-1",
                                "Capitulo 1",
                                "sc-1",
                                "Cena 1",
                                "A casa ficou em silencio.",
                                90,
                                0,
                                SentimentLabel.NEGATIVE,
                                EmotionLabel.FEAR,
                                Map.of(EmotionLabel.FEAR, 0.74)
                        )
                )
        );

        String formatted = DesktopEmotionReportFormatter.retainedReportFailureState(report);

        assertTrue(formatted.startsWith("Falha ao atualizar a leitura emocional."));
        assertTrue(formatted.contains("A ultima leitura valida foi mantida abaixo."));
        assertTrue(formatted.contains("Panorama emocional do manuscrito"));
    }
}
