package br.com.marconardes.storyflame.swing.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PasswordEntryDialog extends JDialog {
    private JPasswordField passwordField;
    private String password;

    public PasswordEntryDialog(Frame parent, String title, String message) {
        super(parent, title, true); // Modal
        initComponents(message);
        pack();
        setLocationRelativeTo(parent); // Center dialog
    }

    private void initComponents(String message) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Message Label
        JLabel messageLabel = new JLabel(message);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(messageLabel, gbc);

        // Password Field
        passwordField = new JPasswordField(20);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(passwordField, gbc);

        // OK Button
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                password = new String(passwordField.getPassword());
                dispose();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        add(okButton, gbc);

        // Cancel Button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                password = null;
                dispose();
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(cancelButton, gbc);

        // Make OK button default on Enter press
        getRootPane().setDefaultButton(okButton);
    }

    public String getPassword() {
        // Make the dialog visible only when this method is called,
        // ensuring it blocks until input is received or dialog is closed.
        // This is a common pattern for modal dialogs that return a value.
        setVisible(true);
        return password;
    }
}
