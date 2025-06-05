package br.com.marconardes.storyflame.swing.view;

import br.com.marconardes.storyflame.swing.model.Project;
import br.com.marconardes.storyflame.swing.viewmodel.ProjectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.SpinnerNumberModel;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Frame; // For casting parent window
// GridBagConstraints and GridBagLayout will be used for goalsPanel

// Custom Dialogs
import br.com.marconardes.storyflame.swing.view.PasswordEntryDialog;
import br.com.marconardes.storyflame.swing.view.ManagePasswordDialog;

public class ProjectListView extends JPanel {
    private final ProjectViewModel viewModel;
    private final JList<Project> projectJList;
    private final DefaultListModel<Project> projectListModel;

    // UI Components for Goals
    private JSpinner dailyGoalSpinner;
    private JSpinner totalGoalSpinner;
    private JButton saveGoalsButton;
    private JPanel goalsPanel;

    // UI Components for Progress Display
    private JLabel dailyProgressLabel;
    private JProgressBar dailyProgressBar;
    private JLabel totalProgressLabel;
    private JProgressBar totalProgressBar;

    public ProjectListView(ProjectViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout(5,5)); // Added some gaps
        setPreferredSize(new Dimension(250, 0)); // Give it a preferred width

        projectListModel = new DefaultListModel<>();
        projectJList = new JList<>(projectListModel);
        projectJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Populate initial list
        updateProjectList(viewModel.getProjects());

        // Listen for ViewModel changes (property change listeners remain the same)
        viewModel.addPropertyChangeListener(evt -> {
            if (ProjectViewModel.PROJECTS_PROPERTY.equals(evt.getPropertyName())) {
                if (evt.getNewValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Project> newProjects = (List<Project>) evt.getNewValue();
                    updateProjectList(newProjects);
                }
            } else if (ProjectViewModel.SELECTED_PROJECT_PROPERTY.equals(evt.getPropertyName())) {
                Project selectedProject = (Project) evt.getNewValue();
                projectJList.setSelectedValue(selectedProject, true);
                if (selectedProject != null) {
                    goalsPanel.setVisible(true);
                    saveGoalsButton.setEnabled(true);
                    dailyGoalSpinner.setValue(selectedProject.getDailyWritingGoal());
                    totalGoalSpinner.setValue(selectedProject.getTotalWritingGoal());
                    updateProgressDisplay(selectedProject);
                } else {
                    goalsPanel.setVisible(false);
                    saveGoalsButton.setEnabled(false);
                    updateProgressDisplay(null);
                }
            }
            // Removed redundant PROJECTS_PROPERTY listener block as it was similar to the first one.
            // Ensure the first one covers all necessary updates for PROJECTS_PROPERTY.
        });

        projectJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Project selectedProjectFromList = projectJList.getSelectedValue();
                // This listener primarily updates the ViewModel if the selection changes *in the JList first*.
                // If the project is selected due to ViewModel change, this might be redundant or could
                // potentially cause loops if not handled carefully. The current logic seems to be:
                // JList selection -> viewModel.selectProject() -> viewModel fires SELECTED_PROJECT_PROPERTY
                // -> this updates JList.setSelectedValue and goals panel. This is okay.
                if (selectedProjectFromList != null && selectedProjectFromList != viewModel.getSelectedProject()) {
                    // Do not attempt to open or ask for password here. This is just selection.
                    // The opening action will be via double-click or context menu.
                    viewModel.selectProject(selectedProjectFromList);
                } else if (selectedProjectFromList == null && viewModel.getSelectedProject() != null) {
                    // If JList selection is cleared, and ViewModel still has a selected project,
                    // tell the ViewModel that no project is selected.
                    // However, this might not be desired if the clearing is temporary.
                    // For now, let the viewModel handle this.
                    // viewModel.selectProject(null);
                }
            }
        });

        // Add MouseListener for double-click and right-click context menu
        projectJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int index = projectJList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Project selectedProject = projectJList.getModel().getElementAt(index);
                        openProject(selectedProject);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = projectJList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        projectJList.setSelectedIndex(index); // Select the item under cursor for context menu
                        Project selectedProject = projectJList.getModel().getElementAt(index);
                        showContextMenu(e, selectedProject);
                    }
                }
            }
        });

        // Initialize context menu (it will be populated on demand)
        // final JPopupMenu contextMenu = new JPopupMenu(); // Not needed here, created in showContextMenu

        // (The rest of the JList selection listener and UI setup remains similar)
        // ...
    }

    private void openProject(Project project) {
        if (project == null) return;

        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        if (project.getPasswordHash() != null && !project.getPasswordHash().isEmpty()) {
            PasswordEntryDialog entryDialog = new PasswordEntryDialog(parentFrame,
                    "Enter Password", "Project '" + project.getName() + "' is password protected:");
            // entryDialog.setVisible(true); // PasswordEntryDialog's getPassword() makes it visible
            String passwordAttempt = entryDialog.getPassword();

            if (passwordAttempt != null) { // User clicked OK
                boolean success = viewModel.attemptLoadProtectedProject(project, passwordAttempt);
                if (success) {
                    // Project loaded successfully, MainWindow should react to SELECT_PROJECT_PROPERTY change
                    // (or whatever signal indicates project is fully open for editing)
                    System.out.println("Password correct. Project " + project.getName() + " should be opened.");
                } else {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Incorrect password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            }
            // If passwordAttempt is null, user canceled, do nothing.
        } else {
            // Not password protected, select it (which should trigger main view update)
            viewModel.selectProject(project);
            System.out.println("Project " + project.getName() + " (no password) should be opened.");
        }
    }

    private void showContextMenu(MouseEvent e, Project selectedProject) {
        JPopupMenu contextMenu = new JPopupMenu();
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        // --- Set/Change Password ---
        JMenuItem setChangePasswordItem = new JMenuItem("Set/Change Password...");
        setChangePasswordItem.addActionListener(ae -> {
            boolean isPasswordSet = selectedProject.getPasswordHash() != null && !selectedProject.getPasswordHash().isEmpty();
            ManagePasswordDialog.Action action = isPasswordSet ? ManagePasswordDialog.Action.CHANGE : ManagePasswordDialog.Action.SET;

            ManagePasswordDialog dialog = new ManagePasswordDialog(parentFrame, action, isPasswordSet);
            dialog.setVisible(true); // Show the dialog

            String currentPassword = dialog.getCurrentPassword(); // Will be null if action is SET
            String newPassword = dialog.getNewPassword();       // Will be null if dialog was cancelled

            if (newPassword != null) { // User clicked OK and new password is provided (or cleared for removal if dialog supported it)
                boolean success = false;
                String message;
                if (action == ManagePasswordDialog.Action.SET) {
                    success = viewModel.setProjectPassword(selectedProject, newPassword);
                    message = success ? "Password set successfully." : "Failed to set password.";
                } else { // CHANGE
                    success = viewModel.changeProjectPassword(selectedProject, currentPassword, newPassword);
                    message = success ? "Password changed successfully." : "Failed to change password (check current password?).";
                }
                JOptionPane.showMessageDialog(parentFrame, message,
                        "Password Management", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                if (success) projectJList.repaint(); // Repaint to reflect potential changes (e.g. an icon)
            }
        });
        contextMenu.add(setChangePasswordItem);

        // --- Remove Password ---
        JMenuItem removePasswordItem = new JMenuItem("Remove Password...");
        removePasswordItem.addActionListener(ae -> {
            boolean isPasswordSet = selectedProject.getPasswordHash() != null && !selectedProject.getPasswordHash().isEmpty();
            if (!isPasswordSet) {
                JOptionPane.showMessageDialog(parentFrame, "This project is not password protected.",
                        "Remove Password", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            ManagePasswordDialog dialog = new ManagePasswordDialog(parentFrame, ManagePasswordDialog.Action.REMOVE, true);
            dialog.setVisible(true); // Show the dialog

            String passwordToConfirm = dialog.getCurrentPassword(); // For REMOVE, this field is used for confirmation

            if (passwordToConfirm != null) { // User clicked OK
                boolean success = viewModel.removeProjectPassword(selectedProject, passwordToConfirm);
                String message = success ? "Password removed successfully." : "Failed to remove password (check password?).";
                JOptionPane.showMessageDialog(parentFrame, message,
                        "Password Management", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                if (success) projectJList.repaint();
            }
        });
        contextMenu.add(removePasswordItem);

        // --- Option to Open Project (can be useful if double click is not obvious) ---
        contextMenu.addSeparator();
        JMenuItem openItem = new JMenuItem("Open Project");
        openItem.setFont(openItem.getFont().deriveFont(Font.BOLD)); // Make it bold
        openItem.addActionListener(ae -> openProject(selectedProject));
        contextMenu.add(openItem);


        contextMenu.show(projectJList, e.getX(), e.getY());
    }

    // Original ListSelectionListener - keep for single-click selection updates
    // The selection change itself doesn't trigger password dialogs, only explicit open actions.
    projectJList.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            Project selectedProject = projectJList.getSelectedValue();
            if (selectedProject != null) {
                if (selectedProject != viewModel.getSelectedProject()) {
                    viewModel.selectProject(selectedProject); // This updates ViewModel and triggers property change
                }
                // Update goals panel based on the selection from JList
                goalsPanel.setVisible(true);
                saveGoalsButton.setEnabled(true);
                dailyGoalSpinner.setValue(selectedProject.getDailyWritingGoal());
                totalGoalSpinner.setValue(selectedProject.getTotalWritingGoal());
                updateProgressDisplay(selectedProject);
            } else {
                // No project selected in JList
                goalsPanel.setVisible(false);
                saveGoalsButton.setEnabled(false);
                updateProgressDisplay(null);
                if (viewModel.getSelectedProject() != null) {
                    // If JList is cleared but ViewModel has a project, tell ViewModel.
                    // This might happen if the list is entirely new.
                    // viewModel.selectProject(null);
                }
                    }
                }
            }
        });
        // The above ListSelectionListener might be partially redundant if the PropertyChangeListener
        // for SELECTED_PROJECT_PROPERTY already handles updating the goals panel.
        // It's crucial that viewModel.selectProject() is called when JList selection changes,
        // and that the PropertyChangeListener correctly updates UI elements like the goalsPanel
        // when the ViewModel's selectedProject changes.

        JScrollPane scrollPane = new JScrollPane(projectJList);
        add(scrollPane, BorderLayout.CENTER);

        // --- Goals Panel Setup ---
        goalsPanel = new JPanel(new GridBagLayout());
        goalsPanel.setBorder(BorderFactory.createTitledBorder("Metas do Projeto Selecionado"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); // Padding
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        goalsPanel.add(new JLabel("Meta Diária (palavras):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        SpinnerNumberModel dailyGoalModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 100);
        dailyGoalSpinner = new JSpinner(dailyGoalModel);
        Dimension spinnerSize = new Dimension(100, dailyGoalSpinner.getPreferredSize().height);
        dailyGoalSpinner.setPreferredSize(spinnerSize);
        goalsPanel.add(dailyGoalSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        goalsPanel.add(new JLabel("Meta Total (palavras):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        SpinnerNumberModel totalGoalModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1000);
        totalGoalSpinner = new JSpinner(totalGoalModel);
        totalGoalSpinner.setPreferredSize(spinnerSize);
        goalsPanel.add(totalGoalSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; // Keep save button centered
        saveGoalsButton = new JButton("Salvar Metas");
        saveGoalsButton.addActionListener(e -> saveProjectGoals());
        goalsPanel.add(saveGoalsButton, gbc);

        // Reset gridwidth for subsequent components if it was changed
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_START; // Reset anchor for labels

        // Daily Progress Label
        gbc.gridx = 0; gbc.gridy = 3; // Next row
        goalsPanel.add(new JLabel("Progresso Diário:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        dailyProgressLabel = new JLabel("N/A");
        goalsPanel.add(dailyProgressLabel, gbc);

        // Daily Progress Bar
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        dailyProgressBar = new JProgressBar(0, 100);
        dailyProgressBar.setStringPainted(true);
        dailyProgressBar.setPreferredSize(new Dimension(150, dailyProgressBar.getPreferredSize().height));
        goalsPanel.add(dailyProgressBar, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; // Reset

        // Total Progress Label
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.LINE_START;
        goalsPanel.add(new JLabel("Progresso Total:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        totalProgressLabel = new JLabel("N/A");
        goalsPanel.add(totalProgressLabel, gbc);

        // Total Progress Bar
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        totalProgressBar = new JProgressBar(0, 100);
        totalProgressBar.setStringPainted(true);
        totalProgressBar.setPreferredSize(new Dimension(150, totalProgressBar.getPreferredSize().height));
        goalsPanel.add(totalProgressBar, gbc);

        goalsPanel.setVisible(false); // Initially hidden
        saveGoalsButton.setEnabled(false); // Initially disabled

        // --- Original Button Panel (Add/Delete Project) ---
        JPanel projectActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add Project");
        JButton deleteButton = new JButton("Delete Project");
        addButton.addActionListener(this::addProject);
        deleteButton.addActionListener(this::deleteProject);
        projectActionsPanel.add(addButton);
        projectActionsPanel.add(deleteButton);

        // --- South Panel to hold Goals and Project Actions ---
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(goalsPanel);
        southPanel.add(projectActionsPanel);

        add(southPanel, BorderLayout.SOUTH);

        // Set initial selection and goals panel state
        Project initiallySelected = viewModel.getSelectedProject();
        if (initiallySelected != null) {
            projectJList.setSelectedValue(initiallySelected, true);
        }
    }

    private void updateProjectList(List<Project> projects) {
        projectListModel.clear();
        for (Project project : projects) {
            projectListModel.addElement(project);
        }
        // Restore selection if possible
        Project currentSelectedInVM = viewModel.getSelectedProject();
        if (currentSelectedInVM != null && projects.contains(currentSelectedInVM)) {
            projectJList.setSelectedValue(currentSelectedInVM, true);
        } else if (!projects.isEmpty()){
            // If previous selection is gone, select the first one by default
            // projectJList.setSelectedIndex(0); // This would trigger a selection event.
            // Let the ViewModel decide the new selection, which should then update the JList via property change.
        }
    }

    private void addProject(ActionEvent e) {
        String projectName = JOptionPane.showInputDialog(this, "Enter project name:", "New Project", JOptionPane.PLAIN_MESSAGE);
        if (projectName != null && !projectName.trim().isEmpty()) {
            viewModel.createProject(projectName.trim());
        }
    }

    private void deleteProject(ActionEvent e) {
        Project selectedProject = projectJList.getSelectedValue();
        if (selectedProject != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete project: " + selectedProject.getName() + "?",
                    "Delete Project", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                viewModel.deleteProject(selectedProject.getId());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a project to delete.", "No Project Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveProjectGoals() {
        Project selectedProject = projectJList.getSelectedValue();
        if (selectedProject != null) {
            int dailyGoal = (Integer) dailyGoalSpinner.getValue();
            int totalGoal = (Integer) totalGoalSpinner.getValue();

            // This method will be implemented in ProjectViewModel in a subsequent step.
            // For now, this call assumes it exists or will exist.
            viewModel.updateProjectGoals(selectedProject.getId(), dailyGoal, totalGoal);
            updateProgressDisplay(selectedProject); // Update progress after saving goals

            JOptionPane.showMessageDialog(this, "Metas salvas para o projeto: " + selectedProject.getName(), "Metas Salvas", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateProgressDisplay(Project project) {
        if (project == null) {
            dailyProgressLabel.setText("N/A");
            dailyProgressBar.setValue(0);
            dailyProgressBar.setMaximum(100); // Reset max to a default
            dailyProgressBar.setString("N/A");

            totalProgressLabel.setText("N/A");
            totalProgressBar.setValue(0);
            totalProgressBar.setMaximum(100); // Reset max
            totalProgressBar.setString("N/A");
            return;
        }

        // Metas
        int dailyGoal = project.getDailyWritingGoal();
        int totalGoal = project.getTotalWritingGoal();

        // Contagens Atuais
        int currentDailyWords = viewModel.getWordCountForDate(project, LocalDate.now());
        int currentTotalWords = viewModel.calculateTotalWordCount(project);

        // Atualizar UI para Progresso Diário
        dailyProgressLabel.setText(String.format("%d / %d palavras", currentDailyWords, dailyGoal));
        dailyProgressBar.setMaximum(dailyGoal > 0 ? dailyGoal : 1);
        dailyProgressBar.setValue(Math.min(currentDailyWords, dailyGoal > 0 ? dailyGoal : currentDailyWords));
        if (dailyGoal > 0) {
            dailyProgressBar.setString(String.format("%.0f%%", (Math.min(1.0, (double)currentDailyWords / dailyGoal)) * 100.0));
        } else {
            dailyProgressBar.setString(currentDailyWords > 0 ? "Meta não definida" : "0/0");
        }


        // Atualizar UI para Progresso Total
        totalProgressLabel.setText(String.format("%d / %d palavras", currentTotalWords, totalGoal));
        totalProgressBar.setMaximum(totalGoal > 0 ? totalGoal : 1);
        totalProgressBar.setValue(Math.min(currentTotalWords, totalGoal > 0 ? totalGoal : currentTotalWords));
        if (totalGoal > 0) {
            totalProgressBar.setString(String.format("%.0f%%", (Math.min(1.0, (double)currentTotalWords / totalGoal)) * 100.0));
        } else {
            totalProgressBar.setString(currentTotalWords > 0 ? "Meta não definida" : "0/0");
        }
    }
}
