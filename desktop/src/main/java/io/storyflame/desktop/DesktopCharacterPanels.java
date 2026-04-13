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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

final class DesktopCharacterPanels {
    private DesktopCharacterPanels() {
    }

    static JPanel buildCharacterPanel(
            JTextField characterSearchField,
            JList<String> characterList,
            JLabel characterCountLabel,
            JLabel integrityLabel,
            JLabel characterDetailModeLabel,
            JTextField characterNameField,
            JLabel selectedCharacterScenesLabel,
            JLabel selectedCharacterPointOfViewLabel,
            JTextArea characterDescriptionArea,
            JLabel selectedCharacterTagsLabel,
            JLabel characterDraftHintLabel,
            Runnable addCharacter,
            Runnable deleteCharacter,
            Runnable applyCharacterNameUpdate,
            Runnable cancelCharacterDraft,
            Runnable clearCharacterSearch
    ) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(244, 239, 231));

        JPanel header = new JPanel(new BorderLayout(6, 6));
        header.setOpaque(false);
        header.add(characterSearchField, BorderLayout.NORTH);
        header.add(buildSectionHint("Crie primeiro. Relacoes e acoes avancadas aparecem depois."), BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout(8, 8));
        listPanel.setOpaque(false);
        listPanel.add(buildCharacterToolbar(addCharacter, deleteCharacter), BorderLayout.NORTH);
        listPanel.add(new JScrollPane(characterList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                listPanel,
                buildCharacterDetailsPanel(
                        characterDetailModeLabel,
                        characterNameField,
                        selectedCharacterScenesLabel,
                        selectedCharacterPointOfViewLabel,
                        characterDescriptionArea,
                        selectedCharacterTagsLabel,
                        characterDraftHintLabel,
                        applyCharacterNameUpdate,
                        cancelCharacterDraft,
                        clearCharacterSearch
                )
        );
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.5);
        root.add(splitPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        footer.add(buildBadge(characterCountLabel));
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private static JPanel buildCharacterToolbar(
            Runnable addCharacter,
            Runnable deleteCharacter
    ) {
        JPanel panel = new JPanel();
        JButton addButton = new JButton("Novo personagem");
        JButton deleteButton = new JButton("Excluir personagem");
        addButton.setName("addCharacterButton");
        deleteButton.setName("deleteCharacterButton");
        addButton.addActionListener(event -> addCharacter.run());
        deleteButton.addActionListener(event -> deleteCharacter.run());
        panel.add(addButton);
        panel.add(deleteButton);
        return panel;
    }

    private static JPanel buildCharacterDetailsPanel(
            JLabel characterDetailModeLabel,
            JTextField characterNameField,
            JLabel selectedCharacterScenesLabel,
            JLabel selectedCharacterPointOfViewLabel,
            JTextArea characterDescriptionArea,
            JLabel selectedCharacterTagsLabel,
            JLabel characterDraftHintLabel,
            Runnable applyCharacterNameUpdate,
            Runnable cancelCharacterDraft,
            Runnable clearCharacterSearch
    ) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Ficha do personagem"));

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(buildSectionHintLabel(characterDetailModeLabel), BorderLayout.NORTH);
        header.add(characterNameField, BorderLayout.CENTER);

        header.add(buildSectionHintLabel(characterDraftHintLabel), BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(new JScrollPane(characterDescriptionArea), BorderLayout.CENTER);
        JPanel centerFooter = new JPanel(new GridLayout(1, 0, 8, 0));
        centerFooter.setOpaque(false);
        centerFooter.add(buildBadge(selectedCharacterScenesLabel));
        centerFooter.add(buildBadge(selectedCharacterPointOfViewLabel));
        centerFooter.add(buildBadge(selectedCharacterTagsLabel));
        center.add(centerFooter, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        JButton updateNameButton = new JButton("Salvar personagem");
        JButton cancelButton = new JButton("Cancelar");
        JButton clearSearchButton = new JButton("Limpar filtro");
        updateNameButton.setName("saveCharacterButton");
        cancelButton.setName("cancelCharacterDraftButton");
        clearSearchButton.setName("clearCharacterSearchButton");
        updateNameButton.addActionListener(event -> applyCharacterNameUpdate.run());
        cancelButton.addActionListener(event -> cancelCharacterDraft.run());
        clearSearchButton.addActionListener(event -> clearCharacterSearch.run());
        footer.add(updateNameButton);
        footer.add(cancelButton);
        footer.add(clearSearchButton);
        panel.add(footer, BorderLayout.SOUTH);
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
