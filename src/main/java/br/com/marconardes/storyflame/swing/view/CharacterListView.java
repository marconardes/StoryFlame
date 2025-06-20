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
    private final Project currentProject;
    private final DefaultListModel<Character> characterListModel;
    private final JList<Character> characterJList;

    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton closeButton; // Or done button

    public CharacterListView(Frame owner, ProjectViewModel viewModel, Project project) {
        this.viewModel = viewModel;
        this.currentProject = project;
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
        closeButton = new JButton("Close");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Some spacing
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addCharacter());
        editButton.addActionListener(e -> editCharacter());
        deleteButton.addActionListener(e -> deleteCharacter());
        closeButton.addActionListener(e -> closeView(owner)); // Pass owner to close dialog

        // Initial state of buttons
        updateButtonStates();
        characterJList.addListSelectionListener(e -> updateButtonStates());
    }

    private void loadCharacters() {
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
        CharacterEditDialog dialog = new CharacterEditDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Character newCharacter = dialog.getCharacter();
            if (newCharacter != null && currentProject != null) {
                viewModel.addCharacterToProject(currentProject.getId(), newCharacter);
                loadCharacters(); // Refresh list
            }
        }
    }

    private void editCharacter() {
        Character selectedCharacter = characterJList.getSelectedValue();
        if (selectedCharacter == null || currentProject == null) {
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
        Character selectedCharacter = characterJList.getSelectedValue();
        if (selectedCharacter == null || currentProject == null) {
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

    private void closeView(Frame owner) {
        // If this panel is inside a JDialog, get the dialog and dispose it
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JDialog) {
            ((JDialog) window).dispose();
        } else if (owner instanceof JDialog) {
            // Fallback if it was directly added to a JDialog passed as owner
             ((JDialog) owner).dispose();
        }
        // If it's in a JFrame, other logic might be needed,
        // but typically a character list view would be in a dialog.
    }

    // Optional: A static method to show this panel in a dialog
    public static void showDialog(Frame owner, ProjectViewModel viewModel, Project project) {
        JDialog dialog = new JDialog(owner, "Manage Characters: " + project.getName(), true); // true for modal
        CharacterListView view = new CharacterListView(owner, viewModel, project); // Pass owner for close logic
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(view);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
