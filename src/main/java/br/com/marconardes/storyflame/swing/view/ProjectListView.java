package br.com.marconardes.storyflame.swing.view;

import br.com.marconardes.storyflame.swing.model.Project;
import br.com.marconardes.storyflame.swing.viewmodel.ProjectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ProjectListView extends JPanel {
    private final ProjectViewModel viewModel;
    private final JList<Project> projectJList;
    private final DefaultListModel<Project> projectListModel;

    public ProjectListView(ProjectViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());
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
            }
        });

        projectJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Project selectedProject = projectJList.getSelectedValue();
                // Check if the selected project in ViewModel is already this one to avoid loop
                if (selectedProject != null && selectedProject != viewModel.getSelectedProject()) {
                     viewModel.selectProject(selectedProject);
                } else if (selectedProject == null && viewModel.getSelectedProject() != null) {
                    // If selection is cleared in JList (e.g. list becomes empty after delete)
                    // and ViewModel still has a selected project, update ViewModel.
                    // However, typical JList behavior might not allow clearing selection this way
                    // unless the list is entirely empty.
                    // For now, selectProject(null) is handled by delete operations.
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(projectJList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add Project");
        JButton deleteButton = new JButton("Delete Project");

        addButton.addActionListener(this::addProject);
        deleteButton.addActionListener(this::deleteProject);

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set initial selection in JList based on ViewModel
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
}
