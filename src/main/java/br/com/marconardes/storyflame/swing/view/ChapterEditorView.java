package br.com.marconardes.storyflame.swing.view;

import br.com.marconardes.storyflame.swing.model.Chapter;
import br.com.marconardes.storyflame.swing.model.Project;
import br.com.marconardes.storyflame.swing.viewmodel.ProjectViewModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
// javax.swing.text.Utilities is not needed here anymore as logic moved
import java.awt.*;
import java.awt.event.ActionEvent;
import br.com.marconardes.storyflame.swing.util.MarkdownFormatter;
import br.com.marconardes.storyflame.swing.util.WordCounterUtil;
// No Timer import needed if using lambda for ActionEvent, but good to have if complex.
// For this case, lambda `this::performAutoSave` is fine.
// import javax.swing.Timer; // Not strictly necessary due to lambda usage if performAutoSave matches ActionEvent handler signature


public class ChapterEditorView extends JPanel {
    private final ProjectViewModel viewModel;
    private ChapterEditorListener listener;

    private Project currentProject;
    private Chapter currentChapter;

    private JTextField titleField;
    private JTextArea summaryArea;
    private JTextArea contentArea;
    private JLabel editorTitleLabel;
    private JLabel wordCountLabel; // Added for word count

    private javax.swing.Timer autoSaveTimer; // Fully qualify to avoid import if preferred, or add import
    private static final int AUTOSAVE_DELAY = 1500; // milliseconds (1.5 seconds)


    public ChapterEditorView(ProjectViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        editorTitleLabel = new JLabel("Edit Chapter", SwingConstants.CENTER);
        editorTitleLabel.setFont(editorTitleLabel.getFont().deriveFont(Font.BOLD, 16f));
        add(editorTitleLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        // Title
        JPanel titlePanel = new JPanel(new BorderLayout(5,0));
        titlePanel.add(new JLabel("Title:"), BorderLayout.WEST);
        titleField = new JTextField();
        titlePanel.add(titleField, BorderLayout.CENTER);
        fieldsPanel.add(titlePanel);
        fieldsPanel.add(Box.createRigidArea(new Dimension(0,10)));


        // Summary
        fieldsPanel.add(new JLabel("Summary:"));
        summaryArea = new JTextArea(5, 20);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        JScrollPane summaryScrollPane = new JScrollPane(summaryArea);
        fieldsPanel.add(summaryScrollPane);
        fieldsPanel.add(Box.createRigidArea(new Dimension(0,10)));

        // Content
        fieldsPanel.add(new JLabel("Content:"));

        // Create formatting toolbar
        JToolBar formatToolbar = new JToolBar();
        formatToolbar.setFloatable(false);
        formatToolbar.setAlignmentX(Component.LEFT_ALIGNMENT); // Align toolbar to the left in BoxLayout

        JButton boldButton = new JButton("Bold");
        boldButton.addActionListener(e -> applyMarkdownFormat("**", "**", false));
        formatToolbar.add(boldButton);

        JButton italicButton = new JButton("Italic");
        italicButton.addActionListener(e -> applyMarkdownFormat("*", "*", false));
        formatToolbar.add(italicButton);

        JButton underlineButton = new JButton("U"); // Underline button
        underlineButton.addActionListener(e -> applyMarkdownFormat("<u>", "</u>", false));
        formatToolbar.add(underlineButton);

        formatToolbar.addSeparator();

        JButton h1Button = new JButton("H1");
        h1Button.addActionListener(e -> applyMarkdownFormat("# ", null, true));
        formatToolbar.add(h1Button);

        JButton h2Button = new JButton("H2");
        h2Button.addActionListener(e -> applyMarkdownFormat("## ", null, true));
        formatToolbar.add(h2Button);

        JButton h3Button = new JButton("H3");
        h3Button.addActionListener(e -> applyMarkdownFormat("### ", null, true));
        formatToolbar.add(h3Button);

        // Panel to hold toolbar and contentArea
        JPanel contentPanelWrapper = new JPanel(new BorderLayout());
        contentPanelWrapper.add(formatToolbar, BorderLayout.NORTH);

        contentArea = new JTextArea(15, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentPanelWrapper.add(contentScrollPane, BorderLayout.CENTER);
        contentPanelWrapper.setAlignmentX(Component.LEFT_ALIGNMENT); // Align wrapper to the left

        fieldsPanel.add(contentPanelWrapper);

        // Word Count Label
        fieldsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        wordCountLabel = new JLabel("Palavras: 0");
        wordCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(wordCountLabel);

        add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(this::saveChapter);
        cancelButton.addActionListener(this::cancelEdit);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initialize auto-save timer
        autoSaveTimer = new javax.swing.Timer(AUTOSAVE_DELAY, this::performAutoSave);
        autoSaveTimer.setRepeats(false); // Only fire once

        // Create and attach document listener for auto-save
        DocumentListener autoSaveListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleTextChange(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                handleTextChange(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                // Plain text components do not typically fire this
                handleTextChange(e);
            }

            private void handleTextChange(DocumentEvent e) {
                restartAutoSaveTimer();
                // Update word count only if the change is from contentArea
                if (e.getDocument() == contentArea.getDocument()) {
                    updateWordCount();
                }
            }
        };

        titleField.getDocument().addDocumentListener(autoSaveListener); // Might trigger word count if not handled
        summaryArea.getDocument().addDocumentListener(autoSaveListener); // Might trigger word count if not handled

        // Specific listener for contentArea to ensure word count updates correctly
        // or modify the shared listener to check document source.
        // For simplicity, the shared listener was modified above.
        contentArea.getDocument().addDocumentListener(autoSaveListener);
    }

    private void updateWordCount() {
        if (contentArea == null || wordCountLabel == null) return;
        String text = contentArea.getText();
        int count = WordCounterUtil.countWords(text);
        wordCountLabel.setText("Palavras: " + count);
    }

    private void applyMarkdownFormat(String syntaxOpen, String syntaxClose, boolean isPrefix) {
        String currentText = contentArea.getText();
        int selectionStart = contentArea.getSelectionStart();
        int selectionEnd = contentArea.getSelectionEnd();

        // Handle case where JTextArea might be empty and getSelectionStart/End return 0
        // but we want to ensure it's treated as a valid caret position.
        if (currentText.isEmpty()) {
            selectionStart = 0;
            selectionEnd = 0;
        }

        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(currentText, selectionStart, selectionEnd, syntaxOpen, syntaxClose, isPrefix);

        contentArea.setText(result.newText); // This triggers DocumentListener for auto-save

        // Try to set selection, protect against invalid indices
        try {
            if (result.newSelectionStart >=0 && result.newSelectionEnd >= result.newSelectionStart && result.newSelectionEnd <= result.newText.length()) {
                 contentArea.setSelectionStart(result.newSelectionStart);
                 contentArea.setSelectionEnd(result.newSelectionEnd);
            } else if (result.newSelectionStart >=0 && result.newSelectionStart <= result.newText.length()) { // Fallback for caret
                 contentArea.setCaretPosition(result.newSelectionStart);
            }
        } catch (IllegalArgumentException iae) {
            System.err.println("Error setting selection after format: " + iae.getMessage());
            // Let caret be at the end or start if error
        }
        contentArea.requestFocusInWindow();
    }

    private void restartAutoSaveTimer() {
        autoSaveTimer.restart();
    }

    private void performAutoSave(ActionEvent e) {
        if (currentProject != null && currentChapter != null) {
            // System.out.println("Auto-saving chapter: " + currentChapter.getTitle() + " for project " + currentProject.getName()); // For debugging

            String newTitle = titleField.getText().trim();
            String newSummary = summaryArea.getText().trim();
            String newContent = contentArea.getText().trim();

            if (newTitle.isEmpty()) {
                // System.err.println("Auto-save: Title cannot be empty. Save skipped.");
                // Optionally provide non-modal feedback (e.g., status bar update) if desired.
                // For now, just skip saving if title is empty during auto-save.
                return;
            }

            // Check if anything actually changed to avoid redundant updates
            boolean titleChanged = !newTitle.equals(currentChapter.getTitle());
            boolean summaryChanged = !newSummary.equals(currentChapter.getSummary());
            boolean contentChanged = !newContent.equals(currentChapter.getContent());

            if (titleChanged) {
                viewModel.updateChapterTitle(currentProject.getId(), currentChapter.getId(), newTitle);
                currentChapter.setTitle(newTitle); // Update local copy to prevent re-triggering save
            }
            if (summaryChanged) {
                viewModel.updateChapterSummary(currentProject.getId(), currentChapter.getId(), newSummary);
                currentChapter.setSummary(newSummary); // Update local copy
            }
            if (contentChanged) {
                viewModel.updateChapterContent(currentProject.getId(), currentChapter.getId(), newContent);
                currentChapter.setContent(newContent); // Update local copy
            }

            if (titleChanged || summaryChanged || contentChanged) {
                 System.out.println("Auto-saved changes for chapter: " + currentChapter.getTitle());
            }
        }
    }

    public void setEditorListener(ChapterEditorListener listener) {
        this.listener = listener;
    }

    public void editChapter(Project project, Chapter chapter) {
        autoSaveTimer.stop(); // Stop any pending auto-save from previously edited chapter
        this.currentProject = project;
        this.currentChapter = chapter;

        if (chapter != null) {
            editorTitleLabel.setText("Edit Chapter: " + chapter.getTitle());
            titleField.setText(chapter.getTitle());
            summaryArea.setText(chapter.getSummary());
            contentArea.setText(chapter.getContent()); // This will trigger DocumentListener, so updateWordCount() is called
        } else {
            // Should not happen if called correctly, but good to handle
            editorTitleLabel.setText("Error: Chapter not provided");
            titleField.setText("");
            summaryArea.setText("");
            contentArea.setText("");
            updateWordCount(); // Also update for empty/error state
        }
        // Explicit call to update word count when chapter is first loaded,
        // as setText might not fire listener immediately or before component is fully realized.
        // However, the listener on contentArea.setText() should handle this.
        // To be safe, an explicit call after setting text ensures it.
        // updateWordCount(); // This is now handled by the DocumentListener if setText fires it.
        // Let's rely on the listener for now, if problematic, can add explicit call here.
    }

    private void saveChapter(ActionEvent e) {
        autoSaveTimer.stop(); // Stop auto-save timer on manual save
        if (currentProject != null && currentChapter != null) {
            String newTitle = titleField.getText().trim();
            String newSummary = summaryArea.getText().trim();
            String newContent = contentArea.getText().trim();

            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            viewModel.updateChapterTitle(currentProject.getId(), currentChapter.getId(), newTitle);
            viewModel.updateChapterSummary(currentProject.getId(), currentChapter.getId(), newSummary);
            viewModel.updateChapterContent(currentProject.getId(), currentChapter.getId(), newContent);

            if (listener != null) {
                listener.onEditorClosed(true);
            }
        }
    }

    private void cancelEdit(ActionEvent e) {
        if (listener != null) {
            listener.onEditorClosed(false);
        }
    }
}
