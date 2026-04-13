package io.storyflame.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

final class DesktopTagPanels {
    private DesktopTagPanels() {
    }

    static JPanel buildTagsPanel(
            JTextField tagSearchField,
            JList<String> tagList,
            JTextField tagIdField,
            JTextField tagLabelField,
            JTextField tagTemplateField,
            JLabel tagDetailModeLabel,
            JLabel tagDraftHintLabel,
            JLabel selectedTagUsageLabel,
            JLabel selectedTagStatusLabel,
            JLabel tagCountLabel,
            JLabel tagLibraryIssuesLabel,
            Runnable addTag,
            Runnable deleteTag,
            Runnable clearTagSearch,
            Runnable saveTag,
            Runnable cancelTagDraft
    ) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(244, 239, 231));

        root.add(buildTagLibraryPanel(
                tagSearchField,
                tagList,
                tagIdField,
                tagLabelField,
                tagTemplateField,
                tagDetailModeLabel,
                tagDraftHintLabel,
                selectedTagUsageLabel,
                selectedTagStatusLabel,
                addTag,
                deleteTag,
                clearTagSearch,
                saveTag,
                cancelTagDraft
        ), BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        footer.add(buildBadge(tagCountLabel));
        footer.add(buildBadge(tagLibraryIssuesLabel));
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private static JPanel buildTagLibraryPanel(
            JTextField tagSearchField,
            JList<String> tagList,
            JTextField tagIdField,
            JTextField tagLabelField,
            JTextField tagTemplateField,
            JLabel tagDetailModeLabel,
            JLabel tagDraftHintLabel,
            JLabel selectedTagUsageLabel,
            JLabel selectedTagStatusLabel,
            Runnable addTag,
            Runnable deleteTag,
            Runnable clearTagSearch,
            Runnable saveTag,
            Runnable cancelTagDraft
    ) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Biblioteca de tags"));

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        JPanel searchPanel = new JPanel(new BorderLayout(6, 6));
        searchPanel.setOpaque(false);
        searchPanel.add(tagSearchField, BorderLayout.NORTH);
        searchPanel.add(buildSectionHint("Crie primeiro. Vinculos e acoes avancadas aparecem depois."), BorderLayout.SOUTH);
        header.add(searchPanel, BorderLayout.NORTH);
        header.add(buildTagLibrarySummaryPanel(selectedTagUsageLabel, selectedTagStatusLabel), BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(buildTagToolbar(addTag, deleteTag, clearTagSearch), BorderLayout.NORTH);
        center.add(new JScrollPane(tagList), BorderLayout.CENTER);
        center.add(buildTagDetailsPanel(
                tagIdField,
                tagLabelField,
                tagTemplateField,
                tagDetailModeLabel,
                tagDraftHintLabel,
                saveTag,
                cancelTagDraft
        ), BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel buildTagDetailsPanel(
            JTextField tagIdField,
            JTextField tagLabelField,
            JTextField tagTemplateField,
            JLabel tagDetailModeLabel,
            JLabel tagDraftHintLabel,
            Runnable saveTag,
            Runnable cancelTagDraft
    ) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Ficha da tag"));
        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.setOpaque(false);
        top.add(buildSectionHintLabel(tagDetailModeLabel), BorderLayout.NORTH);
        top.add(buildSectionHintLabel(tagDraftHintLabel), BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(1, 0, 8, 0));
        fields.setOpaque(false);
        fields.add(tagIdField);
        fields.add(tagLabelField);
        fields.add(tagTemplateField);
        panel.add(fields, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        JButton saveButton = new JButton("Salvar tag");
        JButton cancelButton = new JButton("Cancelar");
        saveButton.setName("saveTagButton");
        cancelButton.setName("cancelTagDraftButton");
        saveButton.addActionListener(event -> saveTag.run());
        cancelButton.addActionListener(event -> cancelTagDraft.run());
        footer.add(saveButton);
        footer.add(cancelButton);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel buildTagLibrarySummaryPanel(
            JLabel selectedTagUsageLabel,
            JLabel selectedTagStatusLabel
    ) {
        JPanel panel = new JPanel(new GridLayout(1, 0, 8, 0));
        panel.setOpaque(false);
        panel.add(buildBadge(selectedTagUsageLabel));
        panel.add(buildBadge(selectedTagStatusLabel));
        return panel;
    }

    private static JPanel buildTagToolbar(
            Runnable addTag,
            Runnable deleteTag,
            Runnable clearTagSearch
    ) {
        JPanel panel = new JPanel();
        JButton addButton = new JButton("Nova tag");
        JButton deleteButton = new JButton("Excluir tag");
        JButton clearButton = new JButton("Limpar busca");
        addButton.setName("addTagButton");
        deleteButton.setName("deleteTagButton");
        clearButton.setName("clearTagSearchButton");
        addButton.addActionListener(event -> addTag.run());
        deleteButton.addActionListener(event -> deleteTag.run());
        clearButton.addActionListener(event -> clearTagSearch.run());
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        return panel;
    }

    private static JLabel buildSectionHint(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(111, 101, 84));
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        return label;
    }

    private static JPanel buildSectionHintLabel(JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        label.setForeground(new Color(111, 101, 84));
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        panel.add(label, BorderLayout.CENTER);
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
