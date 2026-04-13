package io.storyflame.desktop;

import io.storyflame.core.analysis.EmotionAnalysisReport;
import io.storyflame.core.analysis.EmotionChunkAnalysis;
import io.storyflame.core.analysis.EmotionLabel;
import io.storyflame.core.analysis.SentimentLabel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

final class DesktopEmotionReportFormatter {
    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(new Locale("pt", "BR"))
            .withZone(ZoneId.systemDefault());

    private DesktopEmotionReportFormatter() {
    }

    static String emptyState() {
        return "Nenhuma leitura emocional foi gerada ainda.";
    }

    static String loadingState() {
        return "Analisando o clima emocional do manuscrito...";
    }

    static String scopeNotice() {
        return "Leitura heuristica offline. Use como apoio, nao como avaliacao final do manuscrito.";
    }

    static String failureState() {
        return "Falha ao atualizar a leitura emocional.\n"
                + "Verifique o status da operacao e tente novamente.";
    }

    static String retainedReportFailureState(EmotionAnalysisReport report) {
        return "Falha ao atualizar a leitura emocional.\n"
                + "A ultima leitura valida foi mantida abaixo.\n\n"
                + format(report);
    }

    static String format(EmotionAnalysisReport report) {
        StringBuilder builder = new StringBuilder();
        builder.append("Panorama emocional do manuscrito\n");
        builder.append("Aviso: ").append(scopeNotice()).append('\n');
        builder.append("Gerado em: ").append(REPORT_DATE_FORMATTER.format(report.generatedAt())).append('\n');
        builder.append("Resumo geral: ").append(summarySentence(report)).append('\n');
        builder.append("Trechos avaliados: ").append(report.chunkCount()).append("\n\n");

        builder.append("Emocoes predominantes\n");
        report.averageEmotionScores().entrySet().stream()
                .filter(entry -> entry.getValue() > 0.0)
                .sorted(Map.Entry.<EmotionLabel, Double>comparingByValue(Comparator.reverseOrder()))
                .forEach(entry ->
                builder.append("- ").append(formatEmotionLabel(entry.getKey())).append(": ")
                        .append(String.format(Locale.ROOT, "%.2f", entry.getValue())).append('\n'));

        builder.append("\nTrechos analisados\n");
        for (EmotionChunkAnalysis chunk : report.chunks()) {
            builder.append('\n')
                    .append(nonBlank(chunk.chapterTitle(), "Capitulo sem titulo"))
                    .append(" / ")
                    .append(nonBlank(chunk.sceneTitle(), "Cena sem titulo"))
                    .append(" [trecho ").append(chunk.chunkIndex() + 1).append("]\n")
                    .append("Clima do trecho: ").append(formatSentimentLabel(chunk.sentiment()))
                    .append(" | Emocao dominante do trecho: ").append(formatEmotionLabel(chunk.dominantEmotion())).append('\n')
                    .append(nonBlank(chunk.excerpt(), "Sem resumo do trecho.")).append('\n');
        }
        return builder.toString().strip();
    }

    static String summarySentence(EmotionAnalysisReport report) {
        return "o manuscrito esta com clima predominantemente "
                + formatSentimentLabel(report.overallSentiment())
                + ", com maior presenca de "
                + formatEmotionLabel(report.dominantEmotion());
    }

    static String formatOverviewLabel(SentimentLabel sentiment) {
        return "Clima geral: " + formatSentimentLabel(sentiment);
    }

    static String formatDominantEmotionOverview(EmotionLabel emotion) {
        return "Emocao mais presente: " + formatEmotionLabel(emotion);
    }

    static String formatChunkCountLabel(int count) {
        return count + " trechos avaliados";
    }

    static String formatSentimentLabel(SentimentLabel label) {
        if (label == null) {
            return "-";
        }
        return switch (label) {
            case POSITIVE -> "positivo";
            case NEGATIVE -> "negativo";
            case NEUTRAL -> "neutro";
        };
    }

    static String formatEmotionLabel(EmotionLabel label) {
        if (label == null) {
            return "-";
        }
        return switch (label) {
            case JOY -> "alegria";
            case SADNESS -> "tristeza";
            case ANGER -> "raiva";
            case FEAR -> "medo";
            case TENSION -> "tensao";
            case CALM -> "calma";
            case NEUTRAL -> "neutro";
        };
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
