package io.storyflame.desktop;

import io.storyflame.app.project.ProjectApplicationService;
import io.storyflame.app.project.ProjectCharacterApplicationService;
import io.storyflame.app.project.ProjectEditorApplicationService;
import io.storyflame.app.project.ProjectStructureApplicationService;
import io.storyflame.app.project.ProjectTagApplicationService;
import io.storyflame.core.analysis.EmotionAnalysisService;
import io.storyflame.core.character.CharacterDirectory;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.publication.PublicationExportService;
import io.storyflame.core.publication.PublicationFormat;
import io.storyflame.core.search.ProjectSearch;
import io.storyflame.core.search.SearchMatch;
import io.storyflame.core.search.SearchTarget;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectAutosaveService;
import io.storyflame.core.storage.ProjectBackupService;
import io.storyflame.core.storage.ProjectStoragePaths;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.CharacterTagProfileSynchronizer;
import io.storyflame.core.tags.NarrativeTag;
import io.storyflame.core.tags.NarrativeTagIdPolicy;
import io.storyflame.core.tags.ParsedNarrativeTag;
import io.storyflame.core.tags.TagLibraryIssue;
import io.storyflame.core.tags.TagLibraryValidator;
import io.storyflame.core.tags.TemplateExpansionMode;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public final class StoryFlameDesktopApp {
    private static final String UNDO_ACTION_KEY = "storyflame-undo";
    private static final String REDO_ACTION_KEY = "storyflame-redo";

    private final ProjectArchiveStore store;
    private final ProjectAutosaveService autosaveService;
    private final ProjectBackupService backupService;
    private final ProjectApplicationService projectApplicationService;
    private final ProjectCharacterApplicationService projectCharacterApplicationService;
    private final ProjectEditorApplicationService projectEditorApplicationService;
    private final ProjectStructureApplicationService projectStructureApplicationService;
    private final ProjectTagApplicationService projectTagApplicationService;
    private final PublicationExportService publicationExportService;
    private final EmotionAnalysisService emotionAnalysisService;
    private final JTextField titleField;
    private final JTextField authorField;
    private final JTextField chapterTitleField;
    private final JTextField sceneTitleField;
    private final JTextArea sceneSynopsisArea;
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
    private final JLabel favoriteTagCountLabel;
    private final JLabel recentTagCountLabel;
    private final JLabel renderModeLabel;
    private final JLabel projectPathLabel;
    private final JLabel pointOfViewLabel;
    private final JLabel hoverTagPreviewLabel;
    private final JLabel integrityLabel;
    private final JLabel sceneContextSynopsisLabel;
    private final JLabel sceneContextCharactersLabel;
    private final JLabel sceneContextTagsLabel;
    private final JLabel sceneContextIntegrityLabel;
    private final JLabel tagLibraryIssuesLabel;
    private final JLabel selectedTagUsageLabel;
    private final JLabel selectedTagStatusLabel;
    private final JLabel tagDetailModeLabel;
    private final JLabel selectedProfileCharacterLabel;
    private final JLabel selectedProfileStatusLabel;
    private final JLabel selectedCharacterScenesLabel;
    private final JLabel selectedCharacterPointOfViewLabel;
    private final JLabel selectedCharacterTagsLabel;
    private final JLabel characterDetailModeLabel;
    private final JLabel characterDraftHintLabel;
    private final JLabel tagDraftHintLabel;
    private final JLabel statusLabel;
    private final DefaultListModel<String> chapterListModel;
    private final DefaultListModel<String> sceneListModel;
    private final DefaultListModel<String> searchListModel;
    private final DefaultListModel<String> tagListModel;
    private final DefaultListModel<String> tagSuggestionListModel;
    private final DefaultListModel<String> characterListModel;
    private final DefaultListModel<String> povListModel;
    private final DefaultListModel<String> profileListModel;
    private final JList<String> chapterList;
    private final JList<String> sceneList;
    private final JList<String> searchList;
    private final JList<String> tagList;
    private final JList<String> tagSuggestionList;
    private final JList<String> characterList;
    private final JList<String> povList;
    private final JList<String> profileList;
    private final JTree editorStructureTree;
    private final UndoManager sceneUndoManager;
    private final List<SearchMatch> searchMatches;
    private final List<NarrativeTag> visibleTags;
    private final List<NarrativeTag> visibleTagSuggestions;
    private final List<Character> visibleCharacters;
    private final List<Character> visiblePointOfViewCharacters;
    private final List<CharacterTagProfile> visibleProfiles;
    private final Set<String> favoriteTagIds;
    private final List<String> recentTagIds;
    private final Timer searchRefreshTimer;
    private final JPopupMenu tagSuggestionPopup;
    private final JToggleButton readingModeToggle;
    private TemplateExpansionMode templateExpansionMode;

    private JFrame frame;
    private JTabbedPane tabbedPane;
    private Project currentProject;
    private Path currentPath;
    private Chapter selectedChapter;
    private Scene selectedScene;
    private Character selectedCharacter;
    private Character selectedCharacterBeforeDraft;
    private NarrativeTag selectedTag;
    private NarrativeTag selectedTagBeforeDraft;
    private CharacterTagProfile selectedProfile;
    private boolean characterDraftMode;
    private boolean tagDraftMode;
    private boolean syncingUi;
    private DesktopBackgroundCoordinator backgroundCoordinator;
    private DesktopProjectWorkflow projectWorkflow;
    private final DesktopEditorStructureCoordinator editorStructureCoordinator;
    private final DesktopCharacterCoordinator characterCoordinator;
    private final DesktopCharacterWorkflow characterWorkflow;
    private final DesktopTagCoordinator tagCoordinator;
    private final DesktopTagWorkflow tagWorkflow;
    private final DesktopEditorTagAutocomplete tagAutocompleteController;
    private final DesktopAnalysisPanel analysisPanel;

    private StoryFlameDesktopApp() {
        this(
                new ProjectArchiveStore(ProjectStoragePaths.defaultDesktopProjectsDirectory()),
                null,
                null,
                new PublicationExportService(),
                new EmotionAnalysisService()
        );
    }

    StoryFlameDesktopApp(
            ProjectArchiveStore store,
            ProjectAutosaveService autosaveService,
            ProjectBackupService backupService,
            PublicationExportService publicationExportService,
            EmotionAnalysisService emotionAnalysisService
    ) {
        this.store = store;
        this.autosaveService = autosaveService != null
                ? autosaveService
                : new ProjectAutosaveService(store, Duration.ofSeconds(2));
        this.backupService = backupService != null
                ? backupService
                : new ProjectBackupService(
                ProjectStoragePaths.defaultDesktopBackupsDirectory(),
                8,
                Duration.ofMinutes(5)
        );
        this.projectApplicationService = new ProjectApplicationService(this.store, this.backupService);
        this.projectCharacterApplicationService = new ProjectCharacterApplicationService();
        this.projectEditorApplicationService = new ProjectEditorApplicationService();
        this.projectStructureApplicationService = new ProjectStructureApplicationService();
        this.projectTagApplicationService = new ProjectTagApplicationService();
        this.publicationExportService = publicationExportService;
        this.emotionAnalysisService = emotionAnalysisService;
        this.titleField = new JTextField();
        this.authorField = new JTextField();
        this.chapterTitleField = new JTextField();
        this.sceneTitleField = new JTextField();
        this.sceneSynopsisArea = new JTextArea();
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
        this.favoriteTagCountLabel = new JLabel(DesktopWritingProductivityFormatter.favoriteCountLabel(0));
        this.recentTagCountLabel = new JLabel(DesktopWritingProductivityFormatter.recentCountLabel(0));
        this.renderModeLabel = new JLabel("Rascunho");
        this.projectPathLabel = new JLabel("Sem arquivo");
        this.pointOfViewLabel = new JLabel("POV: sem personagem");
        this.hoverTagPreviewLabel = new JLabel(DesktopWritingProductivityFormatter.defaultTagHint());
        this.integrityLabel = new JLabel("0 referencias quebradas");
        this.sceneContextSynopsisLabel = new JLabel(DesktopSceneContextFormatter.synopsisText(null));
        this.sceneContextCharactersLabel = new JLabel(DesktopSceneContextFormatter.charactersText(List.of()));
        this.sceneContextTagsLabel = new JLabel(DesktopSceneContextFormatter.tagsText(null));
        this.sceneContextIntegrityLabel = new JLabel(DesktopSceneContextFormatter.integrityText("0 referencias quebradas no POV"));
        this.tagLibraryIssuesLabel = new JLabel("0 inconsistencias de tags");
        this.selectedTagUsageLabel = new JLabel("0 usos no manuscrito");
        this.selectedTagStatusLabel = new JLabel("Tag valida");
        this.tagDetailModeLabel = new JLabel("Editando tag existente");
        this.selectedProfileCharacterLabel = new JLabel("Nenhum perfil selecionado");
        this.selectedProfileStatusLabel = new JLabel("Sem inconsistencias");
        this.selectedCharacterScenesLabel = new JLabel("0 cenas ligadas");
        this.selectedCharacterPointOfViewLabel = new JLabel("Nao e o POV atual");
        this.selectedCharacterTagsLabel = new JLabel("Tags do personagem: nenhuma");
        this.characterDetailModeLabel = new JLabel("Editando personagem existente");
        this.characterDraftHintLabel = new JLabel(DesktopDraftStateFormatter.characterDraftHint(false, "", ""));
        this.tagDraftHintLabel = new JLabel(DesktopDraftStateFormatter.tagDraftHint(false, "", "", ""));
        this.statusLabel = new JLabel("Nenhum projeto carregado.");
        this.chapterListModel = new DefaultListModel<>();
        this.sceneListModel = new DefaultListModel<>();
        this.searchListModel = new DefaultListModel<>();
        this.tagListModel = new DefaultListModel<>();
        this.tagSuggestionListModel = new DefaultListModel<>();
        this.characterListModel = new DefaultListModel<>();
        this.povListModel = new DefaultListModel<>();
        this.profileListModel = new DefaultListModel<>();
        this.chapterList = new JList<>(chapterListModel);
        this.sceneList = new JList<>(sceneListModel);
        this.searchList = new JList<>(searchListModel);
        this.tagList = new JList<>(tagListModel);
        this.tagSuggestionList = new JList<>(tagSuggestionListModel);
        this.characterList = new JList<>(characterListModel);
        this.povList = new JList<>(povListModel);
        this.profileList = new JList<>(profileListModel);
        this.editorStructureTree = new JTree(new DefaultMutableTreeNode("Livro"));
        this.sceneUndoManager = new UndoManager();
        this.searchMatches = new ArrayList<>();
        this.visibleTags = new ArrayList<>();
        this.visibleTagSuggestions = new ArrayList<>();
        this.visibleCharacters = new ArrayList<>();
        this.visiblePointOfViewCharacters = new ArrayList<>();
        this.visibleProfiles = new ArrayList<>();
        this.favoriteTagIds = new LinkedHashSet<>();
        this.recentTagIds = new ArrayList<>();
        this.searchRefreshTimer = new Timer(250, event -> refreshSearchResultsNow());
        this.searchRefreshTimer.setRepeats(false);
        this.tagSuggestionPopup = new JPopupMenu();
        this.tagSuggestionPopup.setFocusable(false);
        this.tagSuggestionPopup.setRequestFocusEnabled(false);
        this.readingModeToggle = new JToggleButton(DesktopWritingProductivityFormatter.modeToggleLabel(TemplateExpansionMode.DRAFT));
        this.readingModeToggle.setToolTipText("Alterna entre os modos Rascunho e Render.");
        this.templateExpansionMode = TemplateExpansionMode.DRAFT;
        this.analysisPanel = new DesktopAnalysisPanel();
        assignComponentNames();
        this.editorStructureCoordinator = new DesktopEditorStructureCoordinator(
                new DesktopEditorStructureCoordinator.Host() {
                    @Override
                    public Project currentProject() {
                        return currentProject;
                    }

                    @Override
                    public Chapter selectedChapter() {
                        return selectedChapter;
                    }

                    @Override
                    public void setSelectedChapter(Chapter chapter) {
                        selectedChapter = chapter;
                    }

                    @Override
                    public Scene selectedScene() {
                        return selectedScene;
                    }

                    @Override
                    public void setSelectedScene(Scene scene) {
                        selectedScene = scene;
                    }

                    @Override
                    public boolean isSyncingUi() {
                        return syncingUi;
                    }

                    @Override
                    public void setSyncingUi(boolean value) {
                        syncingUi = value;
                    }

                    @Override
                    public void syncProjectFromFields() {
                        StoryFlameDesktopApp.this.syncProjectFromFields();
                    }

                    @Override
                    public void ensureChapterHasScene(Chapter chapter) {
                        StoryFlameDesktopApp.this.ensureChapterHasScene(chapter);
                    }

                    @Override
                    public String displayTitle(String value, String fallbackPrefix) {
                        return StoryFlameDesktopApp.this.displayTitle(value, fallbackPrefix);
                    }

                    @Override
                    public void renderSummary() {
                        StoryFlameDesktopApp.this.renderSummary();
                    }

                    @Override
                    public void refreshPointOfViewList() {
                        StoryFlameDesktopApp.this.refreshPointOfViewList();
                    }

                    @Override
                    public void updateWordCount() {
                        StoryFlameDesktopApp.this.updateWordCount();
                    }

                    @Override
                    public void updateTagCountLabel() {
                        StoryFlameDesktopApp.this.updateTagCountLabel();
                    }

                    @Override
                    public void hideTagSuggestionPopup() {
                        StoryFlameDesktopApp.this.hideTagSuggestionPopup();
                    }

                    @Override
                    public String projectPathText() {
                        return currentPath == null ? "Sem arquivo" : currentPath.toString();
                    }

                    @Override
                    public String currentStatusText() {
                        return statusLabel.getText();
                    }

                    @Override
                    public void setStatusText(String text) {
                        statusLabel.setText(text);
                    }
                },
                chapterListModel,
                sceneListModel,
                chapterList,
                sceneList,
                editorStructureTree,
                chapterTitleField,
                sceneTitleField,
                sceneSynopsisArea,
                sceneEditorArea,
                chapterCountLabel,
                sceneCountLabel,
                contextLabel,
                projectPathLabel,
                searchRefreshTimer,
                sceneUndoManager
        );
        this.characterCoordinator = new DesktopCharacterCoordinator(
                new DesktopCharacterCoordinator.Host() {
                    @Override
                    public Project currentProject() {
                        return currentProject;
                    }

                    @Override
                    public Scene selectedScene() {
                        return selectedScene;
                    }

                    @Override
                    public Character selectedCharacter() {
                        return selectedCharacter;
                    }

                    @Override
                    public void setSelectedCharacter(Character character) {
                        selectedCharacter = character;
                    }

                    @Override
                    public Character selectedCharacterBeforeDraft() {
                        return selectedCharacterBeforeDraft;
                    }

                    @Override
                    public void setSelectedCharacterBeforeDraft(Character character) {
                        selectedCharacterBeforeDraft = character;
                    }

                    @Override
                    public boolean isCharacterDraftMode() {
                        return characterDraftMode;
                    }

                    @Override
                    public void setCharacterDraftMode(boolean value) {
                        characterDraftMode = value;
                    }

                    @Override
                    public CharacterTagProfile selectedProfile() {
                        return selectedProfile;
                    }

                    @Override
                    public void setSelectedProfile(CharacterTagProfile profile) {
                        selectedProfile = profile;
                    }

                    @Override
                    public boolean isSyncingUi() {
                        return syncingUi;
                    }

                    @Override
                    public void setSyncingUi(boolean value) {
                        syncingUi = value;
                    }

                    @Override
                    public void updateIntegrityLabel() {
                        StoryFlameDesktopApp.this.updateIntegrityLabel();
                    }

                    @Override
                    public void refreshTagProfiles() {
                        StoryFlameDesktopApp.this.refreshTagProfiles();
                    }

                    @Override
                    public void displayNoResultsStatus(String text) {
                        statusLabel.setText(text);
                    }

                    @Override
                    public String displayTitle(String value, String fallbackPrefix) {
                        return StoryFlameDesktopApp.this.displayTitle(value, fallbackPrefix);
                    }
                },
                visibleCharacters,
                characterListModel,
                characterList,
                characterSearchField,
                characterNameField,
                characterDescriptionArea,
                characterCountLabel,
                selectedCharacterScenesLabel,
                selectedCharacterPointOfViewLabel,
                selectedCharacterTagsLabel,
                characterDetailModeLabel,
                characterDraftHintLabel
        );
        this.characterWorkflow = new DesktopCharacterWorkflow(
                new DesktopCharacterWorkflow.Host() {
                    @Override
                    public boolean isSyncingUi() {
                        return syncingUi;
                    }

                    @Override
                    public DesktopCharacterWorkflow.PathState currentPathState() {
                        return new DesktopCharacterWorkflow.PathState(currentPath != null);
                    }

                    @Override
                    public Project currentProject() {
                        return currentProject;
                    }

                    @Override
                    public Scene selectedScene() {
                        return selectedScene;
                    }

                    @Override
                    public Character selectedCharacter() {
                        return selectedCharacter;
                    }

                    @Override
                    public void setSelectedCharacter(Character character) {
                        selectedCharacter = character;
                    }

                    @Override
                    public Character selectedCharacterBeforeDraft() {
                        return selectedCharacterBeforeDraft;
                    }

                    @Override
                    public void setSelectedCharacterBeforeDraft(Character character) {
                        selectedCharacterBeforeDraft = character;
                    }

                    @Override
                    public boolean isCharacterDraftMode() {
                        return characterDraftMode;
                    }

                    @Override
                    public void setCharacterDraftMode(boolean value) {
                        characterDraftMode = value;
                    }

                    @Override
                    public CharacterTagProfile selectedProfile() {
                        return selectedProfile;
                    }

                    @Override
                    public void setSelectedProfile(CharacterTagProfile profile) {
                        selectedProfile = profile;
                    }

                    @Override
                    public NarrativeTag selectedTag() {
                        return selectedTag;
                    }

                    @Override
                    public void syncProjectFromFields() {
                        StoryFlameDesktopApp.this.syncProjectFromFields();
                    }

                    @Override
                    public void syncProjectFromFieldsExceptCharacter() {
                        StoryFlameDesktopApp.this.syncProjectFromFieldsExceptCharacter();
                    }

                    @Override
                    public void refreshCharacterLists() {
                        StoryFlameDesktopApp.this.refreshCharacterLists();
                    }

                    @Override
                    public void refreshTagLibrary() {
                        StoryFlameDesktopApp.this.refreshTagLibrary();
                    }

                    @Override
                    public void refreshTagProfiles() {
                        StoryFlameDesktopApp.this.refreshTagProfiles();
                    }

                    @Override
                    public void refreshPointOfViewList() {
                        StoryFlameDesktopApp.this.refreshPointOfViewList();
                    }

                    @Override
                    public void renderSummary() {
                        StoryFlameDesktopApp.this.renderSummary();
                    }

                    @Override
                    public void scheduleAutosave() {
                        StoryFlameDesktopApp.this.scheduleAutosave();
                    }

                    @Override
                    public void onProjectEdited() {
                        StoryFlameDesktopApp.this.onProjectEdited();
                    }

                    @Override
                    public void updateIntegrityLabel() {
                        StoryFlameDesktopApp.this.updateIntegrityLabel();
                    }

                    @Override
                    public void setStatusText(String text) {
                        statusLabel.setText(text);
                    }

                    @Override
                    public JFrame frame() {
                        return frame;
                    }

                    @Override
                    public void focusEditorFrame() {
                        StoryFlameDesktopApp.this.focusEditorFrame();
                    }
                },
                projectCharacterApplicationService,
                characterSearchField,
                characterNameField,
                characterDescriptionArea
        );
        this.tagCoordinator = new DesktopTagCoordinator(
                new DesktopTagCoordinator.Host() {
                    @Override
                    public Project currentProject() {
                        return currentProject;
                    }

                    @Override
                    public Scene selectedScene() {
                        return selectedScene;
                    }

                    @Override
                    public Character selectedCharacter() {
                        return selectedCharacter;
                    }

                    @Override
                    public void setSelectedCharacter(Character character) {
                        selectedCharacter = character;
                    }

                    @Override
                    public NarrativeTag selectedTag() {
                        return selectedTag;
                    }

                    @Override
                    public void setSelectedTag(NarrativeTag tag) {
                        selectedTag = tag;
                    }

                    @Override
                    public NarrativeTag selectedTagBeforeDraft() {
                        return selectedTagBeforeDraft;
                    }

                    @Override
                    public void setSelectedTagBeforeDraft(NarrativeTag tag) {
                        selectedTagBeforeDraft = tag;
                    }

                    @Override
                    public boolean isTagDraftMode() {
                        return tagDraftMode;
                    }

                    @Override
                    public void setTagDraftMode(boolean value) {
                        tagDraftMode = value;
                    }

                    @Override
                    public CharacterTagProfile selectedProfile() {
                        return selectedProfile;
                    }

                    @Override
                    public void setSelectedProfile(CharacterTagProfile profile) {
                        selectedProfile = profile;
                    }

                    @Override
                    public boolean isSyncingUi() {
                        return syncingUi;
                    }

                    @Override
                    public void setSyncingUi(boolean value) {
                        syncingUi = value;
                    }

                    @Override
                    public String displayTitle(String value, String fallbackPrefix) {
                        return StoryFlameDesktopApp.this.displayTitle(value, fallbackPrefix);
                    }

                    @Override
                    public List<NarrativeTag> sortedTagsForProductivity(List<NarrativeTag> source, String normalizedQuery) {
                        return StoryFlameDesktopApp.this.sortedTagsForProductivity(source, normalizedQuery);
                    }

                    @Override
                    public void updateTagLibraryIssuesLabel() {
                        StoryFlameDesktopApp.this.updateTagLibraryIssuesLabel();
                    }

                    @Override
                    public void refreshCharacterLists() {
                        StoryFlameDesktopApp.this.refreshCharacterLists();
                    }
                },
                visibleTags,
                visibleProfiles,
                tagListModel,
                profileListModel,
                tagList,
                profileList,
                tagSearchField,
                tagIdField,
                tagLabelField,
                tagTemplateField,
                profilePrefixField,
                profilePreferredTagsField,
                tagLibraryIssuesLabel,
                selectedTagUsageLabel,
                selectedTagStatusLabel,
                tagDetailModeLabel,
                tagDraftHintLabel,
                selectedProfileCharacterLabel,
                selectedProfileStatusLabel,
                selectedCharacterTagsLabel
        );
        this.tagWorkflow = new DesktopTagWorkflow(
                new DesktopTagWorkflow.Host() {
                    @Override
                    public boolean isSyncingUi() {
                        return syncingUi;
                    }

                    @Override
                    public boolean hasCurrentPath() {
                        return currentPath != null;
                    }

                    @Override
                    public Project currentProject() {
                        return currentProject;
                    }

                    @Override
                    public NarrativeTag selectedTag() {
                        return selectedTag;
                    }

                    @Override
                    public void setSelectedTag(NarrativeTag tag) {
                        selectedTag = tag;
                    }

                    @Override
                    public NarrativeTag selectedTagBeforeDraft() {
                        return selectedTagBeforeDraft;
                    }

                    @Override
                    public void setSelectedTagBeforeDraft(NarrativeTag tag) {
                        selectedTagBeforeDraft = tag;
                    }

                    @Override
                    public boolean isTagDraftMode() {
                        return tagDraftMode;
                    }

                    @Override
                    public void setTagDraftMode(boolean value) {
                        tagDraftMode = value;
                    }

                    @Override
                    public CharacterTagProfile selectedProfile() {
                        return selectedProfile;
                    }

                    @Override
                    public Set<String> favoriteTagIds() {
                        return favoriteTagIds;
                    }

                    @Override
                    public DefaultListModel<String> tagListModel() {
                        return tagListModel;
                    }

                    @Override
                    public List<NarrativeTag> visibleTags() {
                        return visibleTags;
                    }

                    @Override
                    public JList<String> tagList() {
                        return tagList;
                    }

                    @Override
                    public void refreshTagLibrary() {
                        StoryFlameDesktopApp.this.refreshTagLibrary();
                    }

                    @Override
                    public void refreshTagProfiles() {
                        StoryFlameDesktopApp.this.refreshTagProfiles();
                    }

                    @Override
                    public void renderSummary() {
                        StoryFlameDesktopApp.this.renderSummary();
                    }

                    @Override
                    public void scheduleAutosave() {
                        StoryFlameDesktopApp.this.scheduleAutosave();
                    }

                    @Override
                    public void setStatusText(String text) {
                        statusLabel.setText(text);
                    }

                    @Override
                    public void updateTagLibraryIssuesLabel() {
                        StoryFlameDesktopApp.this.updateTagLibraryIssuesLabel();
                    }

                    @Override
                    public void updateTagProductivityLabels() {
                        StoryFlameDesktopApp.this.updateTagProductivityLabels();
                    }

                    @Override
                    public JFrame frame() {
                        return frame;
                    }

                    @Override
                    public String displayTitle(String value, String fallbackPrefix) {
                        return StoryFlameDesktopApp.this.displayTitle(value, fallbackPrefix);
                    }

                    @Override
                    public String sanitizeTagId(String value) {
                        return StoryFlameDesktopApp.this.sanitizeTagId(value);
                    }

                    @Override
                    public String ensureUniqueTagId(String baseId) {
                        return StoryFlameDesktopApp.this.ensureUniqueTagId(baseId);
                    }
                },
                projectTagApplicationService,
                tagSearchField,
                tagIdField,
                tagLabelField,
                tagTemplateField,
                selectedTagUsageLabel::setText,
                selectedTagStatusLabel::setText,
                tagDetailModeLabel::setText
        );
        this.tagAutocompleteController = new DesktopEditorTagAutocomplete(
                new DesktopEditorTagAutocomplete.Host() {
                    @Override
                    public Project currentProject() {
                        return currentProject;
                    }

                    @Override
                    public Scene selectedScene() {
                        return selectedScene;
                    }

                    @Override
                    public boolean isSyncingUi() {
                        return syncingUi;
                    }

                    @Override
                    public List<NarrativeTag> sortedTagsForProductivity(List<NarrativeTag> source, String normalizedQuery) {
                        return StoryFlameDesktopApp.this.sortedTagsForProductivity(source, normalizedQuery);
                    }

                    @Override
                    public boolean isFavoriteTag(String tagId) {
                        return favoriteTagIds.contains(tagId);
                    }

                    @Override
                    public boolean isRecentTag(String tagId) {
                        return recentTagIds.contains(tagId);
                    }

                    @Override
                    public void registerRecentTag(String tagId) {
                        StoryFlameDesktopApp.this.registerRecentTag(tagId);
                    }

                    @Override
                    public void setStatusText(String text) {
                        statusLabel.setText(text);
                    }
                },
                sceneEditorArea,
                hoverTagPreviewLabel::setText,
                tagSuggestionPopup,
                tagSuggestionList,
                tagSuggestionListModel,
                visibleTagSuggestions
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StoryFlameDesktopApp().showWindow());
    }

    private void showWindow() {
        applyDesktopLookAndFeel();
        frame = new JFrame("StoryFlame Desktop");
        backgroundCoordinator = new DesktopBackgroundCoordinator(frame, statusLabel);
        projectWorkflow = new DesktopProjectWorkflow(
                projectApplicationService,
                store,
                publicationExportService,
                backgroundCoordinator,
                statusLabel
        );
        frame.setName("mainFrame");
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

    JFrame showWindowForTests() {
        showWindow();
        return frame;
    }

    void closeWindowForTests() {
        autosaveService.close();
        if (frame != null) {
            frame.dispose();
        }
    }

    void openProjectForTests(Path path) {
        projectWorkflow.openProject(path, this::applyLoadedProjectState);
    }

    void importProjectArchiveForTests(Path path) {
        projectWorkflow.importProjectArchive(path, frame, this::applyLoadedProjectState);
    }

    void exportPublishableForTests(PublicationFormat format, Path path) {
        if (currentProject == null) {
            return;
        }
        syncProjectFromFields();
        projectWorkflow.exportPublishable(currentProject, currentPath, format, path, false);
    }

    void runEmotionAnalysisForTests() {
        runEmotionAnalysis();
    }

    private JPanel buildRootPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setName("mainTabs");
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
        fileMenu.add(busyManagedMenuItem("Novo projeto", "control N", this::createProject));
        fileMenu.add(busyManagedMenuItem("Abrir...", "control O", () -> openProject(frame)));
        fileMenu.add(busyManagedMenuItem("Salvar", "control S", this::saveProject));
        fileMenu.add(busyManagedMenuItem("Exportar projeto ZIP...", "control shift E", () -> exportProjectArchive(frame)));
        fileMenu.add(busyManagedMenuItem("Importar projeto ZIP...", "control shift I", () -> importProjectArchive(frame)));
        fileMenu.add(busyManagedMenuItem("Verificar arquivo...", "control shift V", () -> inspectProjectArchive(frame)));
        fileMenu.addSeparator();
        fileMenu.add(busyManagedMenuItem("Publicar manuscrito em TXT...", "control alt T", () -> exportPublishable(frame, PublicationFormat.TXT)));
        fileMenu.add(busyManagedMenuItem("Publicar manuscrito em MD...", "control alt M", () -> exportPublishable(frame, PublicationFormat.MARKDOWN)));
        fileMenu.add(busyManagedMenuItem("Publicar manuscrito em PDF...", "control alt D", () -> exportPublishable(frame, PublicationFormat.PDF)));
        fileMenu.add(busyManagedMenuItem("Publicar manuscrito em EPUB...", "control alt U", () -> exportPublishable(frame, PublicationFormat.EPUB)));
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
        windowMenu.add(menuItem("Analise", "F7", this::focusAnalysisFrame));

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
        JButton exportButton = new JButton("Exportar projeto");
        JButton importButton = new JButton("Importar projeto");
        JButton publishButton = new JButton("Publicar manuscrito");
        JButton structureButton = new JButton("Estrutura");
        JButton projectButton = new JButton("Projeto");
        JButton searchButton = new JButton("Buscar");
        JButton characterButton = new JButton("Personagens");
        JButton tagsButton = new JButton("Tags");
        JButton analysisButton = new JButton("Analise");
        readingModeToggle.addActionListener(event -> setTemplateExpansionMode(
                readingModeToggle.isSelected() ? TemplateExpansionMode.RENDER : TemplateExpansionMode.DRAFT
        ));

        newButton.addActionListener(event -> createProject());
        openButton.addActionListener(event -> openProject(frame));
        saveButton.setName("saveButton");
        saveButton.addActionListener(event -> saveProject());
        exportButton.addActionListener(event -> exportProjectArchive(frame));
        importButton.addActionListener(event -> importProjectArchive(frame));
        publishButton.setName("publishButton");
        publishButton.addActionListener(event -> showPublicationFormatDialog(frame));
        structureButton.addActionListener(event -> focusStructureFrame());
        projectButton.addActionListener(event -> focusProjectFrame());
        searchButton.addActionListener(event -> focusSearchFrame());
        characterButton.addActionListener(event -> focusCharacterFrame());
        tagsButton.addActionListener(event -> focusTagsFrame());
        analysisButton.addActionListener(event -> focusAnalysisFrame());
        registerBusyManagedButton(newButton);
        registerBusyManagedButton(openButton);
        registerBusyManagedButton(saveButton);
        registerBusyManagedButton(exportButton);
        registerBusyManagedButton(importButton);
        registerBusyManagedButton(publishButton);

        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.add(exportButton);
        toolBar.add(importButton);
        toolBar.add(publishButton);
        toolBar.addSeparator();
        toolBar.add(structureButton);
        toolBar.add(projectButton);
        toolBar.add(searchButton);
        toolBar.add(characterButton);
        toolBar.add(tagsButton);
        toolBar.add(analysisButton);
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
        toolBar.add(favoriteTagCountLabel);
        toolBar.add(Box.createHorizontalStrut(12));
        toolBar.add(recentTagCountLabel);
        toolBar.add(Box.createHorizontalStrut(12));
        toolBar.add(renderModeLabel);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(readingModeToggle);
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
        tabbedPane.addTab("Analise", buildAnalysisPanel());
    }

    private JPanel buildEditorPanel() {
        return DesktopEditorStructurePanels.buildEditorPanel(
                editorStructureTree,
                sceneTitleField,
                sceneSynopsisArea,
                sceneEditorArea,
                buildPointOfViewPanel(),
                projectPathLabel,
                renderModeLabel,
                wordCountLabel,
                hoverTagPreviewLabel
        );
    }

    private JPanel buildStructurePanel() {
        return DesktopEditorStructurePanels.buildStructurePanel(
                buildChapterToolbar(),
                chapterList,
                chapterTitleField,
                buildSceneToolbar(),
                sceneList,
                chapterCountLabel,
                sceneCountLabel,
                characterCountLabel,
                contextLabel
        );
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
        registerBusyManagedButton(newButton);
        registerBusyManagedButton(openButton);
        registerBusyManagedButton(saveButton);
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

    private JPanel buildAnalysisPanel() {
        analysisPanel.setAnalyzeAction(this::runEmotionAnalysis);
        backgroundCoordinator.registerBusyStateHandler(busy -> analysisPanel.setAnalyzeEnabled(!busy));
        return analysisPanel.component();
    }

    private JPanel buildCharacterPanel() {
        return DesktopCharacterPanels.buildCharacterPanel(
                characterSearchField,
                characterList,
                characterCountLabel,
                integrityLabel,
                characterDetailModeLabel,
                characterNameField,
                selectedCharacterScenesLabel,
                selectedCharacterPointOfViewLabel,
                characterDescriptionArea,
                selectedCharacterTagsLabel,
                characterDraftHintLabel,
                this::addCharacter,
                this::deleteCharacter,
                this::applyCharacterNameUpdate,
                this::cancelCharacterDraft,
                () -> characterSearchField.setText("")
        );
    }

    private JPanel buildTagsPanel() {
        return DesktopTagPanels.buildTagsPanel(
                tagSearchField,
                tagList,
                tagIdField,
                tagLabelField,
                tagTemplateField,
                tagDetailModeLabel,
                tagDraftHintLabel,
                selectedTagUsageLabel,
                selectedTagStatusLabel,
                tagCountLabel,
                tagLibraryIssuesLabel,
                this::addTag,
                this::deleteTag,
                () -> tagSearchField.setText(""),
                this::saveTag,
                this::cancelTagDraft
        );
    }

    private JPanel buildPointOfViewPanel() {
        return DesktopEditorStructurePanels.buildPointOfViewPanel(
                pointOfViewLabel,
                sceneContextSynopsisLabel,
                sceneContextCharactersLabel,
                sceneContextTagsLabel,
                sceneContextIntegrityLabel,
                povSearchField,
                povList,
                this::applySelectedPointOfView,
                this::clearPointOfView
        );
    }

    private JPanel buildChapterToolbar() {
        return DesktopEditorStructurePanels.buildChapterToolbar(
                this::addChapter,
                this::deleteChapter,
                () -> moveChapter(-1),
                () -> moveChapter(1),
                this::focusEditorFrame
        );
    }

    private JPanel buildSceneToolbar() {
        return DesktopEditorStructurePanels.buildSceneToolbar(
                this::addScene,
                this::deleteScene,
                () -> moveScene(-1),
                () -> moveScene(1),
                this::focusEditorFrame
        );
    }

    private JLabel buildSectionHint(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(111, 101, 84));
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        return label;
    }

    private JComponent buildSectionHintLabel(JLabel label) {
        label.setForeground(new Color(111, 101, 84));
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        return label;
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

    private JMenuItem busyManagedMenuItem(String label, String keyStroke, Runnable action) {
        JMenuItem item = menuItem(label, keyStroke, action);
        registerBusyManagedButton(item);
        return item;
    }

    private void registerBusyManagedButton(javax.swing.AbstractButton button) {
        if (backgroundCoordinator != null) {
            backgroundCoordinator.registerBusyManagedButton(button);
        }
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
        DocumentListener sceneSynopsisListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onSceneSynopsisEdited();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onSceneSynopsisEdited();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onSceneSynopsisEdited();
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
        sceneSynopsisArea.getDocument().addDocumentListener(sceneSynopsisListener);
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
        editorStructureTree.setRootVisible(false);
        editorStructureTree.setShowsRootHandles(true);

        chapterList.addListSelectionListener(this::onChapterSelected);
        sceneList.addListSelectionListener(this::onSceneSelected);
        searchList.addListSelectionListener(this::onSearchSelected);
        tagList.addListSelectionListener(this::onTagSelected);
        characterList.addListSelectionListener(this::onCharacterSelected);
        profileList.addListSelectionListener(this::onProfileSelected);
        editorStructureTree.addTreeSelectionListener(event -> onEditorTreeSelected());
        searchList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    navigateToSearchSelection();
                }
            }
        });
        searchList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "storyflame-open-search-result");
        searchList.getActionMap().put("storyflame-open-search-result", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                navigateToSearchSelection();
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
        installTagAutocomplete(sceneEditorArea);
        installWindowShortcut(frame.getRootPane(), "F1", this::focusEditorFrame);
        installWindowShortcut(frame.getRootPane(), "F2", this::focusStructureFrame);
        installWindowShortcut(frame.getRootPane(), "F3", this::focusProjectFrame);
        installWindowShortcut(frame.getRootPane(), "F4", this::focusSearchFrame);
        installWindowShortcut(frame.getRootPane(), "F5", this::focusCharacterFrame);
        installWindowShortcut(frame.getRootPane(), "F6", this::focusTagsFrame);
        installWindowShortcut(frame.getRootPane(), "F7", this::focusAnalysisFrame);
        installWindowShortcut(frame.getRootPane(), "control alt P", this::assignSelectedCharacterAsPointOfView);
    }

    private void createProject() {
        projectWorkflow.createProject(state -> {
            applyLoadedProjectState(state);
            focusEditorFrame();
        }, () -> {
            currentProject = null;
            currentPath = null;
            selectedChapter = null;
            selectedScene = null;
            selectedCharacter = null;
        });
    }

    private void openProject(JFrame owner) {
        projectWorkflow.openProject(owner, this::applyLoadedProjectState);
    }

    private void saveProject() {
        if (currentProject == null || currentPath == null) {
            return;
        }
        syncProjectFromFields();
        projectWorkflow.saveProject(currentProject, currentPath, savedPath -> {
            currentPath = savedPath;
            renderSummary();
            statusLabel.setText(DesktopOperationStatusFormatter.success("Projeto salvo em " + currentPath));
        });
    }

    private void exportProjectArchive(JFrame owner) {
        if (currentProject == null) {
            return;
        }
        syncProjectFromFields();
        projectWorkflow.exportProjectArchive(owner, currentProject, currentPath);
    }

    private void importProjectArchive(JFrame owner) {
        projectWorkflow.importProjectArchive(owner, this::applyLoadedProjectState);
    }

    private void inspectProjectArchive(JFrame owner) {
        projectWorkflow.inspectProjectArchive(owner);
    }

    private void exportPublishable(JFrame owner, PublicationFormat format) {
        if (currentProject == null) {
            return;
        }
        syncProjectFromFields();
        projectWorkflow.exportPublishable(owner, currentProject, currentPath, format);
    }

    private void showPublicationFormatDialog(JFrame owner) {
        if (currentProject == null) {
            return;
        }
        syncProjectFromFields();
        projectWorkflow.showPublicationFormatDialog(owner, currentProject, currentPath);
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
        autosaveService.schedule(currentProject, autosavePath, () -> {
            createProjectBackup(autosavePath);
            SwingUtilities.invokeLater(() -> statusLabel.setText("Alteracoes automaticas salvas em " + autosavePath));
        }, exception -> SwingUtilities.invokeLater(() -> backgroundCoordinator.handleAutosaveFailure(autosavePath, exception)));
        statusLabel.setText("Salvando versao automatica...");
    }

    private void onMetadataEdited() {
        onProjectEdited();
    }

    private void onSceneTitleEdited() {
        if (syncingUi || currentProject == null || currentPath == null) {
            return;
        }
        if (selectedScene != null) {
            projectEditorApplicationService.updateSceneTitle(currentProject, selectedScene, sceneTitleField.getText());
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
            projectEditorApplicationService.updateChapterTitle(currentProject, selectedChapter, chapterTitleField.getText());
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
            projectEditorApplicationService.updateSceneContent(currentProject, selectedScene, sceneEditorArea.getText());
        }
        updateWordCount();
        updateTagCountLabel();
        renderSummary();
        scheduleSearchRefresh();
        showTagSuggestionPopup(false);
        scheduleAutosave();
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void onSceneSynopsisEdited() {
        if (syncingUi || currentProject == null || currentPath == null) {
            return;
        }
        if (selectedScene != null) {
            projectEditorApplicationService.updateSceneSynopsis(currentProject, selectedScene, sceneSynopsisArea.getText());
        }
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("Alteracoes pendentes...");
    }

    private void onCharacterEdited() {
        characterWorkflow.onCharacterEdited();
    }

    private void applyCharacterNameUpdate() {
        characterWorkflow.applyCharacterNameUpdate();
    }

    private void onTagEdited() {
        tagWorkflow.onTagEdited();
    }

    private void onProfileEdited() {
        tagWorkflow.onProfileEdited();
    }

    private void syncFieldsFromProject() {
        syncingUi = true;
        titleField.setText(currentProject.getTitle());
        authorField.setText(currentProject.getAuthor());
        CharacterTagProfileSynchronizer.synchronize(currentProject);
        editorStructureCoordinator.refreshStructureLists();
        characterCoordinator.refreshCharacterLists();
        tagCoordinator.refreshTagLibrary();
        tagCoordinator.refreshTagProfiles();
        renderEmotionAnalysis();
        updateTagProductivityLabels();
        editorStructureCoordinator.updateEditorFields();
        refreshSearchResultsNow();
        syncingUi = false;
    }

    private void syncProjectFromFields() {
        projectEditorApplicationService.updateProjectMetadata(currentProject, titleField.getText(), authorField.getText());
        if (selectedChapter != null) {
            projectEditorApplicationService.updateChapterTitle(currentProject, selectedChapter, chapterTitleField.getText());
        }
        if (selectedScene != null) {
            projectEditorApplicationService.updateSceneDraft(
                    currentProject,
                    selectedScene,
                    sceneTitleField.getText(),
                    sceneSynopsisArea.getText(),
                    sceneEditorArea.getText()
            );
        }
        if (selectedCharacter != null) {
            projectCharacterApplicationService.updateCharacter(
                    currentProject,
                    selectedCharacter,
                    characterNameField.getText(),
                    characterDescriptionArea.getText()
            );
        }
        updateWordCount();
    }

    private void addChapter() {
        ensureSelectionState();
        if (currentProject == null) {
            return;
        }
        syncProjectFromFields();
        ProjectStructureApplicationService.StructureSelection selection =
                projectStructureApplicationService.addChapter(currentProject);
        selectedChapter = selection.chapter();
        selectedScene = selection.scene();
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void deleteChapter() {
        ensureSelectionState();
        if (currentProject == null || selectedChapter == null) {
            statusLabel.setText("Selecione um capitulo para excluir.");
            return;
        }
        if (currentProject.getChapters().size() <= 1) {
            statusLabel.setText("Nao e possivel excluir o unico capitulo.");
            return;
        }
        syncProjectFromFields();
        ProjectStructureApplicationService.StructureSelection selection =
                projectStructureApplicationService.removeChapter(currentProject, selectedChapter);
        selectedChapter = selection.chapter();
        selectedScene = selection.scene();
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void moveChapter(int offset) {
        ensureSelectionState();
        if (currentProject == null || selectedChapter == null) {
            statusLabel.setText("Selecione um capitulo para mover.");
            return;
        }
        syncProjectFromFields();
        try {
            ProjectStructureApplicationService.StructureSelection selection =
                    projectStructureApplicationService.moveChapter(currentProject, selectedChapter, offset);
            selectedChapter = selection.chapter();
            selectedScene = selection.scene();
        } catch (IllegalStateException exception) {
            statusLabel.setText(exception.getMessage());
            return;
        }
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
        ProjectStructureApplicationService.StructureSelection selection =
                projectStructureApplicationService.addScene(currentProject, selectedChapter);
        selectedChapter = selection.chapter();
        selectedScene = selection.scene();
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void addCharacter() {
        characterWorkflow.addCharacter();
    }

    private void deleteCharacter() {
        characterWorkflow.deleteCharacter();
    }

    private void cancelCharacterDraft() {
        characterWorkflow.cancelCharacterDraft();
    }

    private void addTag() {
        tagWorkflow.addTag();
    }

    private void deleteTag() {
        tagWorkflow.deleteTag();
    }

    private void cancelTagDraft() {
        tagWorkflow.cancelTagDraft();
    }

    private void saveTag() {
        tagWorkflow.saveTag();
    }

    private void duplicateTag() {
        tagWorkflow.duplicateTag();
    }

    private void clearSelectedTagDraft() {
        tagWorkflow.clearSelectedTagDraft();
    }

    private void appendSelectedTagToProfile() {
        tagWorkflow.appendSelectedTagToProfile();
    }

    private void appendSelectedTagToCharacter() {
        characterWorkflow.appendSelectedTagToCharacter();
    }

    private void removeSelectedTagFromProfile() {
        tagWorkflow.removeSelectedTagFromProfile();
    }

    private void removeSelectedTagFromCharacter() {
        characterWorkflow.removeSelectedTagFromCharacter();
    }

    private void assignSelectedCharacterAsPointOfView() {
        characterWorkflow.assignSelectedCharacterAsPointOfView();
    }

    private void deleteScene() {
        ensureSelectionState();
        if (selectedChapter == null || selectedScene == null) {
            statusLabel.setText("Selecione uma cena para excluir.");
            return;
        }
        if (selectedChapter.getScenes().size() <= 1) {
            statusLabel.setText("Nao e possivel excluir a unica cena deste capitulo.");
            return;
        }
        syncProjectFromFields();
        ProjectStructureApplicationService.StructureSelection selection =
                projectStructureApplicationService.removeScene(currentProject, selectedChapter, selectedScene);
        selectedChapter = selection.chapter();
        selectedScene = selection.scene();
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void moveScene(int offset) {
        ensureSelectionState();
        if (selectedChapter == null || selectedScene == null) {
            statusLabel.setText("Selecione uma cena para mover.");
            return;
        }
        syncProjectFromFields();
        try {
            ProjectStructureApplicationService.StructureSelection selection =
                    projectStructureApplicationService.moveScene(currentProject, selectedChapter, selectedScene, offset);
            selectedChapter = selection.chapter();
            selectedScene = selection.scene();
        } catch (IllegalStateException exception) {
            statusLabel.setText(exception.getMessage());
            return;
        }
        refreshStructureLists();
        selectCurrentObjects();
        onProjectEdited();
    }

    private void refreshStructureLists() {
        editorStructureCoordinator.refreshStructureLists();
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
        if (!searchField.getText().isBlank()) {
            statusLabel.setText(searchMatches.isEmpty()
                    ? "Nenhum resultado para \"" + searchField.getText().trim() + "\"."
                    : searchMatches.size() + " resultado(s) encontrados.");
        }
        renderSummary();
    }

    private void refreshCharacterLists() {
        characterCoordinator.refreshCharacterLists();
    }

    private void refreshTagLibrary() {
        tagCoordinator.refreshTagLibrary();
    }

    private void refreshTagProfiles() {
        tagCoordinator.refreshTagProfiles();
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

        Character selectedPovCharacter = DesktopProjectInsights.findCharacterById(currentProject, selectedScene == null ? null : selectedScene.getPointOfViewCharacterId());
        String query = povSearchField.getText() == null ? "" : povSearchField.getText().trim();
        for (Character character : CharacterDirectory.search(currentProject, query)) {
            visiblePointOfViewCharacters.add(character);
            povListModel.addElement(DesktopProjectInsights.displayCharacterName(currentProject, selectedScene, character));
        }
        if (selectedPovCharacter != null && visiblePointOfViewCharacters.contains(selectedPovCharacter)) {
            povList.setSelectedIndex(visiblePointOfViewCharacters.indexOf(selectedPovCharacter));
        }
        pointOfViewLabel.setText("POV: " + DesktopProjectInsights.displayPointOfViewName(currentProject, selectedScene));
        refreshSceneContextPanel();
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
        editorStructureCoordinator.selectCurrentObjects();
    }

    private void onEditorTreeSelected() {
        editorStructureCoordinator.onEditorTreeSelected();
    }

    private void onChapterSelected(ListSelectionEvent event) {
        editorStructureCoordinator.onChapterSelected(event);
    }

    private void onSceneSelected(ListSelectionEvent event) {
        editorStructureCoordinator.onSceneSelected(event);
    }

    private void onCharacterSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi || currentProject == null) {
            return;
        }
        syncProjectFromFields();
        characterCoordinator.onCharacterSelected(event);
    }

    private void onTagSelected(ListSelectionEvent event) {
        tagCoordinator.onTagSelected(event);
    }

    private void onProfileSelected(ListSelectionEvent event) {
        tagCoordinator.onProfileSelected(event);
    }

    private void onSearchSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || syncingUi) {
            return;
        }
        if (searchList.getSelectedIndex() >= 0) {
            statusLabel.setText("Resultado selecionado. Pressione Enter ou use duplo clique para abrir.");
        }
    }

    private void updateEditorFields() {
        editorStructureCoordinator.updateEditorFields();
    }

    private void renderSummary() {
        if (currentProject == null) {
            summaryArea.setText("");
            return;
        }
        summaryArea.setText(DesktopSummaryFormatter.format(
                currentProject,
                currentPath,
                selectedChapter,
                selectedScene,
                searchMatches,
                templateExpansionMode
        ));
    }

    private void runEmotionAnalysis() {
        if (currentProject == null) {
            return;
        }
        syncProjectFromFields();
        analysisPanel.showLoading();
        backgroundCoordinator.run(
                DesktopOperationStatusFormatter.runningEmotionAnalysis(),
                () -> emotionAnalysisService.analyze(currentProject),
                report -> {
                    renderEmotionAnalysis();
                    scheduleAutosave();
                    statusLabel.setText(DesktopOperationStatusFormatter.success(
                            "Leitura emocional atualizada com " + report.chunkCount() + " trechos avaliados."
                    ));
                },
                "Nao foi possivel gerar a analise emocional.",
                this::renderEmotionAnalysisFailure
        );
    }

    private void applyLoadedProjectState(DesktopProjectWorkflow.LoadedProjectState state) {
        currentPath = state.path();
        currentProject = state.project();
        selectedChapter = currentProject.getChapters().get(0);
        selectedScene = selectedChapter.getScenes().get(0);
        selectedCharacter = currentProject.getCharacters().isEmpty() ? null : currentProject.getCharacters().get(0);
        syncFieldsFromProject();
        statusLabel.setText(state.statusPrefix() + currentPath);
        focusEditorFrame();
    }

    private void renderEmotionAnalysis() {
        analysisPanel.render(currentProject == null ? null : currentProject.getEmotionAnalysis());
    }

    private void renderEmotionAnalysisFailure() {
        analysisPanel.showFailure(currentProject == null ? null : currentProject.getEmotionAnalysis());
    }

    private void createProjectBackup(Path archivePath) {
        if (currentProject == null || archivePath == null) {
            return;
        }
        try {
            backupService.createBackup(archivePath, currentProject);
        } catch (Exception exception) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(DesktopOperationStatusFormatter.partialBackupFailure()));
        }
    }

    private Path resolveSavePath(Path previousPath) {
        return ProjectStoragePaths.resolveManagedArchivePath(store.getBaseDirectory(), previousPath, currentProject);
    }

    private void ensureEditorStructure() {
        ensureEditorStructure(currentProject);
    }

    private void ensureEditorStructure(Project project) {
        if (project.getChapters().isEmpty()) {
            project.getChapters().add(new Chapter(null, "Capitulo 1", List.of(new Scene(null, "Cena 1", "", null))));
        }
        for (Chapter chapter : project.getChapters()) {
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
        sceneSynopsisArea.enableInputMethods(true);
        searchField.enableInputMethods(true);
        tagSearchField.enableInputMethods(true);
        characterSearchField.enableInputMethods(true);
        characterNameField.enableInputMethods(true);
        povSearchField.enableInputMethods(true);
        tagIdField.enableInputMethods(true);
        tagLabelField.enableInputMethods(true);
        tagTemplateField.enableInputMethods(true);
        sceneEditorArea.enableInputMethods(true);
        characterDescriptionArea.enableInputMethods(true);

        titleField.setFont(new Font("Serif", Font.PLAIN, 15));
        authorField.setFont(new Font("Serif", Font.PLAIN, 15));
        titleField.setBorder(BorderFactory.createTitledBorder("Titulo"));
        authorField.setBorder(BorderFactory.createTitledBorder("Autor"));
        chapterTitleField.setBorder(BorderFactory.createTitledBorder("Titulo do capitulo"));
        sceneTitleField.setBorder(BorderFactory.createTitledBorder("Titulo da cena"));
        sceneSynopsisArea.setBorder(BorderFactory.createTitledBorder("Sinopse da cena"));
        searchField.setBorder(BorderFactory.createTitledBorder("Buscar"));
        tagSearchField.setBorder(BorderFactory.createTitledBorder(DesktopDraftStateFormatter.tagSearchTitle()));
        characterSearchField.setBorder(BorderFactory.createTitledBorder(DesktopDraftStateFormatter.characterSearchTitle()));
        characterNameField.setBorder(BorderFactory.createTitledBorder(DesktopDraftStateFormatter.characterNameTitle()));
        povSearchField.setBorder(BorderFactory.createTitledBorder("Buscar POV"));
        tagIdField.setBorder(BorderFactory.createTitledBorder(DesktopDraftStateFormatter.tagIdTitle()));
        tagLabelField.setBorder(BorderFactory.createTitledBorder(DesktopDraftStateFormatter.tagLabelTitle()));
        tagTemplateField.setBorder(BorderFactory.createTitledBorder(DesktopDraftStateFormatter.tagTemplateTitle()));

        chapterList.setFont(new Font("Serif", Font.PLAIN, 15));
        sceneList.setFont(new Font("Serif", Font.PLAIN, 15));
        editorStructureTree.setFont(new Font("Serif", Font.PLAIN, 15));
        searchList.setFont(new Font("Serif", Font.PLAIN, 14));
        tagList.setFont(new Font("Serif", Font.PLAIN, 14));
        characterList.setFont(new Font("Serif", Font.PLAIN, 15));
        povList.setFont(new Font("Serif", Font.PLAIN, 14));
        profileList.setFont(new Font("Serif", Font.PLAIN, 14));
        chapterList.setFixedCellHeight(-1);
        sceneList.setFixedCellHeight(-1);
        editorStructureTree.setRowHeight(0);
        searchList.setFixedCellHeight(32);
        tagList.setFixedCellHeight(28);
        characterList.setFixedCellHeight(28);
        povList.setFixedCellHeight(28);
        profileList.setFixedCellHeight(28);

        sceneTitleField.setFont(new Font("Serif", Font.BOLD, 22));
        sceneSynopsisArea.setFont(new Font("Serif", Font.PLAIN, 15));
        sceneSynopsisArea.setBackground(new Color(251, 248, 242));
        sceneSynopsisArea.setForeground(new Color(66, 57, 46));
        sceneSynopsisArea.setCaretColor(new Color(66, 57, 46));
        sceneSynopsisArea.setLineWrap(true);
        sceneSynopsisArea.setWrapStyleWord(true);
        sceneSynopsisArea.setRows(3);
        sceneSynopsisArea.setMargin(new java.awt.Insets(10, 12, 10, 12));
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
        summaryArea.setBorder(BorderFactory.createTitledBorder("Preview renderizado"));

        characterDescriptionArea.setLineWrap(true);
        characterDescriptionArea.setWrapStyleWord(true);
        characterDescriptionArea.setRows(6);
        characterDescriptionArea.setFont(new Font("Serif", Font.PLAIN, 14));
        characterDescriptionArea.setBackground(new Color(251, 248, 242));
        characterDescriptionArea.setBorder(BorderFactory.createTitledBorder("Descricao"));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(246, 241, 232));
        statusLabel.setForeground(new Color(84, 72, 58));
    }

    private void assignComponentNames() {
        titleField.setName("titleField");
        authorField.setName("authorField");
        sceneTitleField.setName("sceneTitleField");
        sceneSynopsisArea.setName("sceneSynopsisArea");
        sceneEditorArea.setName("sceneEditorArea");
        sceneContextSynopsisLabel.setName("sceneContextSynopsisLabel");
        sceneContextCharactersLabel.setName("sceneContextCharactersLabel");
        sceneContextTagsLabel.setName("sceneContextTagsLabel");
        sceneContextIntegrityLabel.setName("sceneContextIntegrityLabel");
        characterNameField.setName("characterNameField");
        characterDescriptionArea.setName("characterDescriptionArea");
        tagIdField.setName("tagIdField");
        tagLabelField.setName("tagLabelField");
        tagTemplateField.setName("tagTemplateField");
        chapterList.setName("chapterList");
        sceneList.setName("sceneList");
        characterList.setName("characterList");
        tagList.setName("tagList");
        selectedCharacterTagsLabel.setName("selectedCharacterTagsLabel");
        characterDetailModeLabel.setName("characterDetailModeLabel");
        characterDraftHintLabel.setName("characterDraftHintLabel");
        selectedTagStatusLabel.setName("selectedTagStatusLabel");
        tagDetailModeLabel.setName("tagDetailModeLabel");
        tagDraftHintLabel.setName("tagDraftHintLabel");
        statusLabel.setName("statusLabel");
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

    private void installTagAutocomplete(JTextArea editor) {
        tagAutocompleteController.install();
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
                return;
            }
            statusLabel.setText("Nada para desfazer na cena atual.");
        } catch (CannotUndoException ignored) {
        }
    }

    private void redoSceneEdit() {
        try {
            if (sceneUndoManager.canRedo()) {
                sceneUndoManager.redo();
                return;
            }
            statusLabel.setText("Nada para refazer na cena atual.");
        } catch (CannotRedoException ignored) {
        }
    }

    private void updateWordCount() {
        wordCountLabel.setText(DesktopEditorStateFormatter.wordCountLabel(selectedScene, sceneEditorArea.getText()));
    }

    private void updateTagCountLabel() {
        List<ParsedNarrativeTag> parsedTags = DesktopProjectInsights.currentSceneTags(currentProject, selectedScene);
        tagCountLabel.setText(DesktopTagParseFormatter.labelText(parsedTags));
        tagCountLabel.setToolTipText(DesktopTagParseFormatter.tooltipText(parsedTags));
        refreshSceneContextPanel();
    }

    private void applySelectedPointOfView() {
        if (selectedScene == null) {
            return;
        }
        int selectedIndex = povList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visiblePointOfViewCharacters.size()) {
            return;
        }
        projectCharacterApplicationService.assignPointOfView(selectedScene, visiblePointOfViewCharacters.get(selectedIndex));
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
        projectCharacterApplicationService.clearPointOfView(selectedScene);
        refreshPointOfViewList();
        updateIntegrityLabel();
        renderSummary();
        scheduleAutosave();
        statusLabel.setText("POV removido.");
    }

    private void updateIntegrityLabel() {
        List<NarrativeIntegrityIssue> issues = NarrativeIntegrityValidator.findBrokenPointOfViewReferences(currentProject);
        integrityLabel.setText(DesktopNarrativeIntegrityFormatter.labelText(issues));
        integrityLabel.setToolTipText(DesktopNarrativeIntegrityFormatter.tooltipText(issues));
        refreshSceneContextPanel();
    }

    private void refreshSceneContextPanel() {
        sceneContextSynopsisLabel.setText(DesktopSceneContextFormatter.synopsisText(
                selectedScene == null ? null : selectedScene.getSynopsis()
        ));
        sceneContextCharactersLabel.setText(DesktopSceneContextFormatter.charactersText(
                DesktopProjectInsights.currentSceneLinkedCharacterNames(currentProject, selectedScene)
        ));
        sceneContextTagsLabel.setText(DesktopSceneContextFormatter.tagsText(
                DesktopProjectInsights.formatCurrentSceneTagSummary(currentProject, selectedScene)
        ));
        sceneContextIntegrityLabel.setText(DesktopSceneContextFormatter.integrityText(integrityLabel.getText()));
        sceneContextIntegrityLabel.setToolTipText(integrityLabel.getToolTipText());
    }

    private void syncProjectFromFieldsExceptCharacter() {
        projectEditorApplicationService.updateProjectMetadata(currentProject, titleField.getText(), authorField.getText());
        if (selectedChapter != null) {
            projectEditorApplicationService.updateChapterTitle(currentProject, selectedChapter, chapterTitleField.getText());
        }
        if (selectedScene != null) {
            projectEditorApplicationService.updateSceneDraft(
                    currentProject,
                    selectedScene,
                    sceneTitleField.getText(),
                    sceneSynopsisArea.getText(),
                    sceneEditorArea.getText()
            );
        }
        updateWordCount();
    }

    private void setTemplateExpansionMode(TemplateExpansionMode mode) {
        templateExpansionMode = mode;
        readingModeToggle.setSelected(mode == TemplateExpansionMode.RENDER);
        readingModeToggle.setText(DesktopWritingProductivityFormatter.modeToggleLabel(mode));
        renderModeLabel.setText(mode == TemplateExpansionMode.DRAFT ? "Rascunho" : "Render");
        renderSummary();
        statusLabel.setText(mode == TemplateExpansionMode.DRAFT
                ? "Modo rascunho ativo."
                : "Modo render ativo.");
    }

    private void updateTagLibraryIssuesLabel() {
        List<TagLibraryIssue> issues = TagLibraryValidator.validate(currentProject);
        tagLibraryIssuesLabel.setText(DesktopTagLibraryIssueFormatter.labelText(issues));
        tagLibraryIssuesLabel.setToolTipText(DesktopTagLibraryIssueFormatter.tooltipText(issues));
    }

    private void updateTagProductivityLabels() {
        favoriteTagCountLabel.setText(DesktopWritingProductivityFormatter.favoriteCountLabel(favoriteTagIds.size()));
        recentTagCountLabel.setText(DesktopWritingProductivityFormatter.recentCountLabel(recentTagIds.size()));
    }

    private String sanitizeTagId(String value) {
        return NarrativeTagIdPolicy.normalizeExplicitId(value);
    }

    private String ensureUniqueTagId(String baseId) {
        String candidate = baseId;
        int suffix = 2;
        while (hasTagId(candidate)) {
            candidate = candidate.substring(0, 4) + suffix;
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

    private String nonBlankOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private void toggleSelectedTagFavorite() {
        tagWorkflow.toggleSelectedTagFavorite();
    }

    private void registerRecentTag(String tagId) {
        if (tagId == null || tagId.isBlank()) {
            return;
        }
        recentTagIds.remove(tagId);
        recentTagIds.add(0, tagId);
        while (recentTagIds.size() > 8) {
            recentTagIds.remove(recentTagIds.size() - 1);
        }
        updateTagProductivityLabels();
    }

    private void showTagSuggestionPopup(boolean forceShow) {
        tagAutocompleteController.showTagSuggestionPopup(forceShow);
    }

    private void hideTagSuggestionPopup() {
        tagAutocompleteController.hideTagSuggestionPopup();
    }

    private int recentRank(String tagId) {
        int index = recentTagIds.indexOf(tagId);
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private List<NarrativeTag> sortedTagsForProductivity(List<NarrativeTag> source, String normalizedQuery) {
        List<NarrativeTag> sortedTags = new ArrayList<>(source);
        sortedTags.sort((left, right) -> {
            int favoriteCompare = Boolean.compare(favoriteTagIds.contains(right.id()), favoriteTagIds.contains(left.id()));
            if (favoriteCompare != 0) {
                return favoriteCompare;
            }
            int recentCompare = Integer.compare(recentRank(left.id()), recentRank(right.id()));
            if (recentCompare != 0) {
                return recentCompare;
            }
            boolean leftPrefix = left.id().startsWith(normalizedQuery);
            boolean rightPrefix = right.id().startsWith(normalizedQuery);
            int prefixCompare = Boolean.compare(rightPrefix, leftPrefix);
            if (prefixCompare != 0) {
                return prefixCompare;
            }
            return left.id().compareTo(right.id());
        });
        return sortedTags;
    }


    private void scheduleAutosave() {
        currentPath = resolveSavePath(currentPath);
        Path autosavePath = currentPath;
        autosaveService.schedule(currentProject, autosavePath, () ->
                        SwingUtilities.invokeLater(() -> statusLabel.setText("Alteracoes automaticas salvas em " + autosavePath)),
                exception -> SwingUtilities.invokeLater(() -> backgroundCoordinator.handleAutosaveFailure(autosavePath, exception))
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

    private void focusAnalysisFrame() {
        selectTab("Analise");
        analysisPanel.requestFocusInWindow();
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
