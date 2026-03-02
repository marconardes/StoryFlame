package io.storyflame.desktop;

import io.storyflame.core.model.Project;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Scene;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectAutosaveService;
import io.storyflame.core.storage.ProjectStoragePaths;
import io.storyflame.core.text.WordCount;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public final class StoryFlameDesktopApp {
    private final ProjectArchiveStore store;
    private final ProjectAutosaveService autosaveService;
    private final JTextField titleField;
    private final JTextField authorField;
    private final JTextField chapterTitleField;
    private final JTextField sceneTitleField;
    private final JTextArea sceneEditorArea;
    private final JTextArea summaryArea;
    private final JLabel wordCountLabel;
    private final JLabel statusLabel;
    private final DefaultListModel<String> chapterListModel;
    private final DefaultListModel<String> sceneListModel;
    private final JList<String> chapterList;
    private final JList<String> sceneList;
    private final UndoManager sceneUndoManager;
    private static final String UNDO_ACTION_KEY = "storyflame-undo";
    private static final String REDO_ACTION_KEY = "storyflame-redo";
    private Project currentProject;
    private Path currentPath;
    private Chapter selectedChapter;
    private Scene selectedScene;
    private boolean syncingUi;

    private StoryFlameDesktopApp() {
        this.store = new ProjectArchiveStore(ProjectStoragePaths.defaultDesktopProjectsDirectory());
        this.autosaveService = new ProjectAutosaveService(store, Duration.ofSeconds(2));
        this.titleField = new JTextField();
        this.authorField = new JTextField();
        this.chapterTitleField = new JTextField();
        this.sceneTitleField = new JTextField();
        this.sceneEditorArea = new JTextArea();
        this.summaryArea = new JTextArea();
        this.wordCountLabel = new JLabel("0 palavras");
        this.statusLabel = new JLabel("Nenhum projeto carregado.");
        this.chapterListModel = new DefaultListModel<>();
        this.sceneListModel = new DefaultListModel<>();
        this.chapterList = new JList<>(chapterListModel);
        this.sceneList = new JList<>(sceneListModel);
        this.sceneUndoManager = new UndoManager();
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

        JPanel metadataPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.insets.set(0, 0, 8, 0);
        metadataPanel.add(new JLabel("Titulo"), constraints);
        constraints.gridy++;
        metadataPanel.add(titleField, constraints);
        constraints.gridy++;
        metadataPanel.add(new JLabel("Autor"), constraints);
        constraints.gridy++;
        metadataPanel.add(authorField, constraints);

        JPanel actions = new JPanel();
        JButton newButton = new JButton("Novo");
        JButton openButton = new JButton("Abrir");
        JButton saveButton = new JButton("Salvar");
        actions.add(newButton);
        actions.add(openButton);
        actions.add(saveButton);

        configureEditorComponents();

        JPanel chapterPanel = new JPanel(new BorderLayout(8, 8));
        chapterPanel.setBorder(BorderFactory.createTitledBorder("Capitulos"));
        chapterPanel.add(new JScrollPane(chapterList), BorderLayout.CENTER);
        chapterPanel.add(chapterTitleField, BorderLayout.SOUTH);

        JPanel scenePanel = new JPanel(new BorderLayout(8, 8));
        scenePanel.setBorder(BorderFactory.createTitledBorder("Cenas"));
        scenePanel.add(new JScrollPane(sceneList), BorderLayout.CENTER);
        scenePanel.add(sceneTitleField, BorderLayout.SOUTH);

        JPanel sceneEditorPanel = new JPanel(new BorderLayout(8, 8));
        sceneEditorPanel.setBorder(BorderFactory.createTitledBorder("Editor de cena"));
        sceneEditorPanel.add(new JScrollPane(sceneEditorArea), BorderLayout.CENTER);

        JPanel sceneFooter = new JPanel(new BorderLayout());
        sceneFooter.add(wordCountLabel, BorderLayout.WEST);
        sceneFooter.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        sceneEditorPanel.add(sceneFooter, BorderLayout.SOUTH);

        JSplitPane navigationSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chapterPanel, scenePanel);
        navigationSplit.setResizeWeight(0.5);
        JSplitPane workspaceSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigationSplit, sceneEditorPanel);
        workspaceSplit.setResizeWeight(0.32);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.add(actions, BorderLayout.NORTH);
        centerPanel.add(workspaceSplit, BorderLayout.CENTER);

        root.add(metadataPanel, BorderLayout.NORTH);
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
        chapterTitleField.getDocument().addDocumentListener(listener);
        sceneTitleField.getDocument().addDocumentListener(listener);
        sceneEditorArea.getDocument().addDocumentListener(listener);

        chapterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sceneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chapterList.addListSelectionListener(this::onChapterSelected);
        sceneList.addListSelectionListener(this::onSceneSelected);

        sceneEditorArea.getDocument().addUndoableEditListener(sceneUndoManager);
        installUndoRedo(sceneEditorArea, sceneUndoManager);
    }

    private void createProject() {
        currentProject = store.createProject("Novo Projeto", "Autor");
        ensureEditorStructure();
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
        ensureEditorStructure();
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
        refreshLists();
        selectInitialScene();
        syncingUi = false;
    }

    private void syncProjectFromFields() {
        currentProject.setTitle(titleField.getText());
        currentProject.setAuthor(authorField.getText());
        if (selectedChapter != null) {
            selectedChapter.setTitle(chapterTitleField.getText());
        }
        if (selectedScene != null) {
            selectedScene.setTitle(sceneTitleField.getText());
            selectedScene.setContent(sceneEditorArea.getText());
        }
        refreshListLabels();
        updateWordCount();
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
                Capitulo atual: %s
                Cena atual: %s
                Capitulos: %d
                Cenas no capitulo: %d
                Palavras na cena: %d
                Atualizado em: %s
                """.formatted(
                currentPath,
                currentProject.getTitle(),
                currentProject.getAuthor(),
                selectedChapter == null ? "-" : selectedChapter.getTitle(),
                selectedScene == null ? "-" : selectedScene.getTitle(),
                currentProject.getChapters().size(),
                selectedChapter == null ? 0 : selectedChapter.getScenes().size(),
                selectedScene == null ? 0 : WordCount.count(selectedScene.getContent()),
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

    private void ensureEditorStructure() {
        if (currentProject.getChapters().isEmpty()) {
            currentProject.getChapters().add(new Chapter(null, "Capitulo 1", List.of(new Scene(null, "Cena 1", "", null))));
        }
        Chapter firstChapter = currentProject.getChapters().get(0);
        if (firstChapter.getScenes().isEmpty()) {
            firstChapter.getScenes().add(new Scene(null, "Cena 1", "", null));
        }
    }

    private void refreshLists() {
        chapterListModel.clear();
        for (Chapter chapter : currentProject.getChapters()) {
            chapterListModel.addElement(displayTitle(chapter.getTitle(), "Capitulo"));
        }
        Chapter activeChapter = selectedChapter != null ? selectedChapter : currentProject.getChapters().get(0);
        refreshScenes(activeChapter);
    }

    private void refreshScenes(Chapter chapter) {
        sceneListModel.clear();
        if (chapter == null) {
            return;
        }
        for (Scene scene : chapter.getScenes()) {
            sceneListModel.addElement(displayTitle(scene.getTitle(), "Cena"));
        }
    }

    private void refreshListLabels() {
        int chapterIndex = chapterList.getSelectedIndex();
        int sceneIndex = sceneList.getSelectedIndex();
        refreshLists();
        if (chapterIndex >= 0 && chapterIndex < chapterListModel.size()) {
            chapterList.setSelectedIndex(chapterIndex);
        }
        if (sceneIndex >= 0 && sceneIndex < sceneListModel.size()) {
            sceneList.setSelectedIndex(sceneIndex);
        }
    }

    private void selectInitialScene() {
        if (chapterListModel.isEmpty()) {
            return;
        }
        chapterList.setSelectedIndex(0);
        if (!sceneListModel.isEmpty()) {
            sceneList.setSelectedIndex(0);
        }
        updateEditorFields();
    }

    private void onChapterSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi || currentProject == null) {
            return;
        }
        int selectedIndex = chapterList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentProject.getChapters().size()) {
            return;
        }
        selectedChapter = currentProject.getChapters().get(selectedIndex);
        if (selectedChapter.getScenes().isEmpty()) {
            selectedChapter.getScenes().add(new Scene(null, "Cena 1", "", null));
        }
        refreshScenes(selectedChapter);
        sceneList.setSelectedIndex(0);
        updateEditorFields();
    }

    private void onSceneSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi || selectedChapter == null) {
            return;
        }
        int selectedIndex = sceneList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= selectedChapter.getScenes().size()) {
            return;
        }
        selectedScene = selectedChapter.getScenes().get(selectedIndex);
        updateEditorFields();
    }

    private void updateEditorFields() {
        syncingUi = true;
        chapterTitleField.setText(selectedChapter == null ? "" : selectedChapter.getTitle());
        sceneTitleField.setText(selectedScene == null ? "" : selectedScene.getTitle());
        sceneEditorArea.setText(selectedScene == null ? "" : selectedScene.getContent());
        sceneUndoManager.discardAllEdits();
        updateWordCount();
        renderSummary();
        syncingUi = false;
    }

    private void configureEditorComponents() {
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setRows(5);
        summaryArea.setBorder(BorderFactory.createTitledBorder("Resumo local"));

        chapterTitleField.setBorder(BorderFactory.createTitledBorder("Titulo do capitulo"));
        sceneTitleField.setBorder(BorderFactory.createTitledBorder("Titulo da cena"));

        sceneEditorArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        sceneEditorArea.setLineWrap(false);
        sceneEditorArea.setWrapStyleWord(false);
        sceneEditorArea.setTabSize(4);
        sceneEditorArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private void installUndoRedo(JComponent component, UndoManager undoManager) {
        component.getInputMap().put(KeyStroke.getKeyStroke("control Z"), UNDO_ACTION_KEY);
        component.getActionMap().put(UNDO_ACTION_KEY, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                try {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                } catch (CannotUndoException ignored) {
                }
            }
        });
        component.getInputMap().put(KeyStroke.getKeyStroke("control Y"), REDO_ACTION_KEY);
        component.getActionMap().put(REDO_ACTION_KEY, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                try {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                } catch (CannotRedoException ignored) {
                }
            }
        });
    }

    private void updateWordCount() {
        int wordCount = selectedScene == null ? 0 : WordCount.count(sceneEditorArea.getText());
        wordCountLabel.setText(wordCount + " palavras");
    }

    private String displayTitle(String value, String fallbackPrefix) {
        if (value == null || value.isBlank()) {
            return fallbackPrefix;
        }
        return value;
    }
}
