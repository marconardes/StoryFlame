package io.storyflame.desktop;

import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.NarrativeTag;
import io.storyflame.core.tags.ParsedNarrativeTag;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.text.BadLocationException;

final class DesktopEditorTagAutocomplete {
    interface Host {
        Project currentProject();
        Scene selectedScene();
        boolean isSyncingUi();
        List<NarrativeTag> sortedTagsForProductivity(List<NarrativeTag> source, String normalizedQuery);
        boolean isFavoriteTag(String tagId);
        boolean isRecentTag(String tagId);
        void registerRecentTag(String tagId);
        void setStatusText(String text);
    }

    private final Host host;
    private final JTextArea editor;
    private final JLabelState hoverTagPreviewLabel;
    private final JPopupMenu tagSuggestionPopup;
    private final JList<String> tagSuggestionList;
    private final DefaultListModel<String> tagSuggestionListModel;
    private final List<NarrativeTag> visibleTagSuggestions;
    private TagQuery activeTagQuery;

    interface JLabelState {
        void setText(String text);
    }

    DesktopEditorTagAutocomplete(
            Host host,
            JTextArea editor,
            JLabelState hoverTagPreviewLabel,
            JPopupMenu tagSuggestionPopup,
            JList<String> tagSuggestionList,
            DefaultListModel<String> tagSuggestionListModel,
            List<NarrativeTag> visibleTagSuggestions
    ) {
        this.host = host;
        this.editor = editor;
        this.hoverTagPreviewLabel = hoverTagPreviewLabel;
        this.tagSuggestionPopup = tagSuggestionPopup;
        this.tagSuggestionList = tagSuggestionList;
        this.tagSuggestionListModel = tagSuggestionListModel;
        this.visibleTagSuggestions = visibleTagSuggestions;
    }

    void install() {
        tagSuggestionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tagSuggestionList.setFocusable(false);
        tagSuggestionList.setRequestFocusEnabled(false);
        tagSuggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                int index = tagSuggestionList.locationToIndex(event.getPoint());
                if (index >= 0) {
                    tagSuggestionList.setSelectedIndex(index);
                    acceptSelectedTagSuggestion();
                }
            }
        });
        tagSuggestionPopup.add(new JScrollPane(tagSuggestionList));
        editor.setFocusTraversalKeysEnabled(false);

        editor.getInputMap().put(KeyStroke.getKeyStroke("control SPACE"), "tag-autocomplete");
        editor.getActionMap().put("tag-autocomplete", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                showTagSuggestionPopup(true);
            }
        });
        installAutocompleteEditorAction(KeyStroke.getKeyStroke("DOWN"), "storyflame-tag-down", this::selectNextTagSuggestion);
        installAutocompleteEditorAction(KeyStroke.getKeyStroke("UP"), "storyflame-tag-up", this::selectPreviousTagSuggestion);
        installAutocompleteEditorAction(KeyStroke.getKeyStroke("ENTER"), "storyflame-tag-accept-enter", this::acceptSelectedTagSuggestion);
        installAutocompleteEditorAction(KeyStroke.getKeyStroke("TAB"), "storyflame-tag-accept-tab", this::acceptSelectedTagSuggestion);
        installAutocompleteEditorAction(KeyStroke.getKeyStroke("ESCAPE"), "storyflame-tag-close", this::hideTagSuggestionPopup);
        editor.addCaretListener(event -> {
            updateHoverTagPreviewAtCaret();
            if (!host.isSyncingUi()) {
                showTagSuggestionPopup(false);
            }
        });
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent event) {
                if (!tagSuggestionPopup.isVisible()) {
                    if (event.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                        editor.replaceSelection("    ");
                        event.consume();
                    }
                    return;
                }
                if (event.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    selectNextTagSuggestion();
                    event.consume();
                } else if (event.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                    selectPreviousTagSuggestion();
                    event.consume();
                } else if (event.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER
                        || event.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                    acceptSelectedTagSuggestion();
                    event.consume();
                } else if (event.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    hideTagSuggestionPopup();
                    event.consume();
                }
            }
        });
        editor.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent event) {
                updateHoverTagPreview(event.getPoint());
            }
        });
        ToolTipManager.sharedInstance().registerComponent(editor);
    }

    void showTagSuggestionPopup(boolean forceShow) {
        TagQuery tagQuery = currentTagQuery();
        if (!forceShow && (tagQuery == null || !tagQuery.insideTag())) {
            hideTagSuggestionPopup();
            return;
        }
        if (!forceShow && tagQuery.query().trim().length() < 2) {
            hideTagSuggestionPopup();
            return;
        }
        activeTagQuery = tagQuery;
        refreshTagSuggestions(tagQuery == null ? "" : tagQuery.query(), forceShow);
        if (visibleTagSuggestions.isEmpty()) {
            hideTagSuggestionPopup();
            return;
        }
        try {
            Rectangle rectangle = editor.modelToView2D(editor.getCaretPosition()).getBounds();
            tagSuggestionPopup.show(editor, rectangle.x, rectangle.y + rectangle.height);
        } catch (BadLocationException ignored) {
            hideTagSuggestionPopup();
        }
    }

    void hideTagSuggestionPopup() {
        activeTagQuery = null;
        tagSuggestionPopup.setVisible(false);
    }

    void updateHoverTagPreview(Point point) {
        try {
            int offset = editor.viewToModel2D(point);
            String preview = tagPreviewAtOffset(offset);
            hoverTagPreviewLabel.setText(preview == null ? DesktopWritingProductivityFormatter.defaultTagHint() : preview);
            editor.setToolTipText(preview);
        } catch (Exception ignored) {
            hoverTagPreviewLabel.setText(DesktopWritingProductivityFormatter.defaultTagHint());
            editor.setToolTipText(null);
        }
    }

    void updateHoverTagPreviewAtCaret() {
        String preview = tagPreviewAtOffset(editor.getCaretPosition());
        hoverTagPreviewLabel.setText(preview == null ? DesktopWritingProductivityFormatter.defaultTagHint() : preview);
    }

    private void refreshTagSuggestions(String query, boolean forceShow) {
        visibleTagSuggestions.clear();
        tagSuggestionListModel.clear();
        Project currentProject = host.currentProject();
        if (currentProject == null) {
            return;
        }
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        for (NarrativeTag tag : host.sortedTagsForProductivity(currentProject.getNarrativeTags(), normalizedQuery)) {
            if (!forceShow && normalizedQuery.isBlank()) {
                continue;
            }
            String haystack = (tag.id() + "\n" + tag.label() + "\n" + tag.template()).toLowerCase();
            if (!normalizedQuery.isBlank() && !haystack.contains(normalizedQuery)) {
                continue;
            }
            visibleTagSuggestions.add(tag);
            tagSuggestionListModel.addElement(formatTagSuggestion(tag));
        }
        if (!visibleTagSuggestions.isEmpty()) {
            tagSuggestionList.setSelectedIndex(0);
        }
    }

    private void installAutocompleteEditorAction(KeyStroke keyStroke, String actionKey, Runnable popupAction) {
        Object previousActionKey = editor.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, actionKey);
        editor.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (tagSuggestionPopup.isVisible()) {
                    popupAction.run();
                    return;
                }
                if (previousActionKey != null) {
                    javax.swing.Action previousAction = editor.getActionMap().get(previousActionKey);
                    if (previousAction != null) {
                        previousAction.actionPerformed(event);
                    }
                }
            }
        });
    }

    private void selectNextTagSuggestion() {
        if (tagSuggestionListModel.isEmpty()) {
            return;
        }
        int nextIndex = Math.min(tagSuggestionListModel.size() - 1, tagSuggestionList.getSelectedIndex() + 1);
        tagSuggestionList.setSelectedIndex(Math.max(0, nextIndex));
        tagSuggestionList.ensureIndexIsVisible(tagSuggestionList.getSelectedIndex());
    }

    private void selectPreviousTagSuggestion() {
        if (tagSuggestionListModel.isEmpty()) {
            return;
        }
        int previousIndex = Math.max(0, tagSuggestionList.getSelectedIndex() - 1);
        tagSuggestionList.setSelectedIndex(previousIndex);
        tagSuggestionList.ensureIndexIsVisible(previousIndex);
    }

    private void acceptSelectedTagSuggestion() {
        int selectedIndex = tagSuggestionList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visibleTagSuggestions.size()) {
            hideTagSuggestionPopup();
            return;
        }
        NarrativeTag tag = visibleTagSuggestions.get(selectedIndex);
        TagQuery tagQuery = activeTagQuery;
        if (tagQuery == null) {
            hideTagSuggestionPopup();
            return;
        }
        String replacement = "{" + tag.id() + "}";
        int textLength = editor.getText().length();
        int startIndex = Math.max(0, Math.min(tagQuery.startIndex(), textLength));
        int endIndex = Math.max(startIndex, Math.min(textLength, tagQuery.startIndex() + 1 + tagQuery.query().length()));
        editor.select(startIndex, endIndex);
        editor.replaceSelection(replacement);
        host.registerRecentTag(tag.id());
        hideTagSuggestionPopup();
        host.setStatusText("Tag inserida: " + replacement);
        editor.requestFocusInWindow();
    }

    private String formatTagSuggestion(NarrativeTag tag) {
        String favoriteMarker = host.isFavoriteTag(tag.id()) ? "★ " : "";
        String recentMarker = host.isRecentTag(tag.id()) ? "• " : "";
        String preview = tag.template().isBlank() ? "-" : tag.template();
        return favoriteMarker + recentMarker + "{" + tag.id() + "} -> " + preview;
    }

    private TagQuery currentTagQuery() {
        if (host.selectedScene() == null) {
            return null;
        }
        String text = editor.getText();
        int caret = Math.max(0, Math.min(editor.getCaretPosition(), text.length()));
        int start = text.lastIndexOf('{', Math.max(0, caret - 1));
        if (start < 0) {
            return null;
        }
        if (start + 1 > caret || start + 1 > text.length()) {
            return null;
        }
        int close = text.indexOf('}', start);
        if (close >= 0 && close < caret) {
            return null;
        }
        String query = text.substring(start + 1, caret);
        if (query.contains(" ") || query.contains("\n") || query.contains("\t")) {
            return null;
        }
        return new TagQuery(start, query, true);
    }

    private String tagPreviewAtOffset(int offset) {
        Project currentProject = host.currentProject();
        Scene selectedScene = host.selectedScene();
        if (currentProject == null || selectedScene == null) {
            return null;
        }
        for (ParsedNarrativeTag tag : DesktopProjectInsights.currentSceneTags(currentProject, selectedScene)) {
            if (offset >= tag.startIndex() && offset <= tag.endIndex()) {
                NarrativeTag resolvedTag = DesktopProjectInsights.currentNarrativeTagCatalog(currentProject).resolve(tag.tagId());
                if (resolvedTag == null) {
                    return "Tag invalida: {" + tag.tagId() + "}";
                }
                return "{" + resolvedTag.id() + "} -> " + (resolvedTag.template().isBlank() ? "-" : resolvedTag.template());
            }
        }
        return null;
    }

    private record TagQuery(int startIndex, String query, boolean insideTag) {
    }
}
