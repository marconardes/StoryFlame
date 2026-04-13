package io.storyflame.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;

final class DesktopEditorStructurePanels {
    private DesktopEditorStructurePanels() {
    }

    static JPanel buildEditorPanel(
            JTree editorStructureTree,
            JTextField sceneTitleField,
            JTextArea sceneSynopsisArea,
            JTextArea sceneEditorArea,
            JPanel pointOfViewPanel,
            JLabel projectPathLabel,
            JLabel renderModeLabel,
            JLabel wordCountLabel,
            JLabel hoverTagPreviewLabel
    ) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(244, 239, 231));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);
        topPanel.add(sceneTitleField, BorderLayout.NORTH);
        JScrollPane synopsisScrollPane = new JScrollPane(sceneSynopsisArea);
        synopsisScrollPane.setPreferredSize(new Dimension(10, 88));
        topPanel.add(synopsisScrollPane, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel navigationPanel = new JPanel(new BorderLayout(8, 8));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Estrutura do livro"));
        navigationPanel.setBackground(new Color(251, 248, 242));
        navigationPanel.setPreferredSize(new Dimension(260, 10));
        navigationPanel.add(new JScrollPane(editorStructureTree), BorderLayout.CENTER);

        JSplitPane editorSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                navigationPanel,
                new JScrollPane(sceneEditorArea)
        );
        editorSplitPane.setResizeWeight(0.22);
        editorSplitPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(editorSplitPane, BorderLayout.CENTER);
        panel.add(pointOfViewPanel, BorderLayout.EAST);

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        footer.add(buildBadge(projectPathLabel), BorderLayout.CENTER);
        JPanel rightBadges = new JPanel(new GridLayout(1, 0, 8, 0));
        rightBadges.setOpaque(false);
        rightBadges.add(buildBadge(renderModeLabel));
        rightBadges.add(buildBadge(wordCountLabel));
        footer.add(rightBadges, BorderLayout.EAST);
        footer.add(buildBadge(hoverTagPreviewLabel), BorderLayout.WEST);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    static JPanel buildStructurePanel(
            JPanel chapterToolbar,
            JList<String> chapterList,
            JTextField chapterTitleField,
            JPanel sceneToolbar,
            JList<String> sceneList,
            JLabel chapterCountLabel,
            JLabel sceneCountLabel,
            JLabel characterCountLabel,
            JLabel contextLabel
    ) {
        JPanel chapterPanel = new JPanel(new BorderLayout(8, 8));
        chapterPanel.setBorder(BorderFactory.createTitledBorder("Capitulos em outline"));
        chapterPanel.add(chapterToolbar, BorderLayout.NORTH);
        chapterPanel.add(new JScrollPane(chapterList), BorderLayout.CENTER);
        chapterPanel.add(chapterTitleField, BorderLayout.SOUTH);

        JPanel scenePanel = new JPanel(new BorderLayout(8, 8));
        scenePanel.setBorder(BorderFactory.createTitledBorder("Cenas em outline"));
        scenePanel.add(sceneToolbar, BorderLayout.NORTH);
        scenePanel.add(new JScrollPane(sceneList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chapterPanel, scenePanel);
        splitPane.setResizeWeight(0.52);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(244, 239, 231));
        root.add(buildStructureSummaryPanel(chapterCountLabel, sceneCountLabel, characterCountLabel, contextLabel), BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        return root;
    }

    static JPanel buildPointOfViewPanel(
            JLabel pointOfViewLabel,
            JLabel sceneContextSynopsisLabel,
            JLabel sceneContextCharactersLabel,
            JLabel sceneContextTagsLabel,
            JLabel sceneContextIntegrityLabel,
            JTextField povSearchField,
            JList<String> povList,
            Runnable applyPointOfView,
            Runnable clearPointOfView
    ) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setOpaque(false);
        root.setPreferredSize(new Dimension(250, 10));
        root.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 8));
        header.setOpaque(false);
        header.add(buildBadge(pointOfViewLabel));
        header.add(buildBadge(sceneContextSynopsisLabel));
        header.add(buildBadge(sceneContextCharactersLabel));
        header.add(buildBadge(sceneContextTagsLabel));
        header.add(buildBadge(sceneContextIntegrityLabel));
        header.add(povSearchField);
        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(povList), BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        JButton applyButton = new JButton("Usar POV");
        JButton clearButton = new JButton("Limpar POV");
        applyButton.addActionListener(event -> applyPointOfView.run());
        clearButton.addActionListener(event -> clearPointOfView.run());
        footer.add(applyButton);
        footer.add(clearButton);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    static JPanel buildChapterToolbar(
            Runnable addChapter,
            Runnable deleteChapter,
            Runnable moveChapterUp,
            Runnable moveChapterDown,
            Runnable focusEditor
    ) {
        JPanel panel = new JPanel();
        JButton addButton = new JButton("+");
        JButton deleteButton = new JButton("-");
        JButton upButton = new JButton("Subir");
        JButton downButton = new JButton("Descer");
        JButton focusButton = new JButton("Ir");
        addButton.addActionListener(event -> addChapter.run());
        deleteButton.addActionListener(event -> deleteChapter.run());
        upButton.addActionListener(event -> moveChapterUp.run());
        downButton.addActionListener(event -> moveChapterDown.run());
        focusButton.addActionListener(event -> focusEditor.run());
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(upButton);
        panel.add(downButton);
        panel.add(focusButton);
        return panel;
    }

    static JPanel buildSceneToolbar(
            Runnable addScene,
            Runnable deleteScene,
            Runnable moveSceneUp,
            Runnable moveSceneDown,
            Runnable focusEditor
    ) {
        JPanel panel = new JPanel();
        JButton addButton = new JButton("+");
        JButton deleteButton = new JButton("-");
        JButton upButton = new JButton("Subir");
        JButton downButton = new JButton("Descer");
        JButton focusButton = new JButton("Abrir");
        addButton.addActionListener(event -> addScene.run());
        deleteButton.addActionListener(event -> deleteScene.run());
        upButton.addActionListener(event -> moveSceneUp.run());
        downButton.addActionListener(event -> moveSceneDown.run());
        focusButton.addActionListener(event -> focusEditor.run());
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(upButton);
        panel.add(downButton);
        panel.add(focusButton);
        return panel;
    }

    private static JPanel buildStructureSummaryPanel(
            JLabel chapterCountLabel,
            JLabel sceneCountLabel,
            JLabel characterCountLabel,
            JLabel contextLabel
    ) {
        JPanel panel = new JPanel(new GridLayout(1, 0, 8, 0));
        panel.setOpaque(false);
        panel.add(buildBadge(chapterCountLabel));
        panel.add(buildBadge(sceneCountLabel));
        panel.add(buildBadge(characterCountLabel));
        panel.add(buildBadge(contextLabel));
        return panel;
    }

    private static JPanel buildBadge(JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 205, 191)),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
        ));
        panel.setBackground(new Color(251, 248, 242));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setForeground(new Color(92, 79, 62));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
