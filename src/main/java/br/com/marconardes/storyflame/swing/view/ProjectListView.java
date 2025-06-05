package br.com.marconardes.storyflame.swing.view;

import br.com.marconardes.storyflame.swing.model.Project;
import br.com.marconardes.storyflame.swing.viewmodel.ProjectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.SpinnerNumberModel;
import javax.swing.BorderFactory;
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

    public ProjectListView(ProjectViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout(5,5)); // Added some gaps
        setPreferredSize(new Dimension(250, 0)); // Give it a preferred width

        projectListModel = new DefaultListModel<>();
        projectJList = new JList<>(projectListModel);
        projectJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Populate initial list
        updateProjectList(viewModel.getProjects());

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
                } else {
                    goalsPanel.setVisible(false);
                    saveGoalsButton.setEnabled(false);
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

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        saveGoalsButton = new JButton("Salvar Metas");
        saveGoalsButton.addActionListener(e -> saveProjectGoals()); // Changed to lambda
        goalsPanel.add(saveGoalsButton, gbc);

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

            JOptionPane.showMessageDialog(this, "Metas salvas para o projeto: " + selectedProject.getName(), "Metas Salvas", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
