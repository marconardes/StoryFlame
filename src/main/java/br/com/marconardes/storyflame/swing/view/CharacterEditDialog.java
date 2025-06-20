package br.com.marconardes.storyflame.swing.view;

import br.com.marconardes.storyflame.swing.model.Character;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CharacterEditDialog extends JDialog {

    private Character character;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField nicknameField;
    private JTextArea descriptionArea;
    private JTextArea historyArea;
    private JTextArea traitsArea;
    private JTextArea relationshipsArea;
    private JTextArea notesArea;

    private JButton saveButton;
    private JButton cancelButton;

    public CharacterEditDialog(Frame owner, Character characterToEdit) {
        super(owner, (characterToEdit == null ? "Add New Character" : "Edit Character"), true); // Modal
        this.character = (characterToEdit == null) ? new Character("") : characterToEdit;

        initComponents();
        populateFields();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(400, 500)); // Ensure dialog is not too small
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Name*:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        nameField = new JTextField(30);
        fieldsPanel.add(nameField, gbc);
        gbc.weightx = 0.0;

        // Nickname
        gbc.gridx = 0; gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Nickname:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        nicknameField = new JTextField(30);
        fieldsPanel.add(nicknameField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
        fieldsPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(5, 30);
        fieldsPanel.add(new JScrollPane(descriptionArea), gbc);
        gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;


        // History
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST;
        fieldsPanel.add(new JLabel("History:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        historyArea = new JTextArea(5, 30);
        fieldsPanel.add(new JScrollPane(historyArea), gbc);
        gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        // Traits
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHWEST;
        fieldsPanel.add(new JLabel("Traits:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        traitsArea = new JTextArea(5, 30);
        fieldsPanel.add(new JScrollPane(traitsArea), gbc);
        gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        // Relationships
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.NORTHWEST;
        fieldsPanel.add(new JLabel("Relationships:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        relationshipsArea = new JTextArea(5, 30);
        fieldsPanel.add(new JScrollPane(relationshipsArea), gbc);
        gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        // Notes
        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.NORTHWEST;
        fieldsPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        notesArea = new JTextArea(5, 30);
        fieldsPanel.add(new JScrollPane(notesArea), gbc);

        // Make JTextAreas wrap lines
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        traitsArea.setLineWrap(true);
        traitsArea.setWrapStyleWord(true);
        relationshipsArea.setLineWrap(true);
        relationshipsArea.setWrapStyleWord(true);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        saveButton.addActionListener(this::onSave);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateFields() {
        if (character != null) {
            nameField.setText(character.getName());
            nicknameField.setText(character.getNickname());
            descriptionArea.setText(character.getDescription());
            historyArea.setText(character.getHistory());
            traitsArea.setText(character.getTraits());
            relationshipsArea.setText(character.getRelationships());
            notesArea.setText(character.getNotes());
        }
    }

    private void onSave(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is a required field.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // If it's a new character, its ID is already set by the Character constructor.
        // If it's an existing character, we keep its original ID.
        character.setName(name);
        character.setNickname(nicknameField.getText().trim());
        character.setDescription(descriptionArea.getText().trim());
        character.setHistory(historyArea.getText().trim());
        character.setTraits(traitsArea.getText().trim());
        character.setRelationships(relationshipsArea.getText().trim());
        character.setNotes(notesArea.getText().trim());

        saved = true;
        dispose();
    }

    public Character getCharacter() {
        return character;
    }

    public boolean isSaved() {
        return saved;
    }
}
