package io.storyflame.desktop;

import io.storyflame.core.analysis.EmotionAnalysisReport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

final class DesktopAnalysisPanel {
    private final JPanel root;
    private final JTextArea analysisArea;
    private final JLabel emotionSentimentLabel;
    private final JLabel emotionDominantLabel;
    private final JLabel emotionChunkCountLabel;
    private final JLabel scopeNoticeLabel;
    private final JButton analyzeButton;

    DesktopAnalysisPanel() {
        this.analysisArea = new JTextArea();
        this.emotionSentimentLabel = new JLabel("Clima geral: sem analise");
        this.emotionDominantLabel = new JLabel("Emocao mais presente: sem analise");
        this.emotionChunkCountLabel = new JLabel("0 trechos avaliados");
        this.scopeNoticeLabel = new JLabel(DesktopEmotionReportFormatter.scopeNotice());
        this.analyzeButton = new JButton("Gerar analise");
        this.root = build();
        configureComponents();
        assignComponentNames();
    }

    JPanel component() {
        return root;
    }

    void setAnalyzeAction(Runnable action) {
        analyzeButton.addActionListener(event -> action.run());
    }

    void showLoading() {
        emotionSentimentLabel.setText("Clima geral: analisando...");
        emotionDominantLabel.setText("Emocao mais presente: analisando...");
        emotionChunkCountLabel.setText("Avaliando trechos...");
        analysisArea.setText(DesktopEmotionReportFormatter.loadingState());
    }

    void render(EmotionAnalysisReport report) {
        if (report == null) {
            emotionSentimentLabel.setText("Clima geral: sem analise");
            emotionDominantLabel.setText("Emocao mais presente: sem analise");
            emotionChunkCountLabel.setText("0 trechos avaliados");
            analysisArea.setText(DesktopEmotionReportFormatter.emptyState());
            return;
        }
        emotionSentimentLabel.setText(DesktopEmotionReportFormatter.formatOverviewLabel(report.overallSentiment()));
        emotionDominantLabel.setText(DesktopEmotionReportFormatter.formatDominantEmotionOverview(report.dominantEmotion()));
        emotionChunkCountLabel.setText(DesktopEmotionReportFormatter.formatChunkCountLabel(report.chunkCount()));
        analysisArea.setText(DesktopEmotionReportFormatter.format(report));
    }

    void showFailure(EmotionAnalysisReport report) {
        if (report == null) {
            emotionSentimentLabel.setText("Clima geral: analise indisponivel");
            emotionDominantLabel.setText("Emocao mais presente: analise indisponivel");
            emotionChunkCountLabel.setText("0 trechos avaliados");
            analysisArea.setText(DesktopEmotionReportFormatter.failureState());
            return;
        }
        emotionSentimentLabel.setText(DesktopEmotionReportFormatter.formatOverviewLabel(report.overallSentiment()));
        emotionDominantLabel.setText(DesktopEmotionReportFormatter.formatDominantEmotionOverview(report.dominantEmotion()));
        emotionChunkCountLabel.setText(DesktopEmotionReportFormatter.formatChunkCountLabel(report.chunkCount()));
        analysisArea.setText(DesktopEmotionReportFormatter.retainedReportFailureState(report));
    }

    void requestFocusInWindow() {
        analysisArea.requestFocusInWindow();
    }

    void setAnalyzeEnabled(boolean enabled) {
        analyzeButton.setEnabled(enabled);
    }

    private JPanel build() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(244, 239, 231));

        JPanel header = new JPanel(new GridLayout(1, 0, 8, 0));
        header.setOpaque(false);
        header.add(wrapBadge(emotionSentimentLabel));
        header.add(wrapBadge(emotionDominantLabel));
        header.add(wrapBadge(emotionChunkCountLabel));
        panel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(scopeNoticeLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(analysisArea), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        footer.add(analyzeButton);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private void configureComponents() {
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        analysisArea.setRows(12);
        analysisArea.setFont(new Font("Serif", Font.PLAIN, 14));
        analysisArea.setBackground(new Color(251, 248, 242));
        analysisArea.setBorder(BorderFactory.createTitledBorder("Relatorio emocional heuristico"));
        scopeNoticeLabel.setForeground(new Color(117, 85, 60));
        scopeNoticeLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    }

    private void assignComponentNames() {
        analysisArea.setName("analysisArea");
        emotionSentimentLabel.setName("emotionSentimentLabel");
        emotionDominantLabel.setName("emotionDominantLabel");
        emotionChunkCountLabel.setName("emotionChunkCountLabel");
        scopeNoticeLabel.setName("emotionScopeNoticeLabel");
        analyzeButton.setName("analyzeButton");
    }

    private JPanel wrapBadge(JLabel label) {
        JPanel badge = new JPanel(new BorderLayout());
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(162, 123, 92)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        badge.add(label, BorderLayout.CENTER);
        return badge;
    }
}
