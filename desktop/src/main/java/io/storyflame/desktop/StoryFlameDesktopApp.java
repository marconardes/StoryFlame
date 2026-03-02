package io.storyflame.desktop;

import io.storyflame.core.model.Project;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectAutosaveService;
import io.storyflame.core.storage.ProjectStoragePaths;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class StoryFlameDesktopApp {
    private final ProjectArchiveStore store;
    private final ProjectAutosaveService autosaveService;
    private final JTextField titleField;
    private final JTextField authorField;
    private final JTextArea summaryArea;
    private final JLabel statusLabel;
    private Project currentProject;
    private Path currentPath;
    private boolean syncingUi;

    private StoryFlameDesktopApp() {
        this.store = new ProjectArchiveStore(ProjectStoragePaths.defaultDesktopProjectsDirectory());
        this.autosaveService = new ProjectAutosaveService(store, Duration.ofSeconds(2));
        this.titleField = new JTextField();
        this.authorField = new JTextField();
        this.summaryArea = new JTextArea();
        this.statusLabel = new JLabel("Nenhum projeto carregado.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StoryFlameDesktopApp().showWindow());
    }

    private void showWindow() {
        JFrame frame = new JFrame("StoryFlame Desktop");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(860, 540);
        frame.setMinimumSize(new Dimension(720, 480));

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.add(new JLabel("Titulo"));
        form.add(titleField);
        form.add(new JLabel("Autor"));
        form.add(authorField);

        JPanel actions = new JPanel();
        JButton newButton = new JButton("Novo");
        JButton openButton = new JButton("Abrir");
        JButton saveButton = new JButton("Salvar");
        actions.add(newButton);
        actions.add(openButton);
        actions.add(saveButton);

        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setBorder(BorderFactory.createTitledBorder("Resumo local"));

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.add(actions, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        root.add(form, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);

        bindFieldListeners();
        newButton.addActionListener(event -> createProject());
        openButton.addActionListener(event -> openProject(frame));
        saveButton.addActionListener(event -> saveProject());

        frame.setContentPane(root);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                autosaveService.close();
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        createProject();
    }

    private void bindFieldListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onProjectEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onProjectEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onProjectEdited();
            }
        };
        titleField.getDocument().addDocumentListener(listener);
        authorField.getDocument().addDocumentListener(listener);
    }

    private void createProject() {
        currentProject = store.createProject("Novo Projeto", "Autor");
        currentPath = ProjectStoragePaths.suggestedArchivePath(store.getBaseDirectory(), currentProject);
        store.save(currentProject, currentPath);
        syncFieldsFromProject();
        renderSummary();
        statusLabel.setText("Projeto criado em " + currentPath);
    }

    private void openProject(JFrame frame) {
        JFileChooser chooser = new JFileChooser(store.getBaseDirectory().toFile());
        int result = chooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        currentPath = chooser.getSelectedFile().toPath();
        currentProject = store.open(currentPath);
        syncFieldsFromProject();
        renderSummary();
        statusLabel.setText("Projeto aberto de " + currentPath);
    }

    private void saveProject() {
        if (currentProject == null || currentPath == null) {
            return;
        }
        syncProjectFromFields();
        currentPath = saveProjectArchive(currentPath);
        renderSummary();
        statusLabel.setText("Projeto salvo em " + currentPath);
    }

    private void onProjectEdited() {
        if (syncingUi || currentProject == null || currentPath == null) {
            return;
        }
        syncProjectFromFields();
        renderSummary();
        currentPath = resolveSavePath(currentPath);
        Path autosavePath = currentPath;
        autosaveService.schedule(currentProject, autosavePath, () ->
                SwingUtilities.invokeLater(() -> statusLabel.setText("Autosave concluido em " + autosavePath))
        );
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void syncFieldsFromProject() {
        syncingUi = true;
        titleField.setText(currentProject.getTitle());
        authorField.setText(currentProject.getAuthor());
        syncingUi = false;
    }

    private void syncProjectFromFields() {
        currentProject.setTitle(titleField.getText());
        currentProject.setAuthor(authorField.getText());
    }

    private void renderSummary() {
        if (currentProject == null) {
            summaryArea.setText("");
            return;
        }
        summaryArea.setText("""
                Arquivo: %s
                Projeto: %s
                Autor: %s
                Capitulos: %d
                Personagens: %d
                Atualizado em: %s
                """.formatted(
                currentPath,
                currentProject.getTitle(),
                currentProject.getAuthor(),
                currentProject.getChapters().size(),
                currentProject.getCharacters().size(),
                currentProject.getUpdatedAt()
        ));
    }

    private Path saveProjectArchive(Path previousPath) {
        Path targetPath = resolveSavePath(previousPath);
        store.save(currentProject, targetPath);
        deleteSupersededArchive(previousPath, targetPath);
        return targetPath;
    }

    private Path resolveSavePath(Path previousPath) {
        return ProjectStoragePaths.resolveManagedArchivePath(store.getBaseDirectory(), previousPath, currentProject);
    }

    private void deleteSupersededArchive(Path previousPath, Path targetPath) {
        if (previousPath.equals(targetPath)) {
            return;
        }
        try {
            Files.deleteIfExists(previousPath);
        } catch (Exception exception) {
            statusLabel.setText("Projeto salvo em " + targetPath + " (arquivo antigo mantido)");
        }
    }
}
