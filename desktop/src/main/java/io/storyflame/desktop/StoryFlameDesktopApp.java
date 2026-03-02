package io.storyflame.desktop;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.search.ProjectSearch;
import io.storyflame.core.search.SearchMatch;
import io.storyflame.core.search.SearchTarget;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectAutosaveService;
import io.storyflame.core.storage.ProjectStoragePaths;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import io.storyflame.core.tags.NarrativeTagCatalog;
import io.storyflame.core.tags.NarrativeTagParser;
import io.storyflame.core.tags.ParsedNarrativeTag;
import io.storyflame.core.tags.TagLibraryIssue;
import io.storyflame.core.tags.TagLibraryValidator;
import io.storyflame.core.tags.TemplateExpansionEngine;
import io.storyflame.core.tags.TemplateExpansionMode;
import io.storyflame.core.tags.TemplateExpansionResult;
import io.storyflame.core.text.WordCount;
import io.storyflame.core.validation.NarrativeIntegrityIssue;
import io.storyflame.core.validation.NarrativeIntegrityValidator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public final class StoryFlameDesktopApp {
    private static final String UNDO_ACTION_KEY = "storyflame-undo";
    private static final String REDO_ACTION_KEY = "storyflame-redo";

    private final ProjectArchiveStore store;
    private final ProjectAutosaveService autosaveService;
    private final JTextField titleField;
    private final JTextField authorField;
    private final JTextField chapterTitleField;
    private final JTextField sceneTitleField;
    private final JTextField searchField;
    private final JTextField tagSearchField;
    private final JTextField characterSearchField;
    private final JTextField characterNameField;
    private final JTextField povSearchField;
    private final JTextField tagIdField;
    private final JTextField tagLabelField;
    private final JTextField tagTemplateField;
    private final JTextField profilePrefixField;
    private final JTextField profilePreferredTagsField;
    private final JTextArea sceneEditorArea;
    private final JTextArea summaryArea;
    private final JTextArea characterDescriptionArea;
    private final JTextArea tagDescriptionArea;
    private final JLabel contextLabel;
    private final JLabel wordCountLabel;
    private final JLabel chapterCountLabel;
    private final JLabel sceneCountLabel;
    private final JLabel characterCountLabel;
    private final JLabel searchCountLabel;
    private final JLabel tagCountLabel;
    private final JLabel renderModeLabel;
    private final JLabel projectPathLabel;
    private final JLabel pointOfViewLabel;
    private final JLabel integrityLabel;
    private final JLabel tagLibraryIssuesLabel;
    private final JLabel selectedTagUsageLabel;
    private final JLabel selectedTagStatusLabel;
    private final JLabel selectedProfileCharacterLabel;
    private final JLabel selectedProfileStatusLabel;
    private final JLabel selectedCharacterScenesLabel;
    private final JLabel selectedCharacterPointOfViewLabel;
    private final JLabel statusLabel;
    private final DefaultListModel<String> chapterListModel;
    private final DefaultListModel<String> sceneListModel;
    private final DefaultListModel<String> searchListModel;
    private final DefaultListModel<String> tagListModel;
    private final DefaultListModel<String> characterListModel;
    private final DefaultListModel<String> povListModel;
    private final DefaultListModel<String> profileListModel;
    private final JList<String> chapterList;
    private final JList<String> sceneList;
    private final JList<String> searchList;
    private final JList<String> tagList;
    private final JList<String> characterList;
    private final JList<String> povList;
    private final JList<String> profileList;
    private final UndoManager sceneUndoManager;
    private final List<SearchMatch> searchMatches;
    private final List<NarrativeTag> visibleTags;
    private final List<Character> visibleCharacters;
    private final List<Character> visiblePointOfViewCharacters;
    private final List<CharacterTagProfile> visibleProfiles;
    private final Timer searchRefreshTimer;
    private TemplateExpansionMode templateExpansionMode;

    private JFrame frame;
    private JTabbedPane tabbedPane;
    private Project currentProject;
    private Path currentPath;
    private Chapter selectedChapter;
    private Scene selectedScene;
    private Character selectedCharacter;
    private NarrativeTag selectedTag;
    private CharacterTagProfile selectedProfile;
    private boolean syncingUi;

    private StoryFlameDesktopApp() {
        this.store = new ProjectArchiveStore(ProjectStoragePaths.defaultDesktopProjectsDirectory());
        this.autosaveService = new ProjectAutosaveService(store, Duration.ofSeconds(2));
        this.titleField = new JTextField();
        this.authorField = new JTextField();
        this.chapterTitleField = new JTextField();
        this.sceneTitleField = new JTextField();
        this.searchField = new JTextField();
        this.tagSearchField = new JTextField();
        this.characterSearchField = new JTextField();
        this.characterNameField = new JTextField();
        this.povSearchField = new JTextField();
        this.tagIdField = new JTextField();
        this.tagLabelField = new JTextField();
        this.tagTemplateField = new JTextField();
        this.profilePrefixField = new JTextField();
        this.profilePreferredTagsField = new JTextField();
        this.sceneEditorArea = new JTextArea();
        this.summaryArea = new JTextArea();
        this.characterDescriptionArea = new JTextArea();
        this.tagDescriptionArea = new JTextArea();
        this.contextLabel = new JLabel("Nenhuma cena selecionada");
        this.wordCountLabel = new JLabel("0 palavras");
        this.chapterCountLabel = new JLabel("0 capitulos");
        this.sceneCountLabel = new JLabel("0 cenas");
        this.characterCountLabel = new JLabel("0 personagens");
        this.searchCountLabel = new JLabel("0 resultados");
        this.tagCountLabel = new JLabel("0 tags");
        this.renderModeLabel = new JLabel("Rascunho");
        this.projectPathLabel = new JLabel("Sem arquivo");
        this.pointOfViewLabel = new JLabel("POV: sem personagem");
        this.integrityLabel = new JLabel("0 referencias quebradas");
        this.tagLibraryIssuesLabel = new JLabel("0 inconsistencias de tags");
        this.selectedTagUsageLabel = new JLabel("0 usos no manuscrito");
        this.selectedTagStatusLabel = new JLabel("Tag valida");
        this.selectedProfileCharacterLabel = new JLabel("Nenhum perfil selecionado");
        this.selectedProfileStatusLabel = new JLabel("Sem inconsistencias");
        this.selectedCharacterScenesLabel = new JLabel("0 cenas ligadas");
        this.selectedCharacterPointOfViewLabel = new JLabel("Nao e o POV atual");
        this.statusLabel = new JLabel("Nenhum projeto carregado.");
        this.chapterListModel = new DefaultListModel<>();
        this.sceneListModel = new DefaultListModel<>();
        this.searchListModel = new DefaultListModel<>();
        this.tagListModel = new DefaultListModel<>();
        this.characterListModel = new DefaultListModel<>();
        this.povListModel = new DefaultListModel<>();
        this.profileListModel = new DefaultListModel<>();
        this.chapterList = new JList<>(chapterListModel);
        this.sceneList = new JList<>(sceneListModel);
        this.searchList = new JList<>(searchListModel);
        this.tagList = new JList<>(tagListModel);
        this.characterList = new JList<>(characterListModel);
        this.povList = new JList<>(povListModel);
        this.profileList = new JList<>(profileListModel);
        this.sceneUndoManager = new UndoManager();
        this.searchMatches = new ArrayList<>();
        this.visibleTags = new ArrayList<>();
        this.visibleCharacters = new ArrayList<>();
        this.visiblePointOfViewCharacters = new ArrayList<>();
        this.visibleProfiles = new ArrayList<>();
        this.searchRefreshTimer = new Timer(250, event -> refreshSearchResultsNow());
        this.searchRefreshTimer.setRepeats(false);
        this.templateExpansionMode = TemplateExpansionMode.DRAFT;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StoryFlameDesktopApp().showWindow());
    }

    private void showWindow() {
        applyDesktopLookAndFeel();
        frame = new JFrame("StoryFlame Desktop");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1280, 820);
        frame.setMinimumSize(new Dimension(1024, 700));
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setJMenuBar(buildMenuBar());
        frame.setContentPane(buildRootPanel());
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                autosaveService.close();
            }
        });

        configureEditorComponents();
        createTabs();
        bindFieldListeners();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        createProject();
    }

    private JPanel buildRootPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(236, 232, 224));

        JPanel projectSidebar = buildProjectPanel();
        projectSidebar.setPreferredSize(new Dimension(300, 10));

        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectSidebar, tabbedPane);
        contentSplitPane.setBorder(BorderFactory.createEmptyBorder());
        contentSplitPane.setResizeWeight(0.0);
        contentSplitPane.setDividerLocation(300);
        contentSplitPane.setOneTouchExpandable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildToolBar(), BorderLayout.NORTH);
        root.add(contentSplitPane, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);
        return root;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Arquivo");
        fileMenu.add(menuItem("Novo projeto", "control N", this::createProject));
        fileMenu.add(menuItem("Abrir...", "control O", () -> openProject(frame)));
        fileMenu.add(menuItem("Salvar", "control S", this::saveProject));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Fechar", "control W", frame::dispose));

        JMenu editMenu = new JMenu("Editar");
        editMenu.add(menuItem("Desfazer", "control Z", this::undoSceneEdit));
        editMenu.add(menuItem("Refazer", "control Y", this::redoSceneEdit));
        editMenu.addSeparator();
        editMenu.add(menuItem("Modo rascunho", "control 1", () -> setTemplateExpansionMode(TemplateExpansionMode.DRAFT)));
        editMenu.add(menuItem("Modo render", "control 2", () -> setTemplateExpansionMode(TemplateExpansionMode.RENDER)));

        JMenu windowMenu = new JMenu("Janelas");
        windowMenu.add(menuItem("Editor", "F1", this::focusEditorFrame));
        windowMenu.add(menuItem("Estrutura", "F2", this::focusStructureFrame));
        windowMenu.add(menuItem("Projeto", "F3", this::focusProjectFrame));
        windowMenu.add(menuItem("Busca", "F4", this::focusSearchFrame));
        windowMenu.add(menuItem("Personagens", "F5", this::focusCharacterFrame));
        windowMenu.add(menuItem("Tags", "F6", this::focusTagsFrame));

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(windowMenu);
        return menuBar;
    }

    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(201, 192, 178)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        toolBar.setBackground(new Color(246, 241, 232));

        JButton newButton = new JButton("Novo");
        JButton openButton = new JButton("Abrir");
        JButton saveButton = new JButton("Salvar");
        JButton structureButton = new JButton("Estrutura");
        JButton projectButton = new JButton("Projeto");
        JButton searchButton = new JButton("Buscar");
        JButton characterButton = new JButton("Personagens");
        JButton tagsButton = new JButton("Tags");

        newButton.addActionListener(event -> createProject());
        openButton.addActionListener(event -> openProject(frame));
        saveButton.addActionListener(event -> saveProject());
        structureButton.addActionListener(event -> focusStructureFrame());
        projectButton.addActionListener(event -> focusProjectFrame());
        searchButton.addActionListener(event -> focusSearchFrame());
        characterButton.addActionListener(event -> focusCharacterFrame());
        tagsButton.addActionListener(event -> focusTagsFrame());

        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(structureButton);
        toolBar.add(projectButton);
        toolBar.add(searchButton);
        toolBar.add(characterButton);
        toolBar.add(tagsButton);
        toolBar.addSeparator();
        toolBar.add(chapterCountLabel);
        toolBar.add(Box.createHorizontalStrut(12));
        toolBar.add(sceneCountLabel);
        toolBar.add(Box.createHorizontalStrut(12));
        toolBar.add(characterCountLabel);
        toolBar.add(Box.createHorizontalStrut(12));
        toolBar.add(searchCountLabel);
        toolBar.add(Box.createHorizontalStrut(12));
        toolBar.add(tagCountLabel);
        toolBar.add(Box.createHorizontalStrut(12));
        toolBar.add(renderModeLabel);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(contextLabel);
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(wordCountLabel);
        return toolBar;
    }

    private void createTabs() {
        tabbedPane.addTab("Editor", buildEditorPanel());
        tabbedPane.addTab("Estrutura", buildStructurePanel());
        tabbedPane.addTab("Busca", buildSearchPanel());
        tabbedPane.addTab("Personagens", buildCharacterPanel());
        tabbedPane.addTab("Tags", buildTagsPanel());
    }

    private JPanel buildEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setBackground(new Color(244, 239, 231));
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(sceneTitleField, BorderLayout.CENTER);
        header.add(buildEditorBadge(contextLabel), BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(sceneEditorArea), BorderLayout.CENTER);
        panel.add(buildPointOfViewPanel(), BorderLayout.EAST);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        footer.add(buildEditorBadge(projectPathLabel), BorderLayout.CENTER);
        JPanel rightBadges = new JPanel(new GridLayout(1, 0, 8, 0));
        rightBadges.setOpaque(false);
        rightBadges.add(buildEditorBadge(renderModeLabel));
        rightBadges.add(buildEditorBadge(wordCountLabel));
        footer.add(rightBadges, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStructurePanel() {
        JPanel chapterPanel = new JPanel(new BorderLayout(8, 8));
        chapterPanel.setBorder(BorderFactory.createTitledBorder("Capitulos"));
        chapterPanel.add(buildChapterToolbar(), BorderLayout.NORTH);
        chapterPanel.add(new JScrollPane(chapterList), BorderLayout.CENTER);
        chapterPanel.add(chapterTitleField, BorderLayout.SOUTH);

        JPanel scenePanel = new JPanel(new BorderLayout(8, 8));
        scenePanel.setBorder(BorderFactory.createTitledBorder("Cenas"));
        scenePanel.add(buildSceneToolbar(), BorderLayout.NORTH);
        scenePanel.add(new JScrollPane(sceneList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chapterPanel, scenePanel);
        splitPane.setResizeWeight(0.52);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(244, 239, 231));
        root.add(buildStructureSummaryPanel(), BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildProjectPanel() {
        JPanel metaPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        metaPanel.add(titleField);
        metaPanel.add(authorField);

        JPanel actionPanel = new JPanel(new GridLayout(1, 0, 8, 0));
        JButton newButton = new JButton("Novo");
        JButton openButton = new JButton("Abrir");
        JButton saveButton = new JButton("Salvar");
        newButton.addActionListener(event -> createProject());
        openButton.addActionListener(event -> openProject(frame));
        saveButton.addActionListener(event -> saveProject());
        actionPanel.add(newButton);
        actionPanel.add(openButton);
        actionPanel.add(saveButton);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.add(metaPanel);
        fields.add(Box.createVerticalStrut(10));
        fields.add(actionPanel);
        fields.add(Box.createVerticalStrut(10));
        fields.add(buildEditorBadge(projectPathLabel));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(244, 239, 231));
        root.add(fields, BorderLayout.NORTH);
        root.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSearchPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(244, 239, 231));
        root.add(searchField, BorderLayout.NORTH);
        root.add(new JScrollPane(searchList), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        JButton openButton = new JButton("Abrir resultado");
        openButton.addActionListener(event -> navigateToSearchSelection());
        footer.add(buildEditorBadge(searchCountLabel), BorderLayout.CENTER);
        footer.add(openButton, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildCharacterPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(244, 239, 231));
        root.add(characterSearchField, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout(8, 8));
        listPanel.setOpaque(false);
        listPanel.add(buildCharacterToolbar(), BorderLayout.NORTH);
        listPanel.add(new JScrollPane(characterList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, buildCharacterDetailsPanel());
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.5);
        root.add(splitPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        footer.add(buildEditorBadge(characterCountLabel));
        footer.add(buildEditorBadge(integrityLabel));
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildTagsPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(244, 239, 231));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildTagLibraryPanel(), buildTagProfilesPanel());
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.58);
        root.add(splitPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        footer.add(buildEditorBadge(tagCountLabel));
        footer.add(buildEditorBadge(tagLibraryIssuesLabel));
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildTagLibraryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Biblioteca de tags"));
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(tagSearchField, BorderLayout.NORTH);
        header.add(buildTagLibrarySummaryPanel(), BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(buildTagToolbar(), BorderLayout.NORTH);
        center.add(new JScrollPane(tagList), BorderLayout.CENTER);
        center.add(buildTagDetailsPanel(), BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTagProfilesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Perfis por personagem"));
        panel.add(buildTagProfileSummaryPanel(), BorderLayout.NORTH);
        panel.add(new JScrollPane(profileList), BorderLayout.CENTER);
        panel.add(buildProfileDetailsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTagDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes da tag"));

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 8));
        fields.setOpaque(false);
        fields.add(tagIdField);
        fields.add(tagLabelField);
        fields.add(tagTemplateField);
        panel.add(fields, BorderLayout.NORTH);
        panel.add(new JScrollPane(tagDescriptionArea), BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        JButton duplicateButton = new JButton("Duplicar tag");
        JButton clearButton = new JButton("Limpar campos");
        duplicateButton.addActionListener(event -> duplicateTag());
        clearButton.addActionListener(event -> clearSelectedTagDraft());
        footer.add(duplicateButton);
        footer.add(clearButton);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildProfileDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes do perfil"));

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 8));
        fields.setOpaque(false);
        fields.add(profilePrefixField);
        fields.add(profilePreferredTagsField);
        panel.add(fields, BorderLayout.CENTER);

        JButton applyCurrentTagButton = new JButton("Usar tag selecionada");
        applyCurrentTagButton.addActionListener(event -> appendSelectedTagToProfile());
        panel.add(applyCurrentTagButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTagLibrarySummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 8, 0));
        panel.setOpaque(false);
        panel.add(buildEditorBadge(selectedTagUsageLabel));
        panel.add(buildEditorBadge(selectedTagStatusLabel));
        return panel;
    }

    private JPanel buildTagProfileSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 8, 0));
        panel.setOpaque(false);
        panel.add(buildEditorBadge(selectedProfileCharacterLabel));
        panel.add(buildEditorBadge(selectedProfileStatusLabel));
        return panel;
    }

    private JPanel buildCharacterDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Ficha do personagem"));

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(characterNameField, BorderLayout.NORTH);

        JPanel badges = new JPanel(new GridLayout(1, 0, 8, 0));
        badges.setOpaque(false);
        badges.add(buildEditorBadge(selectedCharacterScenesLabel));
        badges.add(buildEditorBadge(selectedCharacterPointOfViewLabel));
        header.add(badges, BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);

        panel.add(new JScrollPane(characterDescriptionArea), BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        JButton assignButton = new JButton("Usar como POV da cena");
        JButton clearSearchButton = new JButton("Limpar busca");
        assignButton.addActionListener(event -> assignSelectedCharacterAsPointOfView());
        clearSearchButton.addActionListener(event -> characterSearchField.setText(""));
        footer.add(assignButton);
        footer.add(clearSearchButton);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPointOfViewPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setOpaque(false);
        root.setPreferredSize(new Dimension(250, 10));
        root.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 8));
        header.setOpaque(false);
        header.add(buildEditorBadge(pointOfViewLabel));
        header.add(povSearchField);
        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(povList), BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 0, 8, 0));
        footer.setOpaque(false);
        JButton applyButton = new JButton("Usar POV");
        JButton clearButton = new JButton("Limpar POV");
        applyButton.addActionListener(event -> applySelectedPointOfView());
        clearButton.addActionListener(event -> clearPointOfView());
        footer.add(applyButton);
        footer.add(clearButton);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildChapterToolbar() {
        JPanel panel = new JPanel();
        JButton addButton = new JButton("+");
        JButton deleteButton = new JButton("-");
        JButton upButton = new JButton("Subir");
        JButton downButton = new JButton("Descer");
        JButton focusButton = new JButton("Ir");
        addButton.addActionListener(event -> addChapter());
        deleteButton.addActionListener(event -> deleteChapter());
        upButton.addActionListener(event -> moveChapter(-1));
        downButton.addActionListener(event -> moveChapter(1));
        focusButton.addActionListener(event -> focusEditorFrame());
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(upButton);
        panel.add(downButton);
        panel.add(focusButton);
        return panel;
    }

    private JPanel buildSceneToolbar() {
        JPanel panel = new JPanel();
        JButton addButton = new JButton("+");
        JButton deleteButton = new JButton("-");
        JButton upButton = new JButton("Subir");
        JButton downButton = new JButton("Descer");
        JButton focusButton = new JButton("Abrir");
        addButton.addActionListener(event -> addScene());
        deleteButton.addActionListener(event -> deleteScene());
        upButton.addActionListener(event -> moveScene(-1));
        downButton.addActionListener(event -> moveScene(1));
        focusButton.addActionListener(event -> focusEditorFrame());
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(upButton);
        panel.add(downButton);
        panel.add(focusButton);
        return panel;
    }

    private JPanel buildCharacterToolbar() {
        JPanel panel = new JPanel();
        JButton addButton = new JButton("+");
        JButton deleteButton = new JButton("-");
        JButton focusButton = new JButton("POV");
        JButton assignButton = new JButton("Usar");
        addButton.addActionListener(event -> addCharacter());
        deleteButton.addActionListener(event -> deleteCharacter());
        focusButton.addActionListener(event -> focusEditorFrame());
        assignButton.addActionListener(event -> assignSelectedCharacterAsPointOfView());
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(assignButton);
        panel.add(focusButton);
        return panel;
    }

    private JPanel buildTagToolbar() {
        JPanel panel = new JPanel();
        JButton addButton = new JButton("+");
        JButton deleteButton = new JButton("-");
        JButton clearButton = new JButton("Limpar busca");
        addButton.addActionListener(event -> addTag());
        deleteButton.addActionListener(event -> deleteTag());
        clearButton.addActionListener(event -> tagSearchField.setText(""));
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        return panel;
    }

    private JPanel buildStructureSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 8, 0));
        panel.setOpaque(false);
        panel.add(buildEditorBadge(chapterCountLabel));
        panel.add(buildEditorBadge(sceneCountLabel));
        panel.add(buildEditorBadge(characterCountLabel));
        panel.add(buildEditorBadge(contextLabel));
        return panel;
    }

    private JPanel buildEditorBadge(JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 205, 191)),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
        ));
        panel.setBackground(new Color(251, 248, 242));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setForeground(new Color(92, 79, 62));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JMenuItem menuItem(String label, String keyStroke, Runnable action) {
        JMenuItem item = new JMenuItem(label);
        if (keyStroke != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyStroke));
        }
        item.addActionListener(event -> action.run());
        return item;
    }

    private void bindFieldListeners() {
        DocumentListener metadataListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onMetadataEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onMetadataEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onMetadataEdited();
            }
        };
        DocumentListener sceneTitleListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onSceneTitleEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onSceneTitleEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onSceneTitleEdited();
            }
        };
        DocumentListener chapterTitleListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onChapterTitleEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onChapterTitleEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onChapterTitleEdited();
            }
        };
        DocumentListener sceneContentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onSceneContentEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onSceneContentEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onSceneContentEdited();
            }
        };
        DocumentListener characterListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onCharacterEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onCharacterEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onCharacterEdited();
            }
        };
        DocumentListener tagListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onTagEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onTagEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onTagEdited();
            }
        };
        DocumentListener profileListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onProfileEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onProfileEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onProfileEdited();
            }
        };

        titleField.getDocument().addDocumentListener(metadataListener);
        authorField.getDocument().addDocumentListener(metadataListener);
        chapterTitleField.getDocument().addDocumentListener(chapterTitleListener);
        sceneTitleField.getDocument().addDocumentListener(sceneTitleListener);
        sceneEditorArea.getDocument().addDocumentListener(sceneContentListener);
        characterNameField.getDocument().addDocumentListener(characterListener);
        characterDescriptionArea.getDocument().addDocumentListener(characterListener);
        tagIdField.getDocument().addDocumentListener(tagListener);
        tagLabelField.getDocument().addDocumentListener(tagListener);
        tagTemplateField.getDocument().addDocumentListener(tagListener);
        tagDescriptionArea.getDocument().addDocumentListener(tagListener);
        profilePrefixField.getDocument().addDocumentListener(profileListener);
        profilePreferredTagsField.getDocument().addDocumentListener(profileListener);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                scheduleSearchRefresh();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                scheduleSearchRefresh();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                scheduleSearchRefresh();
            }
        });
        tagSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshTagLibrary();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshTagLibrary();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshTagLibrary();
            }
        });
        characterSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshCharacterLists();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshCharacterLists();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshCharacterLists();
            }
        });
        povSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshPointOfViewList();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshPointOfViewList();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshPointOfViewList();
            }
        });

        chapterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sceneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        characterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        povList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        chapterList.addListSelectionListener(this::onChapterSelected);
        sceneList.addListSelectionListener(this::onSceneSelected);
        searchList.addListSelectionListener(this::onSearchSelected);
        tagList.addListSelectionListener(this::onTagSelected);
        characterList.addListSelectionListener(this::onCharacterSelected);
        profileList.addListSelectionListener(this::onProfileSelected);
        searchList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    navigateToSearchSelection();
                }
            }
        });
        povList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    applySelectedPointOfView();
                }
            }
        });
        sceneEditorArea.getDocument().addUndoableEditListener(sceneUndoManager);
        installUndoRedo(sceneEditorArea, sceneUndoManager);
        installWindowShortcut(frame.getRootPane(), "F1", this::focusEditorFrame);
        installWindowShortcut(frame.getRootPane(), "F2", this::focusStructureFrame);
        installWindowShortcut(frame.getRootPane(), "F3", this::focusProjectFrame);
        installWindowShortcut(frame.getRootPane(), "F4", this::focusSearchFrame);
        installWindowShortcut(frame.getRootPane(), "F5", this::focusCharacterFrame);
        installWindowShortcut(frame.getRootPane(), "F6", this::focusTagsFrame);
        installWindowShortcut(frame.getRootPane(), "control alt P", this::assignSelectedCharacterAsPointOfView);
    }

    private void createProject() {
        currentProject = store.createProject("Novo Projeto", "Autor");
        ensureEditorStructure();
        currentPath = ProjectStoragePaths.suggestedArchivePath(store.getBaseDirectory(), currentProject);
        store.save(currentProject, currentPath);
        selectedChapter = currentProject.getChapters().get(0);
        selectedScene = selectedChapter.getScenes().get(0);
        selectedCharacter = currentProject.getCharacters().isEmpty() ? null : currentProject.getCharacters().get(0);
        syncFieldsFromProject();
        statusLabel.setText("Projeto criado em " + currentPath);
        focusEditorFrame();
    }

    private void openProject(JFrame owner) {
        JFileChooser chooser = new JFileChooser(store.getBaseDirectory().toFile());
        int result = chooser.showOpenDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        currentPath = chooser.getSelectedFile().toPath();
        currentProject = store.open(currentPath);
        ensureEditorStructure();
        selectedChapter = currentProject.getChapters().get(0);
        selectedScene = selectedChapter.getScenes().get(0);
        selectedCharacter = currentProject.getCharacters().isEmpty() ? null : currentProject.getCharacters().get(0);
        syncFieldsFromProject();
        statusLabel.setText("Projeto aberto de " + currentPath);
        focusEditorFrame();
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
        scheduleSearchRefresh();
        currentPath = resolveSavePath(currentPath);
        Path autosavePath = currentPath;
        autosaveService.schedule(currentProject, autosavePath, () ->
                SwingUtilities.invokeLater(() -> statusLabel.setText("Autosave concluido em " + autosavePath))
        );
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void onMetadataEdited() {
        onProjectEdited();
    }

    private void onSceneTitleEdited() {
        if (syncingUi || currentProject == null || currentPath == null) {
            return;
        }
        if (selectedScene != null) {
            selectedScene.setTitle(sceneTitleField.getText());
            int sceneIndex = selectedChapter == null ? -1 : selectedChapter.getScenes().indexOf(selectedScene);
            if (sceneIndex >= 0 && sceneIndex < sceneListModel.size()) {
                sceneListModel.set(sceneIndex, (sceneIndex + 1) + ". " + displayTitle(selectedScene.getTitle(), "Cena"));
            }
        }
        contextLabel.setText((selectedChapter == null ? "-" : displayTitle(selectedChapter.getTitle(), "Capitulo"))
                + " / "
                + (selectedScene == null ? "-" : displayTitle(selectedScene.getTitle(), "Cena")));
        sceneCountLabel.setText(selectedChapter == null ? "0 cenas" : selectedChapter.getScenes().size() + " cenas");
        renderSummary();
        scheduleSearchRefresh();
        scheduleAutosave();
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void onChapterTitleEdited() {
        if (syncingUi || currentProject == null || currentPath == null) {
            return;
        }
        if (selectedChapter != null) {
            selectedChapter.setTitle(chapterTitleField.getText());
            int chapterIndex = currentProject.getChapters().indexOf(selectedChapter);
            if (chapterIndex >= 0 && chapterIndex < chapterListModel.size()) {
                chapterListModel.set(chapterIndex, (chapterIndex + 1) + ". " + displayTitle(selectedChapter.getTitle(), "Capitulo"));
            }
        }
        contextLabel.setText((selectedChapter == null ? "-" : displayTitle(selectedChapter.getTitle(), "Capitulo"))
                + " / "
                + (selectedScene == null ? "-" : displayTitle(selectedScene.getTitle(), "Cena")));
        chapterCountLabel.setText(currentProject.getChapters().size() + " capitulos");
        sceneCountLabel.setText(selectedChapter == null ? "0 cenas" : selectedChapter.getScenes().size() + " cenas");
        renderSummary();
        scheduleSearchRefresh();
        scheduleAutosave();
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void onSceneContentEdited() {
        if (syncingUi || currentProject == null || currentPath == null) {
            return;
        }
        if (selectedScene != null) {
            selectedScene.setContent(sceneEditorArea.getText());
        }
        updateWordCount();
        updateTagCountLabel();
        renderSummary();
        scheduleSearchRefresh();
        scheduleAutosave();
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void onCharacterEdited() {
        if (syncingUi || currentProject == null || currentPath == null || selectedCharacter == null) {
            return;
        }
        selectedCharacter.setName(characterNameField.getText());
        selectedCharacter.setDescription(characterDescriptionArea.getText());
        refreshCharacterLists();
        refreshPointOfViewList();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void onTagEdited() {
        if (syncingUi || currentProject == null || currentPath == null || selectedTag == null) {
            return;
        }
        NarrativeTag updatedTag = new NarrativeTag(
                nonBlankOrFallback(tagIdField.getText(), selectedTag.id()),
                tagLabelField.getText(),
                tagDescriptionArea.getText(),
                tagTemplateField.getText()
        );
        int index = currentProject.getNarrativeTags().indexOf(selectedTag);
        if (index >= 0) {
            currentProject.getNarrativeTags().set(index, updatedTag);
            selectedTag = updatedTag;
        }
        refreshTagLibrary();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void onProfileEdited() {
        if (syncingUi || currentProject == null || currentPath == null || selectedProfile == null) {
            return;
        }
        selectedProfile.setPrefix(profilePrefixField.getText().trim());
        selectedProfile.getPreferredTagIds().clear();
        for (String value : profilePreferredTagsField.getText().split(",")) {
            String trimmed = value.trim().toLowerCase();
            if (!trimmed.isBlank()) {
                selectedProfile.getPreferredTagIds().add(trimmed);
            }
        }
        refreshTagProfiles();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void syncFieldsFromProject() {
        syncingUi = true;
        titleField.setText(currentProject.getTitle());
        authorField.setText(currentProject.getAuthor());
        ensureCharacterTagProfiles();
        refreshStructureLists();
        refreshCharacterLists();
        refreshTagLibrary();
        refreshTagProfiles();
        updateEditorFields();
        refreshSearchResultsNow();
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
        if (selectedCharacter != null) {
            selectedCharacter.setName(characterNameField.getText());
            selectedCharacter.setDescription(characterDescriptionArea.getText());
        }
        updateWordCount();
    }

    private void addChapter() {
        ensureSelectionState();
        if (currentProject == null) {
            return;
        }
        syncProjectFromFields();
        Chapter chapter = new Chapter(null, "Capitulo " + (currentProject.getChapters().size() + 1), List.of(new Scene(null, "Cena 1", "", null)));
        currentProject.getChapters().add(chapter);
        selectedChapter = chapter;
        selectedScene = chapter.getScenes().get(0);
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void deleteChapter() {
        ensureSelectionState();
        if (currentProject == null || selectedChapter == null || currentProject.getChapters().size() <= 1) {
            return;
        }
        syncProjectFromFields();
        int removedIndex = currentProject.getChapters().indexOf(selectedChapter);
        currentProject.getChapters().remove(selectedChapter);
        int nextIndex = Math.max(0, removedIndex - 1);
        selectedChapter = currentProject.getChapters().get(nextIndex);
        ensureChapterHasScene(selectedChapter);
        selectedScene = selectedChapter.getScenes().get(0);
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void moveChapter(int offset) {
        ensureSelectionState();
        if (currentProject == null || selectedChapter == null) {
            return;
        }
        syncProjectFromFields();
        List<Chapter> chapters = currentProject.getChapters();
        int currentIndex = chapters.indexOf(selectedChapter);
        int nextIndex = currentIndex + offset;
        if (currentIndex < 0 || nextIndex < 0 || nextIndex >= chapters.size()) {
            return;
        }
        chapters.remove(currentIndex);
        chapters.add(nextIndex, selectedChapter);
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void addScene() {
        ensureSelectionState();
        if (selectedChapter == null) {
            return;
        }
        syncProjectFromFields();
        Scene scene = new Scene(null, "Cena " + (selectedChapter.getScenes().size() + 1), "", null);
        selectedChapter.getScenes().add(scene);
        selectedScene = scene;
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void addCharacter() {
        if (currentProject == null) {
            return;
        }
        syncProjectFromFields();
        String draftName = preferredNewCharacterName();
        Character character = new Character(
                null,
                draftName.isBlank() ? "Personagem " + (currentProject.getCharacters().size() + 1) : draftName,
                ""
        );
        currentProject.getCharacters().add(character);
        selectedCharacter = character;
        ensureCharacterTagProfiles();
        if (!characterSearchField.getText().isBlank()) {
            characterSearchField.setText("");
        }
        refreshCharacterLists();
        refreshTagProfiles();
        refreshPointOfViewList();
        onProjectEdited();
    }

    private void deleteCharacter() {
        if (currentProject == null || selectedCharacter == null) {
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(
                frame,
                "Excluir personagem '" + displayCharacterName(selectedCharacter) + "'?",
                "Excluir personagem",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        syncProjectFromFields();
        int removedIndex = currentProject.getCharacters().indexOf(selectedCharacter);
        if (removedIndex < 0) {
            return;
        }
        currentProject.getCharacters().remove(removedIndex);
        selectedCharacter = currentProject.getCharacters().isEmpty()
                ? null
                : currentProject.getCharacters().get(Math.max(0, removedIndex - 1));
        currentProject.getCharacterTagProfiles().removeIf(profile -> currentProject.getCharacters().stream().noneMatch(character -> character.getId().equals(profile.getCharacterId())));
        refreshCharacterLists();
        refreshTagProfiles();
        refreshPointOfViewList();
        onProjectEdited();
    }

    private void addTag() {
        if (currentProject == null) {
            return;
        }
        String baseId = sanitizeTagId(tagSearchField.getText().isBlank() ? tagIdField.getText() : tagSearchField.getText());
        if (baseId.isBlank()) {
            baseId = "tag" + (currentProject.getNarrativeTags().size() + 1);
        }
        String uniqueId = ensureUniqueTagId(baseId);
        NarrativeTag tag = new NarrativeTag(uniqueId, uniqueId, "", "");
        currentProject.getNarrativeTags().add(tag);
        selectedTag = tag;
        tagSearchField.setText("");
        refreshTagLibrary();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("Nova tag criada.");
    }

    private void deleteTag() {
        if (currentProject == null || selectedTag == null) {
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(
                frame,
                "Excluir tag '" + selectedTag.id() + "'?",
                "Excluir tag",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        currentProject.getNarrativeTags().remove(selectedTag);
        for (CharacterTagProfile profile : currentProject.getCharacterTagProfiles()) {
            profile.getPreferredTagIds().removeIf(tagId -> tagId.equals(selectedTag.id()));
        }
        selectedTag = currentProject.getNarrativeTags().isEmpty() ? null : currentProject.getNarrativeTags().get(0);
        refreshTagLibrary();
        refreshTagProfiles();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("Tag removida.");
    }

    private void duplicateTag() {
        if (currentProject == null || selectedTag == null) {
            return;
        }
        String newId = ensureUniqueTagId(selectedTag.id() + "-copy");
        NarrativeTag copy = new NarrativeTag(newId, selectedTag.label(), selectedTag.description(), selectedTag.template());
        currentProject.getNarrativeTags().add(copy);
        selectedTag = copy;
        refreshTagLibrary();
        scheduleAutosave();
        statusLabel.setText("Tag duplicada.");
    }

    private void clearSelectedTagDraft() {
        syncingUi = true;
        tagIdField.setText("");
        tagLabelField.setText("");
        tagTemplateField.setText("");
        tagDescriptionArea.setText("");
        syncingUi = false;
    }

    private void appendSelectedTagToProfile() {
        if (selectedProfile == null || selectedTag == null) {
            return;
        }
        if (!selectedProfile.getPreferredTagIds().contains(selectedTag.id())) {
            selectedProfile.getPreferredTagIds().add(selectedTag.id());
        }
        refreshTagProfiles();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("Tag adicionada ao perfil.");
    }

    private void assignSelectedCharacterAsPointOfView() {
        if (selectedScene == null || selectedCharacter == null) {
            return;
        }
        selectedScene.setPointOfViewCharacterId(selectedCharacter.getId());
        refreshPointOfViewList();
        updateIntegrityLabel();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("POV definido a partir do personagem selecionado.");
        focusEditorFrame();
    }

    private void deleteScene() {
        ensureSelectionState();
        if (selectedChapter == null || selectedScene == null || selectedChapter.getScenes().size() <= 1) {
            return;
        }
        syncProjectFromFields();
        int removedIndex = selectedChapter.getScenes().indexOf(selectedScene);
        selectedChapter.getScenes().remove(selectedScene);
        int nextIndex = Math.max(0, removedIndex - 1);
        selectedScene = selectedChapter.getScenes().get(nextIndex);
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void moveScene(int offset) {
        ensureSelectionState();
        if (selectedChapter == null || selectedScene == null) {
            return;
        }
        syncProjectFromFields();
        List<Scene> scenes = selectedChapter.getScenes();
        int currentIndex = scenes.indexOf(selectedScene);
        int nextIndex = currentIndex + offset;
        if (currentIndex < 0 || nextIndex < 0 || nextIndex >= scenes.size()) {
            return;
        }
        scenes.remove(currentIndex);
        scenes.add(nextIndex, selectedScene);
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void refreshStructureLists() {
        boolean previousSyncingUi = syncingUi;
        syncingUi = true;
        int selectedChapterIndex = currentProject == null || selectedChapter == null
                ? -1
                : currentProject.getChapters().indexOf(selectedChapter);
        int selectedSceneIndex = selectedChapter == null || selectedScene == null
                ? -1
                : selectedChapter.getScenes().indexOf(selectedScene);

        chapterListModel.clear();
        sceneListModel.clear();
        if (currentProject == null) {
            chapterTitleField.setText("");
            chapterCountLabel.setText("0 capitulos");
            sceneCountLabel.setText("0 cenas");
            syncingUi = previousSyncingUi;
            return;
        }
        chapterCountLabel.setText(currentProject.getChapters().size() + " capitulos");
        for (int index = 0; index < currentProject.getChapters().size(); index++) {
            Chapter chapter = currentProject.getChapters().get(index);
            chapterListModel.addElement((index + 1) + ". " + displayTitle(chapter.getTitle(), "Capitulo"));
        }
        if (selectedChapter != null) {
            chapterTitleField.setText(selectedChapter.getTitle());
            sceneCountLabel.setText(selectedChapter.getScenes().size() + " cenas");
            for (int index = 0; index < selectedChapter.getScenes().size(); index++) {
                Scene scene = selectedChapter.getScenes().get(index);
                sceneListModel.addElement((index + 1) + ". " + displayTitle(scene.getTitle(), "Cena"));
            }
        } else {
            chapterTitleField.setText("");
            sceneCountLabel.setText("0 cenas");
        }

        if (selectedChapterIndex >= 0 && selectedChapterIndex < chapterListModel.size()) {
            chapterList.setSelectedIndex(selectedChapterIndex);
        }
        if (selectedSceneIndex >= 0 && selectedSceneIndex < sceneListModel.size()) {
            sceneList.setSelectedIndex(selectedSceneIndex);
        }
        syncingUi = previousSyncingUi;
    }

    private void refreshSearchResultsNow() {
        searchMatches.clear();
        searchListModel.clear();
        if (currentProject == null) {
            renderSummary();
            return;
        }
        searchMatches.addAll(ProjectSearch.search(currentProject, searchField.getText()));
        for (SearchMatch match : searchMatches) {
            searchListModel.addElement(formatSearchLabel(match));
        }
        searchCountLabel.setText(searchMatches.size() + " resultados");
        renderSummary();
    }

    private void refreshCharacterLists() {
        boolean previousSyncingUi = syncingUi;
        syncingUi = true;
        Character previousSelection = selectedCharacter;

        visibleCharacters.clear();
        characterListModel.clear();
        if (currentProject == null) {
            characterNameField.setText("");
            characterDescriptionArea.setText("");
            characterCountLabel.setText("0 personagens");
            selectedCharacterScenesLabel.setText("0 cenas ligadas");
            selectedCharacterPointOfViewLabel.setText("Nao e o POV atual");
            updateIntegrityLabel();
            syncingUi = previousSyncingUi;
            return;
        }

        String query = characterSearchField.getText() == null ? "" : characterSearchField.getText().trim().toLowerCase();
        for (Character character : currentProject.getCharacters()) {
            String haystack = (character.getName() + "\n" + character.getDescription()).toLowerCase();
            if (!query.isBlank() && !haystack.contains(query)) {
                continue;
            }
            visibleCharacters.add(character);
            characterListModel.addElement(displayCharacterName(character));
        }

        characterCountLabel.setText(currentProject.getCharacters().size() + " personagens");
        if (previousSelection != null && visibleCharacters.contains(previousSelection)) {
            characterList.setSelectedIndex(visibleCharacters.indexOf(previousSelection));
        } else if (!visibleCharacters.isEmpty()) {
            characterList.setSelectedIndex(0);
            selectedCharacter = visibleCharacters.get(0);
        } else if (!currentProject.getCharacters().contains(selectedCharacter)) {
            selectedCharacter = currentProject.getCharacters().isEmpty() ? null : currentProject.getCharacters().get(0);
        }

        characterNameField.setText(selectedCharacter == null ? "" : selectedCharacter.getName());
        characterDescriptionArea.setText(selectedCharacter == null ? "" : selectedCharacter.getDescription());
        selectedCharacterScenesLabel.setText(selectedCharacter == null
                ? "0 cenas ligadas"
                : countScenesForCharacter(selectedCharacter) + " cenas ligadas");
        selectedCharacterPointOfViewLabel.setText(selectedCharacter == null
                ? "Nao e o POV atual"
                : (isSelectedCharacterPointOfView() ? "POV da cena atual" : "Nao e o POV atual"));
        updateIntegrityLabel();
        syncingUi = previousSyncingUi;
    }

    private void refreshTagLibrary() {
        boolean previousSyncingUi = syncingUi;
        syncingUi = true;
        NarrativeTag previousSelection = selectedTag;

        visibleTags.clear();
        tagListModel.clear();
        if (currentProject == null) {
            tagIdField.setText("");
            tagLabelField.setText("");
            tagTemplateField.setText("");
            tagDescriptionArea.setText("");
            tagLibraryIssuesLabel.setText("0 inconsistencias de tags");
            selectedTagUsageLabel.setText("0 usos no manuscrito");
            selectedTagStatusLabel.setText("Tag valida");
            syncingUi = previousSyncingUi;
            return;
        }

        String query = tagSearchField.getText() == null ? "" : tagSearchField.getText().trim().toLowerCase();
        for (NarrativeTag tag : currentProject.getNarrativeTags()) {
            String haystack = (tag.id() + "\n" + tag.label() + "\n" + tag.description() + "\n" + tag.template()).toLowerCase();
            if (!query.isBlank() && !haystack.contains(query)) {
                continue;
            }
            visibleTags.add(tag);
            tagListModel.addElement(tag.id() + " | " + displayTitle(tag.label(), "Sem rotulo"));
        }

        if (previousSelection != null && visibleTags.contains(previousSelection)) {
            tagList.setSelectedIndex(visibleTags.indexOf(previousSelection));
        } else if (!visibleTags.isEmpty()) {
            selectedTag = visibleTags.get(0);
            tagList.setSelectedIndex(0);
        } else if (!currentProject.getNarrativeTags().contains(selectedTag)) {
            selectedTag = currentProject.getNarrativeTags().isEmpty() ? null : currentProject.getNarrativeTags().get(0);
        }

        tagIdField.setText(selectedTag == null ? "" : selectedTag.id());
        tagLabelField.setText(selectedTag == null ? "" : selectedTag.label());
        tagTemplateField.setText(selectedTag == null ? "" : selectedTag.template());
        tagDescriptionArea.setText(selectedTag == null ? "" : selectedTag.description());
        selectedTagUsageLabel.setText(selectedTag == null
                ? "0 usos no manuscrito"
                : countTagUsage(selectedTag.id()) + " usos no manuscrito");
        selectedTagStatusLabel.setText(selectedTag == null
                ? "Tag valida"
                : (selectedTag.template().isBlank() ? "Sem template proprio" : "Template configurado"));
        updateTagLibraryIssuesLabel();
        syncingUi = previousSyncingUi;
    }

    private void refreshTagProfiles() {
        boolean previousSyncingUi = syncingUi;
        syncingUi = true;
        ensureCharacterTagProfiles();

        visibleProfiles.clear();
        profileListModel.clear();
        if (currentProject == null) {
            profilePrefixField.setText("");
            profilePreferredTagsField.setText("");
            selectedProfileCharacterLabel.setText("Nenhum perfil selecionado");
            selectedProfileStatusLabel.setText("Sem inconsistencias");
            syncingUi = previousSyncingUi;
            return;
        }

        for (CharacterTagProfile profile : currentProject.getCharacterTagProfiles()) {
            visibleProfiles.add(profile);
            profileListModel.addElement(formatProfileLabel(profile));
        }

        if (selectedProfile != null && visibleProfiles.contains(selectedProfile)) {
            profileList.setSelectedIndex(visibleProfiles.indexOf(selectedProfile));
        } else if (!visibleProfiles.isEmpty()) {
            selectedProfile = visibleProfiles.get(0);
            profileList.setSelectedIndex(0);
        } else {
            selectedProfile = null;
        }

        profilePrefixField.setText(selectedProfile == null ? "" : selectedProfile.getPrefix());
        profilePreferredTagsField.setText(selectedProfile == null ? "" : String.join(", ", selectedProfile.getPreferredTagIds()));
        selectedProfileCharacterLabel.setText(selectedProfile == null
                ? "Nenhum perfil selecionado"
                : formatProfileLabel(selectedProfile));
        selectedProfileStatusLabel.setText(selectedProfile == null
                ? "Sem inconsistencias"
                : profileHasIssues(selectedProfile) ? "Perfil com inconsistencias" : "Perfil consistente");
        updateTagLibraryIssuesLabel();
        syncingUi = previousSyncingUi;
    }

    private void refreshPointOfViewList() {
        boolean previousSyncingUi = syncingUi;
        syncingUi = true;
        visiblePointOfViewCharacters.clear();
        povListModel.clear();
        if (currentProject == null) {
            pointOfViewLabel.setText("POV: sem personagem");
            syncingUi = previousSyncingUi;
            return;
        }

        String query = povSearchField.getText() == null ? "" : povSearchField.getText().trim().toLowerCase();
        Character selectedPovCharacter = findCharacterById(selectedScene == null ? null : selectedScene.getPointOfViewCharacterId());
        for (Character character : currentProject.getCharacters()) {
            String haystack = (character.getName() + "\n" + character.getDescription()).toLowerCase();
            if (!query.isBlank() && !haystack.contains(query)) {
                continue;
            }
            visiblePointOfViewCharacters.add(character);
            povListModel.addElement(displayCharacterName(character));
        }
        if (selectedPovCharacter != null && visiblePointOfViewCharacters.contains(selectedPovCharacter)) {
            povList.setSelectedIndex(visiblePointOfViewCharacters.indexOf(selectedPovCharacter));
        }
        pointOfViewLabel.setText("POV: " + displayPointOfViewName());
        syncingUi = previousSyncingUi;
    }

    private void scheduleSearchRefresh() {
        if (syncingUi) {
            return;
        }
        searchRefreshTimer.restart();
    }

    private String formatSearchLabel(SearchMatch match) {
        String targetLabel = switch (match.target()) {
            case CHAPTER -> "Capitulo";
            case SCENE_TITLE -> "Cena";
            case SCENE_CONTENT -> "Texto";
        };
        return targetLabel + ": " + displayTitle(match.title(), "-") + " | " + match.excerpt();
    }

    private void navigateToSearchSelection() {
        int selectedIndex = searchList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= searchMatches.size() || currentProject == null) {
            return;
        }
        SearchMatch match = searchMatches.get(selectedIndex);
        selectedChapter = currentProject.getChapters().get(match.chapterIndex());
        ensureChapterHasScene(selectedChapter);
        selectedScene = match.target() == SearchTarget.CHAPTER
                ? selectedChapter.getScenes().get(0)
                : selectedChapter.getScenes().get(match.sceneIndex());
        refreshStructureLists();
        selectCurrentObjects();
        focusEditorFrame();
        statusLabel.setText("Navegacao rapida: " + formatSearchLabel(match));
    }

    private void selectCurrentObjects() {
        syncingUi = true;
        int chapterIndex = currentProject.getChapters().indexOf(selectedChapter);
        chapterList.setSelectedIndex(chapterIndex);
        if (selectedChapter != null) {
            int sceneIndex = selectedChapter.getScenes().indexOf(selectedScene);
            sceneList.setSelectedIndex(sceneIndex);
        }
        updateEditorFields();
        syncingUi = false;
    }

    private void onChapterSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi || currentProject == null) {
            return;
        }
        syncProjectFromFields();
        int selectedIndex = chapterList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentProject.getChapters().size()) {
            return;
        }
        selectedChapter = currentProject.getChapters().get(selectedIndex);
        ensureChapterHasScene(selectedChapter);
        selectedScene = selectedChapter.getScenes().get(0);
        refreshStructureLists();
        sceneList.setSelectedIndex(0);
        updateEditorFields();
    }

    private void onSceneSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi || selectedChapter == null) {
            return;
        }
        syncProjectFromFields();
        int selectedIndex = sceneList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= selectedChapter.getScenes().size()) {
            return;
        }
        selectedScene = selectedChapter.getScenes().get(selectedIndex);
        updateEditorFields();
    }

    private void onCharacterSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi || currentProject == null) {
            return;
        }
        syncProjectFromFields();
        int selectedIndex = characterList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visibleCharacters.size()) {
            return;
        }
        selectedCharacter = visibleCharacters.get(selectedIndex);
        refreshCharacterLists();
    }

    private void onTagSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi || currentProject == null) {
            return;
        }
        int selectedIndex = tagList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visibleTags.size()) {
            return;
        }
        selectedTag = visibleTags.get(selectedIndex);
        refreshTagLibrary();
    }

    private void onProfileSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi || currentProject == null) {
            return;
        }
        int selectedIndex = profileList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visibleProfiles.size()) {
            return;
        }
        selectedProfile = visibleProfiles.get(selectedIndex);
        refreshTagProfiles();
    }

    private void onSearchSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi) {
            return;
        }
        if (searchList.getSelectedIndex() >= 0) {
            statusLabel.setText("Resultado selecionado. Use duplo clique para abrir.");
        }
    }

    private void updateEditorFields() {
        boolean previousSyncingUi = syncingUi;
        syncingUi = true;
        searchRefreshTimer.stop();
        sceneTitleField.setText(selectedScene == null ? "" : selectedScene.getTitle());
        sceneEditorArea.setText(selectedScene == null ? "" : selectedScene.getContent());
        contextLabel.setText((selectedChapter == null ? "-" : displayTitle(selectedChapter.getTitle(), "Capitulo"))
                + " / "
                + (selectedScene == null ? "-" : displayTitle(selectedScene.getTitle(), "Cena")));
        projectPathLabel.setText(currentPath == null ? "Sem arquivo" : currentPath.toString());
        sceneUndoManager.discardAllEdits();
        updateWordCount();
        updateTagCountLabel();
        refreshPointOfViewList();
        renderSummary();
        syncingUi = previousSyncingUi;
    }

    private void renderSummary() {
        if (currentProject == null) {
            summaryArea.setText("");
            return;
        }
        TemplateExpansionResult expansionResult = currentSceneExpansion();
        summaryArea.setText("""
                Arquivo: %s
                Projeto: %s
                Autor: %s
                Capitulo atual: %s
                Cena atual: %s
                POV da cena: %s
                Tags na cena: %s
                Modo de expansao: %s
                Tags expandidas: %s
                Tags invalidas: %s
                Inconsistencias de tags: %d
                Capitulos: %d
                Cenas no capitulo: %d
                Personagens: %d
                Resultados de busca: %d
                Referencias quebradas: %d
                Palavras na cena: %d
                Atualizado em: %s

                Preview:
                %s
                """.formatted(
                currentPath,
                currentProject.getTitle(),
                currentProject.getAuthor(),
                selectedChapter == null ? "-" : selectedChapter.getTitle(),
                selectedScene == null ? "-" : selectedScene.getTitle(),
                displayPointOfViewName(),
                formatCurrentSceneTagSummary(),
                templateExpansionMode == TemplateExpansionMode.DRAFT ? "rascunho" : "render",
                expansionResult.expandedTagIds().isEmpty() ? "-" : String.join(", ", expansionResult.expandedTagIds()),
                expansionResult.invalidTagIds().isEmpty() ? "-" : String.join(", ", expansionResult.invalidTagIds()),
                TagLibraryValidator.validate(currentProject).size(),
                currentProject.getChapters().size(),
                selectedChapter == null ? 0 : selectedChapter.getScenes().size(),
                currentProject.getCharacters().size(),
                searchMatches.size(),
                NarrativeIntegrityValidator.findBrokenPointOfViewReferences(currentProject).size(),
                selectedScene == null ? 0 : WordCount.count(selectedScene.getContent()),
                currentProject.getUpdatedAt(),
                expansionResult.text()
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
        for (Chapter chapter : currentProject.getChapters()) {
            ensureChapterHasScene(chapter);
        }
    }

    private void ensureChapterHasScene(Chapter chapter) {
        if (chapter.getScenes().isEmpty()) {
            chapter.getScenes().add(new Scene(null, "Cena 1", "", null));
        }
    }

    private void ensureSelectionState() {
        if (currentProject == null || currentProject.getChapters().isEmpty()) {
            selectedChapter = null;
            selectedScene = null;
            selectedCharacter = null;
            return;
        }
        if (selectedChapter == null || !currentProject.getChapters().contains(selectedChapter)) {
            selectedChapter = currentProject.getChapters().get(0);
        }
        ensureChapterHasScene(selectedChapter);
        if (selectedScene == null || !selectedChapter.getScenes().contains(selectedScene)) {
            selectedScene = selectedChapter.getScenes().get(0);
        }
        if (selectedCharacter != null && !currentProject.getCharacters().contains(selectedCharacter)) {
            selectedCharacter = null;
        }
    }

    private void configureEditorComponents() {
        titleField.enableInputMethods(true);
        authorField.enableInputMethods(true);
        chapterTitleField.enableInputMethods(true);
        sceneTitleField.enableInputMethods(true);
        searchField.enableInputMethods(true);
        tagSearchField.enableInputMethods(true);
        characterSearchField.enableInputMethods(true);
        characterNameField.enableInputMethods(true);
        povSearchField.enableInputMethods(true);
        tagIdField.enableInputMethods(true);
        tagLabelField.enableInputMethods(true);
        tagTemplateField.enableInputMethods(true);
        profilePrefixField.enableInputMethods(true);
        profilePreferredTagsField.enableInputMethods(true);
        sceneEditorArea.enableInputMethods(true);
        characterDescriptionArea.enableInputMethods(true);
        tagDescriptionArea.enableInputMethods(true);

        titleField.setFont(new Font("Serif", Font.PLAIN, 15));
        authorField.setFont(new Font("Serif", Font.PLAIN, 15));
        titleField.setBorder(BorderFactory.createTitledBorder("Titulo"));
        authorField.setBorder(BorderFactory.createTitledBorder("Autor"));
        chapterTitleField.setBorder(BorderFactory.createTitledBorder("Titulo do capitulo"));
        sceneTitleField.setBorder(BorderFactory.createTitledBorder("Titulo da cena"));
        searchField.setBorder(BorderFactory.createTitledBorder("Buscar"));
        tagSearchField.setBorder(BorderFactory.createTitledBorder("Buscar tag"));
        characterSearchField.setBorder(BorderFactory.createTitledBorder("Buscar personagem"));
        characterNameField.setBorder(BorderFactory.createTitledBorder("Nome do personagem"));
        povSearchField.setBorder(BorderFactory.createTitledBorder("Buscar POV"));
        tagIdField.setBorder(BorderFactory.createTitledBorder("Id da tag"));
        tagLabelField.setBorder(BorderFactory.createTitledBorder("Rotulo"));
        tagTemplateField.setBorder(BorderFactory.createTitledBorder("Template"));
        profilePrefixField.setBorder(BorderFactory.createTitledBorder("Prefixo do personagem"));
        profilePreferredTagsField.setBorder(BorderFactory.createTitledBorder("Tags preferidas (csv)"));

        chapterList.setFont(new Font("Serif", Font.PLAIN, 15));
        sceneList.setFont(new Font("Serif", Font.PLAIN, 15));
        searchList.setFont(new Font("Serif", Font.PLAIN, 14));
        tagList.setFont(new Font("Serif", Font.PLAIN, 14));
        characterList.setFont(new Font("Serif", Font.PLAIN, 15));
        povList.setFont(new Font("Serif", Font.PLAIN, 14));
        profileList.setFont(new Font("Serif", Font.PLAIN, 14));
        chapterList.setFixedCellHeight(28);
        sceneList.setFixedCellHeight(28);
        searchList.setFixedCellHeight(32);
        tagList.setFixedCellHeight(28);
        characterList.setFixedCellHeight(28);
        povList.setFixedCellHeight(28);
        profileList.setFixedCellHeight(28);

        sceneTitleField.setFont(new Font("Serif", Font.BOLD, 22));
        sceneEditorArea.setFont(new Font("Serif", Font.PLAIN, 22));
        sceneEditorArea.setBackground(new Color(253, 250, 244));
        sceneEditorArea.setForeground(new Color(48, 42, 34));
        sceneEditorArea.setCaretColor(new Color(48, 42, 34));
        sceneEditorArea.setLineWrap(true);
        sceneEditorArea.setWrapStyleWord(true);
        sceneEditorArea.setTabSize(4);
        sceneEditorArea.setMargin(new java.awt.Insets(24, 32, 24, 32));
        sceneEditorArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 216, 201)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setRows(10);
        summaryArea.setFont(new Font("Serif", Font.PLAIN, 14));
        summaryArea.setBackground(new Color(251, 248, 242));
        summaryArea.setBorder(BorderFactory.createTitledBorder("Resumo"));

        characterDescriptionArea.setLineWrap(true);
        characterDescriptionArea.setWrapStyleWord(true);
        characterDescriptionArea.setRows(6);
        characterDescriptionArea.setFont(new Font("Serif", Font.PLAIN, 14));
        characterDescriptionArea.setBackground(new Color(251, 248, 242));
        characterDescriptionArea.setBorder(BorderFactory.createTitledBorder("Descricao"));

        tagDescriptionArea.setLineWrap(true);
        tagDescriptionArea.setWrapStyleWord(true);
        tagDescriptionArea.setRows(5);
        tagDescriptionArea.setFont(new Font("Serif", Font.PLAIN, 14));
        tagDescriptionArea.setBackground(new Color(251, 248, 242));
        tagDescriptionArea.setBorder(BorderFactory.createTitledBorder("Descricao da tag"));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(246, 241, 232));
        statusLabel.setForeground(new Color(84, 72, 58));
    }

    private void installUndoRedo(JComponent component, UndoManager undoManager) {
        component.getInputMap().put(KeyStroke.getKeyStroke("control Z"), UNDO_ACTION_KEY);
        component.getActionMap().put(UNDO_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                undoSceneEdit();
            }
        });

        component.getInputMap().put(KeyStroke.getKeyStroke("control Y"), REDO_ACTION_KEY);
        component.getActionMap().put(REDO_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                redoSceneEdit();
            }
        });
    }

    private void installWindowShortcut(JComponent component, String keyStroke, Runnable action) {
        String actionKey = "shortcut-" + keyStroke;
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke), actionKey);
        component.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                action.run();
            }
        });
    }

    private void undoSceneEdit() {
        try {
            if (sceneUndoManager.canUndo()) {
                sceneUndoManager.undo();
            }
        } catch (CannotUndoException ignored) {
        }
    }

    private void redoSceneEdit() {
        try {
            if (sceneUndoManager.canRedo()) {
                sceneUndoManager.redo();
            }
        } catch (CannotRedoException ignored) {
        }
    }

    private void updateWordCount() {
        int wordCount = selectedScene == null ? 0 : WordCount.count(sceneEditorArea.getText());
        wordCountLabel.setText(wordCount + " palavras");
    }

    private void updateTagCountLabel() {
        List<ParsedNarrativeTag> parsedTags = currentSceneTags();
        long invalidCount = parsedTags.stream().filter(tag -> !tag.valid()).count();
        if (parsedTags.isEmpty()) {
            tagCountLabel.setText("0 tags");
            return;
        }
        tagCountLabel.setText(parsedTags.size() + " tags"
                + (invalidCount > 0 ? " | " + invalidCount + " invalidas" : " | todas validas"));
    }

    private void applySelectedPointOfView() {
        if (selectedScene == null) {
            return;
        }
        int selectedIndex = povList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visiblePointOfViewCharacters.size()) {
            return;
        }
        selectedScene.setPointOfViewCharacterId(visiblePointOfViewCharacters.get(selectedIndex).getId());
        refreshPointOfViewList();
        updateIntegrityLabel();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("POV atualizado.");
    }

    private void clearPointOfView() {
        if (selectedScene == null) {
            return;
        }
        selectedScene.setPointOfViewCharacterId(null);
        refreshPointOfViewList();
        updateIntegrityLabel();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("POV removido.");
    }

    private void updateIntegrityLabel() {
        List<NarrativeIntegrityIssue> issues = NarrativeIntegrityValidator.findBrokenPointOfViewReferences(currentProject);
        integrityLabel.setText(issues.size() + " referencias quebradas");
    }

    private String displayCharacterName(Character character) {
        if (character == null) {
            return "Personagem";
        }
        int sceneCount = countScenesForCharacter(character);
        String suffix = sceneCount == 1 ? "1 cena" : sceneCount + " cenas";
        String povMarker = selectedScene != null && character.getId().equals(selectedScene.getPointOfViewCharacterId()) ? " | POV atual" : "";
        return displayTitle(character.getName(), "Personagem") + " | " + suffix + povMarker;
    }

    private String preferredNewCharacterName() {
        String searchDraft = characterSearchField.getText();
        if (searchDraft != null && !searchDraft.isBlank()) {
            return searchDraft.trim();
        }
        String nameDraft = characterNameField.getText();
        if (nameDraft != null && !nameDraft.isBlank()) {
            return nameDraft.trim();
        }
        return "";
    }

    private String displayPointOfViewName() {
        Character povCharacter = findCharacterById(selectedScene == null ? null : selectedScene.getPointOfViewCharacterId());
        if (povCharacter != null) {
            return displayCharacterName(povCharacter);
        }
        if (selectedScene != null && selectedScene.getPointOfViewCharacterId() != null && !selectedScene.getPointOfViewCharacterId().isBlank()) {
            return "referencia quebrada (" + selectedScene.getPointOfViewCharacterId() + ")";
        }
        return "sem personagem";
    }

    private Character findCharacterById(String characterId) {
        if (currentProject == null || characterId == null || characterId.isBlank()) {
            return null;
        }
        for (Character character : currentProject.getCharacters()) {
            if (characterId.equals(character.getId())) {
                return character;
            }
        }
        return null;
    }

    private int countScenesForCharacter(Character character) {
        if (currentProject == null || character == null) {
            return 0;
        }
        int count = 0;
        for (Chapter chapter : currentProject.getChapters()) {
            for (Scene scene : chapter.getScenes()) {
                if (character.getId().equals(scene.getPointOfViewCharacterId())) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isSelectedCharacterPointOfView() {
        return selectedCharacter != null
                && selectedScene != null
                && selectedCharacter.getId().equals(selectedScene.getPointOfViewCharacterId());
    }

    private List<ParsedNarrativeTag> currentSceneTags() {
        if (selectedScene == null) {
            return List.of();
        }
        return NarrativeTagParser.parse(selectedScene.getContent(), currentNarrativeTagCatalog());
    }

    private TemplateExpansionResult currentSceneExpansion() {
        if (selectedScene == null) {
            return new TemplateExpansionResult("", List.of(), List.of());
        }
        return TemplateExpansionEngine.expand(selectedScene.getContent(), currentNarrativeTagCatalog(), templateExpansionMode);
    }

    private String formatCurrentSceneTagSummary() {
        List<ParsedNarrativeTag> parsedTags = currentSceneTags();
        if (parsedTags.isEmpty()) {
            return "nenhuma";
        }
        List<String> validTagIds = parsedTags.stream()
                .filter(ParsedNarrativeTag::valid)
                .map(ParsedNarrativeTag::tagId)
                .distinct()
                .toList();
        List<String> invalidTagIds = parsedTags.stream()
                .filter(tag -> !tag.valid())
                .map(ParsedNarrativeTag::tagId)
                .distinct()
                .toList();
        if (invalidTagIds.isEmpty()) {
            return String.join(", ", validTagIds);
        }
        if (validTagIds.isEmpty()) {
            return "invalidas: " + String.join(", ", invalidTagIds);
        }
        return "validas: " + String.join(", ", validTagIds) + " | invalidas: " + String.join(", ", invalidTagIds);
    }

    private void setTemplateExpansionMode(TemplateExpansionMode mode) {
        templateExpansionMode = mode;
        renderModeLabel.setText(mode == TemplateExpansionMode.DRAFT ? "Rascunho" : "Render");
        renderSummary();
        statusLabel.setText(mode == TemplateExpansionMode.DRAFT
                ? "Modo rascunho ativo."
                : "Modo render ativo.");
    }

    private NarrativeTagCatalog currentNarrativeTagCatalog() {
        if (currentProject == null || currentProject.getNarrativeTags().isEmpty()) {
            return NarrativeTagCatalog.defaultCatalog();
        }
        return new NarrativeTagCatalog(currentProject.getNarrativeTags());
    }

    private void updateTagLibraryIssuesLabel() {
        List<TagLibraryIssue> issues = TagLibraryValidator.validate(currentProject);
        tagLibraryIssuesLabel.setText(issues.size() + " inconsistencias de tags");
    }

    private void ensureCharacterTagProfiles() {
        if (currentProject == null) {
            return;
        }
        for (Character character : currentProject.getCharacters()) {
            boolean exists = currentProject.getCharacterTagProfiles().stream()
                    .anyMatch(profile -> profile.getCharacterId().equals(character.getId()));
            if (!exists) {
                currentProject.getCharacterTagProfiles().add(new CharacterTagProfile(character.getId(), "", List.of()));
            }
        }
        currentProject.getCharacterTagProfiles().removeIf(profile ->
                currentProject.getCharacters().stream().noneMatch(character -> character.getId().equals(profile.getCharacterId())));
    }

    private String formatProfileLabel(CharacterTagProfile profile) {
        Character character = findCharacterById(profile.getCharacterId());
        String name = character == null ? profile.getCharacterId() : displayCharacterName(character);
        String prefix = profile.getPrefix() == null || profile.getPrefix().isBlank() ? "sem prefixo" : profile.getPrefix();
        return name + " | " + prefix;
    }

    private String sanitizeTagId(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase().replaceAll("[^a-z0-9_-]+", "-");
    }

    private String ensureUniqueTagId(String baseId) {
        String candidate = baseId;
        int suffix = 2;
        while (hasTagId(candidate)) {
            candidate = baseId + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean hasTagId(String candidate) {
        for (NarrativeTag tag : currentProject.getNarrativeTags()) {
            if (tag.id().equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    private int countTagUsage(String tagId) {
        if (currentProject == null || tagId == null || tagId.isBlank()) {
            return 0;
        }
        int count = 0;
        for (Chapter chapter : currentProject.getChapters()) {
            for (Scene scene : chapter.getScenes()) {
                for (ParsedNarrativeTag parsedTag : NarrativeTagParser.parse(scene.getContent(), currentNarrativeTagCatalog())) {
                    if (parsedTag.tagId().equals(tagId)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private boolean profileHasIssues(CharacterTagProfile profile) {
        if (profile == null || currentProject == null) {
            return false;
        }
        for (TagLibraryIssue issue : TagLibraryValidator.validate(currentProject)) {
            if (issue.message().contains(profile.getCharacterId()) || issue.message().contains(profile.getPrefix())) {
                return true;
            }
        }
        return false;
    }

    private String nonBlankOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private void scheduleAutosave() {
        currentPath = resolveSavePath(currentPath);
        Path autosavePath = currentPath;
        autosaveService.schedule(currentProject, autosavePath, () ->
                SwingUtilities.invokeLater(() -> statusLabel.setText("Autosave concluido em " + autosavePath))
        );
    }

    private String displayTitle(String value, String fallbackPrefix) {
        if (value == null || value.isBlank()) {
            return fallbackPrefix;
        }
        return value;
    }

    private void focusEditorFrame() {
        selectTab("Editor");
        sceneEditorArea.requestFocusInWindow();
    }

    private void focusStructureFrame() {
        selectTab("Estrutura");
        chapterList.requestFocusInWindow();
    }

    private void focusProjectFrame() {
        titleField.requestFocusInWindow();
    }

    private void focusSearchFrame() {
        selectTab("Busca");
        searchField.requestFocusInWindow();
    }

    private void focusCharacterFrame() {
        selectTab("Personagens");
        characterList.requestFocusInWindow();
    }

    private void focusTagsFrame() {
        selectTab("Tags");
        tagList.requestFocusInWindow();
    }

    private void selectTab(String title) {
        if (tabbedPane == null) {
            return;
        }
        int index = tabbedPane.indexOfTab(title);
        if (index >= 0) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    private void applyDesktopLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
