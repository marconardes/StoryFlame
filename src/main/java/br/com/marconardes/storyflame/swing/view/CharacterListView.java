package br.com.marconardes.storyflame.swing.view;

import br.com.marconardes.storyflame.swing.model.Character;
import br.com.marconardes.storyflame.swing.model.Project;
import br.com.marconardes.storyflame.swing.viewmodel.ProjectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class CharacterListView extends JPanel {

    private final ProjectViewModel viewModel;
    // private final Project currentProject; // Removed field
    private final DefaultListModel<Character> characterListModel;
    private final JList<Character> characterJList;

    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    // private JButton closeButton; // Removed field

    public CharacterListView(ProjectViewModel viewModel) { // Changed constructor
        this.viewModel = viewModel;
        // this.currentProject = project; // Removed assignment
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        characterListModel = new DefaultListModel<>();
        characterJList = new JList<>(characterListModel);
        characterJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadCharacters();

        JScrollPane scrollPane = new JScrollPane(characterJList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        // closeButton = new JButton("Close"); // Removed

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        // buttonPanel.add(Box.createHorizontalStrut(20)); // Some spacing // Optional, can keep
        // buttonPanel.add(closeButton); // Removed

        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addCharacter());
        editButton.addActionListener(e -> editCharacter());
        deleteButton.addActionListener(e -> deleteCharacter());
        // closeButton.addActionListener(e -> closeView(owner)); // Removed

        // Initial state of buttons & listener for ViewModel changes
        updateButtonStates();
        characterJList.addListSelectionListener(e -> updateButtonStates());

        this.viewModel.addPropertyChangeListener(evt -> {
            if (ProjectViewModel.SELECTED_PROJECT_PROPERTY.equals(evt.getPropertyName())) {
                loadCharacters();
            }
        });
        loadCharacters(); // Initial load
    }

    private void loadCharacters() {
        Project currentProject = viewModel.getSelectedProject();
        characterListModel.clear();
        if (currentProject != null) {
            List<Character> characters = viewModel.getProjectCharacters(currentProject.getId());
            if (characters != null) {
                for (Character character : characters) {
                    characterListModel.addElement(character);
                }
            }
        }
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean characterSelected = characterJList.getSelectedValue() != null;
        editButton.setEnabled(characterSelected);
        deleteButton.setEnabled(characterSelected);
    }

    private void addCharacter() {
        Project currentProject = viewModel.getSelectedProject();
        if (currentProject == null) {
            JOptionPane.showMessageDialog(this, "Please select a project first.", "No Project Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        CharacterEditDialog dialog = new CharacterEditDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Character newCharacter = dialog.getCharacter();
            if (newCharacter != null) { // currentProject already checked
                viewModel.addCharacterToProject(currentProject.getId(), newCharacter);
                loadCharacters(); // Refresh list
            }
        }
    }

    private void editCharacter() {
        Project currentProject = viewModel.getSelectedProject();
        Character selectedCharacter = characterJList.getSelectedValue();
        if (selectedCharacter == null || currentProject == null) {
            // Button should be disabled if no character or project, but double check
            return;
        }

        CharacterEditDialog dialog = new CharacterEditDialog((Frame) SwingUtilities.getWindowAncestor(this), selectedCharacter);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Character updatedCharacter = dialog.getCharacter();
            if (updatedCharacter != null) {
                viewModel.updateCharacterInProject(currentProject.getId(), updatedCharacter);
                loadCharacters(); // Refresh list
            }
        }
    }

    private void deleteCharacter() {
        Project currentProject = viewModel.getSelectedProject();
        Character selectedCharacter = characterJList.getSelectedValue();
        if (selectedCharacter == null || currentProject == null) {
            // Button should be disabled, but double check
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete character: " + selectedCharacter.getName() + "?",
                "Delete Character",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            viewModel.deleteCharacterFromProject(currentProject.getId(), selectedCharacter.getId());
            loadCharacters(); // Refresh list
        }
    }

    // private void closeView(Frame owner) { // Removed method
    // ...
    // }

    // public static void showDialog(Frame owner, ProjectViewModel viewModel, Project project) { // Removed method
    // ...
    // }
}
