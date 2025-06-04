package br.com.marconardes.storyflame.swing.view;

import br.com.marconardes.storyflame.swing.model.Chapter;
import br.com.marconardes.storyflame.swing.model.Project;
import br.com.marconardes.storyflame.swing.viewmodel.ProjectViewModel;
// Import ChapterSelectionListener
import br.com.marconardes.storyflame.swing.view.ChapterSelectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class ChapterSectionView extends JPanel {
    private final ProjectViewModel viewModel;
    private final JList<Chapter> chapterJList;
    private final DefaultListModel<Chapter> chapterListModel;
    private final JLabel titleLabel;
    private final JButton addChapterButton;
    private final JButton editChapterButton; // Placeholder for now
    private final JButton deleteChapterButton; // Placeholder for now
    private ChapterSelectionListener chapterSelectionListener; // Add listener reference

    public ChapterSectionView(ProjectViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());

        titleLabel = new JLabel("No Project Selected", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        add(titleLabel, BorderLayout.NORTH);

        chapterListModel = new DefaultListModel<>();
        chapterJList = new JList<>(chapterListModel);
        chapterJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        chapterJList.addListSelectionListener(listSelectionEvent -> {
            boolean hasSelection = chapterJList.getSelectedIndex() != -1;
            boolean projectSelected = viewModel.getSelectedProject() != null;
            editChapterButton.setEnabled(hasSelection && projectSelected);
            deleteChapterButton.setEnabled(hasSelection && projectSelected);
        });


        JScrollPane scrollPane = new JScrollPane(chapterJList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addChapterButton = new JButton("Add Chapter");
        editChapterButton = new JButton("Edit Chapter"); // Will be more specific later
        deleteChapterButton = new JButton("Delete Chapter");

        addChapterButton.addActionListener(this::addChapter);
        editChapterButton.addActionListener(this::editChapter);
        deleteChapterButton.addActionListener(this::deleteChapter);

        buttonPanel.add(addChapterButton);
        buttonPanel.add(editChapterButton);
        buttonPanel.add(deleteChapterButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initial state
        updateViewForProject(viewModel.getSelectedProject());
        updateChapterList(viewModel.getSelectedProjectChapters());

        // Listen for ViewModel changes
        viewModel.addPropertyChangeListener(evt -> {
            if (ProjectViewModel.SELECTED_PROJECT_PROPERTY.equals(evt.getPropertyName())) {
                Project selectedProject = (Project) evt.getNewValue();
                updateViewForProject(selectedProject);
                // Chapters will be updated by the SELECTED_PROJECT_CHAPTERS_PROPERTY event
            } else if (ProjectViewModel.SELECTED_PROJECT_CHAPTERS_PROPERTY.equals(evt.getPropertyName())) {
                 if (evt.getNewValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Chapter> newChapters = (List<Chapter>) evt.getNewValue();
                    updateChapterList(newChapters);
                } else {
                    updateChapterList(Collections.emptyList());
                }
            }
        });
    }

    private void updateViewForProject(Project project) {
        if (project != null) {
            titleLabel.setText("Chapters for: " + project.getName());
            addChapterButton.setEnabled(true);
            // Edit/Delete buttons enablement depends on chapter selection (and project existence)
            boolean hasSelection = chapterJList.getSelectedIndex() != -1;
            editChapterButton.setEnabled(hasSelection);
            deleteChapterButton.setEnabled(hasSelection);
        } else {
            titleLabel.setText("No Project Selected");
            chapterListModel.clear();
            addChapterButton.setEnabled(false);
            editChapterButton.setEnabled(false);
            deleteChapterButton.setEnabled(false);
        }
    }

    private void updateChapterList(List<Chapter> chapters) {
        chapterListModel.clear();
        if (chapters != null) {
            for (Chapter chapter : chapters) {
                chapterListModel.addElement(chapter);
            }
        }
        // After updating list, re-evaluate button states based on selection
        boolean hasSelection = chapterJList.getSelectedIndex() != -1;
        boolean projectSelected = viewModel.getSelectedProject() != null;
        editChapterButton.setEnabled(hasSelection && projectSelected);
        deleteChapterButton.setEnabled(hasSelection && projectSelected);
    }

    private void addChapter(ActionEvent e) {
        Project selectedProject = viewModel.getSelectedProject();
        if (selectedProject == null) {
            JOptionPane.showMessageDialog(this, "Please select a project first.", "No Project Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String chapterTitle = JOptionPane.showInputDialog(this, "Enter chapter title:", "New Chapter", JOptionPane.PLAIN_MESSAGE);
        if (chapterTitle != null && !chapterTitle.trim().isEmpty()) {
            viewModel.addNewChapterToProject(selectedProject.getId(), chapterTitle.trim());
        }
    }

    private void editChapter(ActionEvent e) {
        Chapter selectedChapter = chapterJList.getSelectedValue();
        // Project selectedProject = viewModel.getSelectedProject(); // viewModel knows this
        if (selectedChapter != null && viewModel.getSelectedProject() != null) {
            if (chapterSelectionListener != null) {
                chapterSelectionListener.onEditChapterRequested(selectedChapter);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a chapter to edit.", "No Chapter Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void setChapterSelectionListener(ChapterSelectionListener listener) {
        this.chapterSelectionListener = listener;
    }

    private void deleteChapter(ActionEvent e) {
        Chapter selectedChapter = chapterJList.getSelectedValue();
        Project selectedProject = viewModel.getSelectedProject();

        if (selectedProject != null && selectedChapter != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete chapter: " + selectedChapter.getTitle() + "?",
                    "Delete Chapter", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                viewModel.deleteChapter(selectedProject.getId(), selectedChapter.getId());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a chapter to delete.", "No Chapter Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
