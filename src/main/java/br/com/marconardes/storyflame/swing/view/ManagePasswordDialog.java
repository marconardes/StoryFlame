package br.com.marconardes.storyflame.swing.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class ManagePasswordDialog extends JDialog {

    public enum Action {
        SET, CHANGE, REMOVE
    }

    private Action action;
    private boolean isPasswordCurrentlySet;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmNewPasswordField;

    private JLabel currentPasswordLabel;
    private JLabel newPasswordLabel;
    private JLabel confirmNewPasswordLabel;
    private JLabel errorLabel;

    private String currentPassword;
    private String newPassword;

    public ManagePasswordDialog(Frame parent, Action action, boolean isPasswordCurrentlySet) {
        super(parent, true); // Modal
        this.action = action;
        this.isPasswordCurrentlySet = isPasswordCurrentlySet;
        setTitle(getDialogTitle());
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private String getDialogTitle() {
        switch (action) {
            case SET:
                return "Set Project Password";
            case CHANGE:
                return "Change Project Password";
            case REMOVE:
                return "Remove Project Password";
            default:
                return "Manage Password";
        }
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int gridy = 0;

        // Error Label (initially empty and hidden)
        errorLabel = new JLabel(" "); // Start with a space to reserve height
        errorLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        add(errorLabel, gbc);

        // Current Password (for CHANGE and REMOVE)
        if (action == Action.CHANGE || (action == Action.REMOVE && isPasswordCurrentlySet)) {
            currentPasswordLabel = new JLabel("Current Password:");
            gbc.gridx = 0;
            gbc.gridy = gridy;
            gbc.gridwidth = 1;
            add(currentPasswordLabel, gbc);

            currentPasswordField = new JPasswordField(20);
            gbc.gridx = 1;
            gbc.gridy = gridy++;
            add(currentPasswordField, gbc);
        } else if (action == Action.REMOVE && !isPasswordCurrentlySet) {
            // If removing but no password is set, show a message.
            // This case should ideally be handled by the calling logic (e.g., disable "Remove Password" button)
            // but as a safeguard in the dialog:
            JLabel noPasswordLabel = new JLabel("No password is currently set for this project.");
            gbc.gridx = 0;
            gbc.gridy = gridy++;
            gbc.gridwidth = 2;
            add(noPasswordLabel, gbc);
        }


        // New Password (for SET and CHANGE)
        if (action == Action.SET || action == Action.CHANGE) {
            newPasswordLabel = new JLabel("New Password:");
            gbc.gridx = 0;
            gbc.gridy = gridy;
            add(newPasswordLabel, gbc);

            newPasswordField = new JPasswordField(20);
            gbc.gridx = 1;
            gbc.gridy = gridy++;
            add(newPasswordField, gbc);

            confirmNewPasswordLabel = new JLabel("Confirm New Password:");
            gbc.gridx = 0;
            gbc.gridy = gridy;
            add(confirmNewPasswordLabel, gbc);

            confirmNewPasswordField = new JPasswordField(20);
            gbc.gridx = 1;
            gbc.gridy = gridy++;
            add(confirmNewPasswordField, gbc);
        }

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        getRootPane().setDefaultButton(okButton);
    }

    private void onOK() {
        errorLabel.setText(" "); // Clear previous errors

        if (action == Action.CHANGE || (action == Action.REMOVE && isPasswordCurrentlySet)) {
            if (currentPasswordField.getPassword().length == 0) {
                errorLabel.setText("Current password cannot be empty.");
                return;
            }
            currentPassword = new String(currentPasswordField.getPassword());
        }

        if (action == Action.SET || action == Action.CHANGE) {
            char[] newPass = newPasswordField.getPassword();
            char[] confirmPass = confirmNewPasswordField.getPassword();

            if (newPass.length == 0) {
                errorLabel.setText("New password cannot be empty.");
                return;
            }
            if (!Arrays.equals(newPass, confirmPass)) {
                errorLabel.setText("New passwords do not match.");
                return;
            }
            newPassword = new String(newPass);
            // Clear arrays for security
            Arrays.fill(newPass, '0');
            Arrays.fill(confirmPass, '0');
        }

        // If action is REMOVE and no password was set, currentPassword will be null.
        // If action is REMOVE and password was set, currentPassword will be populated.
        // If action is SET, newPassword will be populated.
        // If action is CHANGE, both currentPassword and newPassword will be populated.

        dispose();
    }

    private void onCancel() {
        currentPassword = null;
        newPassword = null;
        dispose();
    }

    // Call setVisible(true) on an instance of this dialog to show it.
    // Then retrieve values using these getters.

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    // To make it behave like PasswordEntryDialog (show on getter call)
    // public void display() {
    // setVisible(true);
    // }
}
