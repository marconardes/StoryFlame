package br.com.marconardes.storyflame.swing.view;

import br.com.marconardes.storyflame.swing.model.Chapter;
import br.com.marconardes.storyflame.swing.model.Project;
import br.com.marconardes.storyflame.swing.viewmodel.ProjectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChapterEditorView extends JPanel {
    private final ProjectViewModel viewModel;
    private ChapterEditorListener listener;

    private Project currentProject;
    private Chapter currentChapter;

    private JTextField titleField;
    private JTextArea summaryArea;
    private JTextArea contentArea;
    private JLabel editorTitleLabel;


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
        contentArea = new JTextArea(15, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        fieldsPanel.add(contentScrollPane);

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
    }

    public void setEditorListener(ChapterEditorListener listener) {
        this.listener = listener;
    }

    public void editChapter(Project project, Chapter chapter) {
        this.currentProject = project;
        this.currentChapter = chapter;

        if (chapter != null) {
            editorTitleLabel.setText("Edit Chapter: " + chapter.getTitle());
            titleField.setText(chapter.getTitle());
            summaryArea.setText(chapter.getSummary());
            contentArea.setText(chapter.getContent());
        } else {
            // Should not happen if called correctly, but good to handle
            editorTitleLabel.setText("Error: Chapter not provided");
            titleField.setText("");
            summaryArea.setText("");
            contentArea.setText("");
        }
    }

    private void saveChapter(ActionEvent e) {
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
