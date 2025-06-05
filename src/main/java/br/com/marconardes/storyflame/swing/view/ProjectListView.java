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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Frame; // For casting parent window

// Custom Dialogs
import br.com.marconardes.storyflame.swing.view.PasswordEntryDialog;
import br.com.marconardes.storyflame.swing.view.ManagePasswordDialog;
// GridBagConstraints and GridBagLayout will be used for goalsPanel

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

        // Add MouseListener for double-click and right-click context menu
        projectJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int index = projectJList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Project selectedProject = projectJList.getModel().getElementAt(index);
                        if (selectedProject != null) { // Ensure project is not null
                           openProject(selectedProject);
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = projectJList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        // Ensure the item under the cursor is selected before showing the context menu
                        if (projectJList.getSelectedIndex() != index) {
                            projectJList.setSelectedIndex(index);
                        }
                        Project selectedProject = projectJList.getModel().getElementAt(index);
                        if (selectedProject != null) { // Ensure project is not null
                            showContextMenu(e, selectedProject);
                        }
                    }
                }
            }
        });

        // Listen for ViewModel changes
        viewModel.addPropertyChangeListener(evt -> {
            if (ProjectViewModel.PROJECTS_PROPERTY.equals(evt.getPropertyName())) {
                // Type safety: Ensure the new value is a List<Project>
                if (evt.getNewValue() instanceof List) {
                    @SuppressWarnings("unchecked") // Checked by instanceof
                    List<Project> newProjects = (List<Project>) evt.getNewValue();
                    updateProjectList(newProjects);
                }
            } else if (ProjectViewModel.SELECTED_PROJECT_PROPERTY.equals(evt.getPropertyName())) {
                Project selectedProject = (Project) evt.getNewValue();
                projectJList.setSelectedValue(selectedProject, true);
                // Update goals panel when ViewModel's selected project changes externally
                if (selectedProject != null) {
                    goalsPanel.setVisible(true);
                    saveGoalsButton.setEnabled(true);
                    dailyGoalSpinner.setValue(selectedProject.getDailyWritingGoal());
                    totalGoalSpinner.setValue(selectedProject.getTotalWritingGoal());
                    updateProgressDisplay(selectedProject); // Call to update progress display
                } else {
                    goalsPanel.setVisible(false);
                    saveGoalsButton.setEnabled(false);
                    updateProgressDisplay(null); // Call to update progress display
                }
            } else if (ProjectViewModel.PROJECTS_PROPERTY.equals(evt.getPropertyName())) {
                // Type safety: Ensure the new value is a List<Project>
                if (evt.getNewValue() instanceof List) {
                    @SuppressWarnings("unchecked") // Checked by instanceof
                    List<Project> newProjects = (List<Project>) evt.getNewValue();
                    updateProjectList(newProjects); // This will update JList & may change selection

                    // After project list updates, re-evaluate selected project for progress display
                    Project currentSelectedInVM = viewModel.getSelectedProject();
                    if (currentSelectedInVM != null) { // If a project remains selected or is newly selected
                        goalsPanel.setVisible(true);
                        saveGoalsButton.setEnabled(true);
                        dailyGoalSpinner.setValue(currentSelectedInVM.getDailyWritingGoal());
                        totalGoalSpinner.setValue(currentSelectedInVM.getTotalWritingGoal());
                        updateProgressDisplay(currentSelectedInVM);
                    } else { // If no project is selected after list update
                        goalsPanel.setVisible(false);
                        saveGoalsButton.setEnabled(false);
                        updateProgressDisplay(null);
                    }
                }
            }
        });

        projectJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Project selectedProject = projectJList.getSelectedValue();
                if (selectedProject != null) {
                    if (selectedProject != viewModel.getSelectedProject()) {
                        viewModel.selectProject(selectedProject);
                    }
                    // This part is now handled by the property change listener above
                    // goalsPanel.setVisible(true);
                    // saveGoalsButton.setEnabled(true);
                    // dailyGoalSpinner.setValue(selectedProject.getDailyWritingGoal());
                    // totalGoalSpinner.setValue(selectedProject.getTotalWritingGoal());
                } else {
                    // goalsPanel.setVisible(false);
                    // saveGoalsButton.setEnabled(false);
                    if (viewModel.getSelectedProject() != null) {
                         // This case might occur if the list is cleared and JList deselects.
                         // viewModel.selectProject(null); // Or let ViewModel handle this via project list changes.
                    }
                }
            }
        });

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

    private void openProject(Project project) {
        if (project == null) return;

        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        // It's good practice to handle parentFrame being null, though for a visible component it's unlikely.
        // JDialog can accept a null parent, but it might not behave as ideally (e.g., regarding modality or positioning).

        if (project.getPasswordHash() != null && !project.getPasswordHash().isEmpty()) {
            PasswordEntryDialog entryDialog = new PasswordEntryDialog(parentFrame,
                    "Enter Password", "Project '" + project.getName() + "' is password protected:");
            String passwordAttempt = entryDialog.getPassword(); // This makes the dialog visible and blocks.

            if (passwordAttempt != null) { // User clicked OK (not Cancel)
                boolean success = viewModel.attemptLoadProtectedProject(project, passwordAttempt);
                if (success) {
                    // Project successfully loaded into ViewModel.
                    // UI should update based on ViewModel's property changes (e.g., SELECTED_PROJECT_PROPERTY).
                    System.out.println("Password correct for project: " + project.getName() + ". ViewModel updated.");
                    // Any navigation or view change is typically handled by a main controller listening to ViewModel.
                } else {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Incorrect password for project '" + project.getName() + "'.",
                            "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            }
            // If passwordAttempt is null, user canceled the dialog; do nothing further.
        } else {
            // Not password protected, so select it directly in the ViewModel.
            // This should trigger the SELECTED_PROJECT_PROPERTY change, and other parts of the UI will react.
            viewModel.selectProject(project);
            System.out.println("Opening project (no password): " + project.getName() + ". ViewModel updated.");
        }
    }

    private void showContextMenu(MouseEvent e, Project selectedProject) {
        if (selectedProject == null) return;

        JPopupMenu contextMenu = new JPopupMenu();
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        // --- Set/Change Password ---
        JMenuItem setChangePasswordItem = new JMenuItem("Set/Change Password...");
        setChangePasswordItem.addActionListener(ae -> {
            boolean isPasswordSet = selectedProject.getPasswordHash() != null && !selectedProject.getPasswordHash().isEmpty();
            ManagePasswordDialog.Action action = isPasswordSet ? ManagePasswordDialog.Action.CHANGE : ManagePasswordDialog.Action.SET;

            ManagePasswordDialog dialog = new ManagePasswordDialog(parentFrame, action, isPasswordSet);
            dialog.setVisible(true);

            String currentPassword = dialog.getCurrentPassword();
            String newPassword = dialog.getNewPassword();

            if (newPassword != null) {
                boolean success = false;
                String message;
                if (action == ManagePasswordDialog.Action.SET) {
                    success = viewModel.setProjectPassword(selectedProject, newPassword);
                    message = success ? "Password set successfully for '" + selectedProject.getName() + "'."
                                      : "Failed to set password for '" + selectedProject.getName() + "'.";
                } else { // CHANGE
                    if (currentPassword == null) { // Should not happen if dialog logic is correct for CHANGE
                         JOptionPane.showMessageDialog(parentFrame, "Current password was not provided.", "Error", JOptionPane.ERROR_MESSAGE);
                         return;
                    }
                    success = viewModel.changeProjectPassword(selectedProject, currentPassword, newPassword);
                    message = success ? "Password changed successfully for '" + selectedProject.getName() + "'."
                                      : "Failed to change password for '" + selectedProject.getName() + "'. (Check current password?)";
                }
                JOptionPane.showMessageDialog(parentFrame, message,
                        "Password Management", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                if (success) projectJList.repaint();
            }
        });
        contextMenu.add(setChangePasswordItem);

        // --- Remove Password ---
        JMenuItem removePasswordItem = new JMenuItem("Remove Password...");
        removePasswordItem.setEnabled(selectedProject.getPasswordHash() != null && !selectedProject.getPasswordHash().isEmpty()); // Enable only if password is set
        removePasswordItem.addActionListener(ae -> {
            // Re-check, as state might change, though setEnabled should cover typical cases
            if (selectedProject.getPasswordHash() == null || selectedProject.getPasswordHash().isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "This project is not password protected.",
                        "Remove Password", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            ManagePasswordDialog dialog = new ManagePasswordDialog(parentFrame, ManagePasswordDialog.Action.REMOVE, true);
            dialog.setVisible(true);

            String passwordToConfirm = dialog.getCurrentPassword();

            if (passwordToConfirm != null) {
                boolean success = viewModel.removeProjectPassword(selectedProject, passwordToConfirm);
                String message = success ? "Password removed successfully from '" + selectedProject.getName() + "'."
                                         : "Failed to remove password for '" + selectedProject.getName() + "'. (Check password?)";
                JOptionPane.showMessageDialog(parentFrame, message,
                        "Password Management", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                if (success) projectJList.repaint();
            }
        });
        contextMenu.add(removePasswordItem);

        contextMenu.addSeparator();
        JMenuItem openItemMenu = new JMenuItem("Open Project");
        openItemMenu.setFont(openItemMenu.getFont().deriveFont(Font.BOLD));
        openItemMenu.addActionListener(ae -> openProject(selectedProject));
        contextMenu.add(openItemMenu);

        contextMenu.show(projectJList, e.getX(), e.getY());
    }
}
