package io.storyflame.desktop;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;

final class DesktopEditorStructureCoordinator {
    interface Host {
        Project currentProject();
        Chapter selectedChapter();
        void setSelectedChapter(Chapter chapter);
        Scene selectedScene();
        void setSelectedScene(Scene scene);
        boolean isSyncingUi();
        void setSyncingUi(boolean syncingUi);
        void syncProjectFromFields();
        void ensureChapterHasScene(Chapter chapter);
        String displayTitle(String value, String fallbackPrefix);
        void renderSummary();
        void refreshPointOfViewList();
        void updateWordCount();
        void updateTagCountLabel();
        void hideTagSuggestionPopup();
        String projectPathText();
        String currentStatusText();
        void setStatusText(String text);
    }

    private final Host host;
    private final DefaultListModel<String> chapterListModel;
    private final DefaultListModel<String> sceneListModel;
    private final JList<String> chapterList;
    private final JList<String> sceneList;
    private final JTree editorStructureTree;
    private final JTextField chapterTitleField;
    private final JTextField sceneTitleField;
    private final JTextArea sceneSynopsisArea;
    private final JTextArea sceneEditorArea;
    private final JLabel chapterCountLabel;
    private final JLabel sceneCountLabel;
    private final JLabel contextLabel;
    private final JLabel projectPathLabel;
    private final Timer searchRefreshTimer;
    private final UndoManager sceneUndoManager;

    DesktopEditorStructureCoordinator(
            Host host,
            DefaultListModel<String> chapterListModel,
            DefaultListModel<String> sceneListModel,
            JList<String> chapterList,
            JList<String> sceneList,
            JTree editorStructureTree,
            JTextField chapterTitleField,
            JTextField sceneTitleField,
            JTextArea sceneSynopsisArea,
            JTextArea sceneEditorArea,
            JLabel chapterCountLabel,
            JLabel sceneCountLabel,
            JLabel contextLabel,
            JLabel projectPathLabel,
            Timer searchRefreshTimer,
            UndoManager sceneUndoManager
    ) {
        this.host = host;
        this.chapterListModel = chapterListModel;
        this.sceneListModel = sceneListModel;
        this.chapterList = chapterList;
        this.sceneList = sceneList;
        this.editorStructureTree = editorStructureTree;
        this.chapterTitleField = chapterTitleField;
        this.sceneTitleField = sceneTitleField;
        this.sceneSynopsisArea = sceneSynopsisArea;
        this.sceneEditorArea = sceneEditorArea;
        this.chapterCountLabel = chapterCountLabel;
        this.sceneCountLabel = sceneCountLabel;
        this.contextLabel = contextLabel;
        this.projectPathLabel = projectPathLabel;
        this.searchRefreshTimer = searchRefreshTimer;
        this.sceneUndoManager = sceneUndoManager;
    }

    void refreshStructureLists() {
        boolean previousSyncingUi = host.isSyncingUi();
        host.setSyncingUi(true);
        Project currentProject = host.currentProject();
        Chapter selectedChapter = host.selectedChapter();
        Scene selectedScene = host.selectedScene();
        int selectedChapterIndex = currentProject == null || selectedChapter == null
                ? -1
                : currentProject.getChapters().indexOf(selectedChapter);
        int selectedSceneIndex = selectedChapter == null || selectedScene == null
                ? -1
                : selectedChapter.getScenes().indexOf(selectedScene);

        chapterListModel.clear();
        sceneListModel.clear();
        refreshEditorStructureTree();
        if (currentProject == null) {
            chapterTitleField.setText("");
            chapterCountLabel.setText("0 capitulos");
            sceneCountLabel.setText("0 cenas");
            host.setSyncingUi(previousSyncingUi);
            return;
        }

        chapterCountLabel.setText(currentProject.getChapters().size() + " capitulos");
        for (int index = 0; index < currentProject.getChapters().size(); index++) {
            Chapter chapter = currentProject.getChapters().get(index);
            chapterListModel.addElement(DesktopOutlineFormatter.chapterLabel(index, chapter, "Capitulo"));
        }
        if (selectedChapter != null) {
            chapterTitleField.setText(selectedChapter.getTitle());
            sceneCountLabel.setText(selectedChapter.getScenes().size() + " cenas");
            for (int index = 0; index < selectedChapter.getScenes().size(); index++) {
                Scene scene = selectedChapter.getScenes().get(index);
                sceneListModel.addElement(DesktopOutlineFormatter.sceneLabel(
                        index,
                        scene,
                        "Cena",
                        pointOfViewName(scene)
                ));
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
        selectEditorTreeCurrentScene();
        host.setSyncingUi(previousSyncingUi);
    }

    void selectCurrentObjects() {
        boolean previousSyncingUi = host.isSyncingUi();
        host.setSyncingUi(true);
        Project currentProject = host.currentProject();
        Chapter selectedChapter = host.selectedChapter();
        Scene selectedScene = host.selectedScene();
        int chapterIndex = currentProject == null ? -1 : currentProject.getChapters().indexOf(selectedChapter);
        chapterList.setSelectedIndex(chapterIndex);
        if (selectedChapter != null) {
            int sceneIndex = selectedChapter.getScenes().indexOf(selectedScene);
            sceneList.setSelectedIndex(sceneIndex);
        } else {
            sceneList.clearSelection();
        }
        selectEditorTreeCurrentScene();
        updateEditorFields();
        host.setSyncingUi(previousSyncingUi);
    }

    void onEditorTreeSelected() {
        if (host.isSyncingUi() || host.currentProject() == null) {
            return;
        }
        Object selectedPathComponent = editorStructureTree.getLastSelectedPathComponent();
        if (!(selectedPathComponent instanceof DefaultMutableTreeNode node)) {
            return;
        }
        EditorTreeNode selectedNode = treeNodeValue(node);
        if (selectedNode == null) {
            return;
        }
        host.syncProjectFromFields();
        host.setSelectedChapter(selectedNode.chapter());
        host.ensureChapterHasScene(host.selectedChapter());
        host.setSelectedScene(selectedNode.scene() == null ? host.selectedChapter().getScenes().get(0) : selectedNode.scene());
        refreshStructureLists();
        selectCurrentObjects();
    }

    void onChapterSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || host.isSyncingUi() || host.currentProject() == null) {
            return;
        }
        host.syncProjectFromFields();
        int selectedIndex = chapterList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= host.currentProject().getChapters().size()) {
            return;
        }
        Chapter selectedChapter = host.currentProject().getChapters().get(selectedIndex);
        host.setSelectedChapter(selectedChapter);
        host.ensureChapterHasScene(selectedChapter);
        host.setSelectedScene(selectedChapter.getScenes().get(0));
        refreshStructureLists();
        sceneList.setSelectedIndex(0);
        updateEditorFields();
    }

    void onSceneSelected(ListSelectionEvent event) {
        Chapter selectedChapter = host.selectedChapter();
        if (event.getValueIsAdjusting() || host.isSyncingUi() || selectedChapter == null) {
            return;
        }
        host.syncProjectFromFields();
        int selectedIndex = sceneList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= selectedChapter.getScenes().size()) {
            return;
        }
        host.setSelectedScene(selectedChapter.getScenes().get(selectedIndex));
        updateEditorFields();
    }

    void updateEditorFields() {
        boolean previousSyncingUi = host.isSyncingUi();
        host.setSyncingUi(true);
        searchRefreshTimer.stop();
        host.hideTagSuggestionPopup();
        Scene selectedScene = host.selectedScene();
        Chapter selectedChapter = host.selectedChapter();
        boolean hasScene = selectedScene != null;
        sceneTitleField.setText(hasScene ? selectedScene.getTitle() : "");
        sceneSynopsisArea.setText(hasScene ? selectedScene.getSynopsis() : "");
        sceneEditorArea.setText(hasScene ? selectedScene.getContent() : DesktopEditorStateFormatter.emptyEditorMessage());
        sceneTitleField.setEditable(hasScene);
        sceneSynopsisArea.setEditable(hasScene);
        sceneEditorArea.setEditable(hasScene);
        contextLabel.setText(DesktopEditorStateFormatter.contextLabel(selectedChapter, selectedScene));
        projectPathLabel.setText(host.projectPathText());
        sceneUndoManager.discardAllEdits();
        host.updateWordCount();
        host.updateTagCountLabel();
        host.refreshPointOfViewList();
        host.renderSummary();
        host.setStatusText(hasScene ? host.currentStatusText() : "Selecione uma cena para comecar a escrever.");
        host.setSyncingUi(previousSyncingUi);
    }

    private void refreshEditorStructureTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Livro");
        Project currentProject = host.currentProject();
        if (currentProject != null) {
            for (int chapterIndex = 0; chapterIndex < currentProject.getChapters().size(); chapterIndex++) {
                Chapter chapter = currentProject.getChapters().get(chapterIndex);
                DefaultMutableTreeNode chapterNode = new DefaultMutableTreeNode(new EditorTreeNode(
                        DesktopOutlineFormatter.chapterLabel(chapterIndex, chapter, "Capitulo"),
                        chapter,
                        null
                ));
                for (int sceneIndex = 0; sceneIndex < chapter.getScenes().size(); sceneIndex++) {
                    Scene scene = chapter.getScenes().get(sceneIndex);
                    chapterNode.add(new DefaultMutableTreeNode(new EditorTreeNode(
                            DesktopOutlineFormatter.sceneLabel(
                                    sceneIndex,
                                    scene,
                                    "Cena",
                                    pointOfViewName(scene)
                            ),
                            chapter,
                            scene
                    )));
                }
                root.add(chapterNode);
            }
        }
        editorStructureTree.setModel(new DefaultTreeModel(root));
        for (int index = 0; index < editorStructureTree.getRowCount(); index++) {
            editorStructureTree.expandRow(index);
        }
    }

    private void selectEditorTreeCurrentScene() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) editorStructureTree.getModel().getRoot();
        Chapter selectedChapter = host.selectedChapter();
        Scene selectedScene = host.selectedScene();
        if (root == null || selectedChapter == null) {
            editorStructureTree.clearSelection();
            return;
        }
        for (int chapterIndex = 0; chapterIndex < root.getChildCount(); chapterIndex++) {
            DefaultMutableTreeNode chapterNode = (DefaultMutableTreeNode) root.getChildAt(chapterIndex);
            EditorTreeNode chapterValue = treeNodeValue(chapterNode);
            if (chapterValue == null || chapterValue.chapter() != selectedChapter) {
                continue;
            }
            if (selectedScene == null) {
                editorStructureTree.setSelectionPath(new TreePath(chapterNode.getPath()));
                return;
            }
            for (int sceneIndex = 0; sceneIndex < chapterNode.getChildCount(); sceneIndex++) {
                DefaultMutableTreeNode sceneNode = (DefaultMutableTreeNode) chapterNode.getChildAt(sceneIndex);
                EditorTreeNode sceneValue = treeNodeValue(sceneNode);
                if (sceneValue != null && sceneValue.scene() == selectedScene) {
                    editorStructureTree.setSelectionPath(new TreePath(sceneNode.getPath()));
                    return;
                }
            }
            editorStructureTree.setSelectionPath(new TreePath(chapterNode.getPath()));
            return;
        }
        editorStructureTree.clearSelection();
    }

    private EditorTreeNode treeNodeValue(DefaultMutableTreeNode node) {
        Object value = node == null ? null : node.getUserObject();
        return value instanceof EditorTreeNode editorTreeNode ? editorTreeNode : null;
    }

    private String pointOfViewName(Scene scene) {
        if (scene == null || host.currentProject() == null) {
            return "";
        }
        String pointOfViewCharacterId = scene.getPointOfViewCharacterId();
        if (pointOfViewCharacterId == null || pointOfViewCharacterId.isBlank()) {
            return "";
        }
        return host.currentProject().getCharacters().stream()
                .filter(character -> pointOfViewCharacterId.equals(character.getId()))
                .map(character -> host.displayTitle(character.getName(), "Personagem"))
                .findFirst()
                .orElse("");
    }

    private record EditorTreeNode(String label, Chapter chapter, Scene scene) {
        @Override
        public String toString() {
            return label;
        }
    }
}
