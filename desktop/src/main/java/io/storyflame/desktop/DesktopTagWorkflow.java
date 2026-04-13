package io.storyflame.desktop;

import io.storyflame.app.project.ProjectTagApplicationService;
import io.storyflame.core.model.Project;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.CharacterTagProfileSynchronizer;
import io.storyflame.core.tags.NarrativeTag;
import io.storyflame.core.tags.NarrativeTagIdPolicy;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

final class DesktopTagWorkflow {
    interface Host {
        boolean isSyncingUi();
        boolean hasCurrentPath();
        Project currentProject();
        NarrativeTag selectedTag();
        void setSelectedTag(NarrativeTag tag);
        NarrativeTag selectedTagBeforeDraft();
        void setSelectedTagBeforeDraft(NarrativeTag tag);
        boolean isTagDraftMode();
        void setTagDraftMode(boolean value);
        CharacterTagProfile selectedProfile();
        Set<String> favoriteTagIds();
        DefaultListModel<String> tagListModel();
        List<NarrativeTag> visibleTags();
        JList<String> tagList();
        void refreshTagLibrary();
        void refreshTagProfiles();
        void renderSummary();
        void scheduleAutosave();
        void setStatusText(String text);
        void updateTagLibraryIssuesLabel();
        void updateTagProductivityLabels();
        JFrame frame();
        String displayTitle(String value, String fallbackPrefix);
        String sanitizeTagId(String value);
        String ensureUniqueTagId(String baseId);
    }

    private final Host host;
    private final ProjectTagApplicationService projectTagApplicationService;
    private final JTextField tagSearchField;
    private final JTextField tagIdField;
    private final JTextField tagLabelField;
    private final JTextField tagTemplateField;
    private final JLabelState selectedTagUsageLabel;
    private final JLabelState selectedTagStatusLabel;
    private final JLabelState tagDetailModeLabel;

    interface JLabelState {
        void setText(String text);
    }

    DesktopTagWorkflow(
            Host host,
            ProjectTagApplicationService projectTagApplicationService,
            JTextField tagSearchField,
            JTextField tagIdField,
            JTextField tagLabelField,
            JTextField tagTemplateField,
            JLabelState selectedTagUsageLabel,
            JLabelState selectedTagStatusLabel,
            JLabelState tagDetailModeLabel
    ) {
        this.host = host;
        this.projectTagApplicationService = projectTagApplicationService;
        this.tagSearchField = tagSearchField;
        this.tagIdField = tagIdField;
        this.tagLabelField = tagLabelField;
        this.tagTemplateField = tagTemplateField;
        this.selectedTagUsageLabel = selectedTagUsageLabel;
        this.selectedTagStatusLabel = selectedTagStatusLabel;
        this.tagDetailModeLabel = tagDetailModeLabel;
    }

    void onTagEdited() {
        if (host.isSyncingUi() || host.currentProject() == null || !host.hasCurrentPath()) {
            return;
        }
        NarrativeTag selectedTag = host.selectedTag();
        if (selectedTag == null) {
            if (host.isTagDraftMode()) {
                host.setStatusText("Preencha rotulo e texto renderizado. Depois clique em Salvar tag.");
            }
            return;
        }
        String updatedId = resolveTagIdForSave(selectedTag.id());
        if (updatedId.isBlank()) {
            host.setStatusText("Preencha o id da tag no formato 4 letras + numero.");
            return;
        }
        if (!NarrativeTagIdPolicy.isValid(updatedId)) {
            host.setStatusText("Use id no formato 4 letras + numero, como falc1.");
            return;
        }
        if (!updatedId.equals(selectedTag.id()) && tagIdExists(updatedId)) {
            host.setStatusText("Ja existe uma tag com esse id.");
            return;
        }
        NarrativeTag updatedTag = projectTagApplicationService.updateTag(
                host.currentProject(),
                selectedTag,
                updatedId,
                tagLabelField.getText(),
                tagTemplateField.getText()
        );
        int visibleIndex = host.visibleTags().indexOf(selectedTag);
        host.setSelectedTag(updatedTag);
        if (visibleIndex >= 0 && visibleIndex < host.tagListModel().size()) {
            host.tagListModel().set(visibleIndex, updatedTag.id() + " | " + host.displayTitle(updatedTag.label(), "Sem rotulo"));
            host.visibleTags().set(visibleIndex, updatedTag);
        }
        tagDetailModeLabel.setText(updatedTag.label().isBlank() && updatedTag.template().isBlank()
                ? "Nova tag"
                : "Editando tag");
        selectedTagStatusLabel.setText(DesktopDraftStateFormatter.tagStatus(
                true,
                updatedTag.label(),
                updatedTag.template(),
                CharacterTagProfileSynchronizer.isCharacterOwnedTag(updatedTag)
        ));
        host.updateTagLibraryIssuesLabel();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("Tag atualizada. Revise e conclua quando estiver pronta.");
    }

    void saveTag() {
        NarrativeTag selectedTag = host.selectedTag();
        if (host.currentProject() == null || selectedTag == null) {
            if (!host.isTagDraftMode()) {
                host.setStatusText("Abra um rascunho de tag para preencher.");
                focusTagDraft();
                return;
            }
        }
        String label = tagLabelField.getText();
        String template = tagTemplateField.getText();
        String explicitTagId = resolveTagIdForSave(host.selectedTag() == null ? "" : host.selectedTag().id());
        if (explicitTagId.isBlank()) {
            host.setStatusText("Preencha o id da tag no formato 4 letras + numero.");
            focusTagId();
            return;
        }
        if (!NarrativeTagIdPolicy.isValid(explicitTagId)) {
            host.setStatusText("Use id no formato 4 letras + numero, como falc1.");
            focusTagId();
            return;
        }
        if (label.isBlank()) {
            host.setStatusText("Preencha o rotulo da tag antes de salvar.");
            focusTagDraft();
            return;
        }
        if (template.isBlank()) {
            host.setStatusText("Preencha o texto renderizado antes de salvar a tag.");
            focusTagTemplate();
            return;
        }
        if (host.isTagDraftMode()) {
            String uniqueId = host.ensureUniqueTagId(explicitTagId);
            if (!uniqueId.equals(explicitTagId)) {
                host.setStatusText("Ja existe uma tag com esse id.");
                focusTagId();
                return;
            }
            NarrativeTag newTag = projectTagApplicationService.createTag(host.currentProject(), uniqueId, label, template);
            host.setSelectedTag(newTag);
            host.setTagDraftMode(false);
            host.setSelectedTagBeforeDraft(null);
            host.refreshTagLibrary();
            host.renderSummary();
            host.scheduleAutosave();
            host.refreshTagLibrary();
            host.setStatusText("Tag criada.");
            return;
        }
        onTagEdited();
        host.setStatusText("Tag salva.");
    }

    void onProfileEdited() {
        if (host.isSyncingUi() || host.currentProject() == null || !host.hasCurrentPath() || host.selectedProfile() == null) {
            return;
        }
        host.refreshTagProfiles();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("Alteracoes pendentes...");
    }

    void addTag() {
        if (host.currentProject() == null) {
            return;
        }
        host.setSelectedTagBeforeDraft(host.selectedTag());
        host.setTagDraftMode(true);
        tagSearchField.setText("");
        tagIdField.setText("");
        tagLabelField.setText("");
        tagTemplateField.setText("");
        host.refreshTagLibrary();
        focusTagDraft();
        host.setStatusText("Nova tag. Preencha rotulo e texto renderizado.");
    }

    void deleteTag() {
        if (host.currentProject() == null || host.selectedTag() == null) {
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(
                host.frame(),
                "Excluir tag '" + host.selectedTag().id() + "'?",
                "Excluir tag",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        host.setSelectedTag(projectTagApplicationService.deleteTag(host.currentProject(), host.selectedTag()));
        host.refreshTagLibrary();
        host.refreshTagProfiles();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("Tag removida.");
    }

    void duplicateTag() {
        if (host.currentProject() == null || host.selectedTag() == null) {
            return;
        }
        NarrativeTag selectedTag = host.selectedTag();
        String newId = host.ensureUniqueTagId(selectedTag.id() + "-copy");
        NarrativeTag copy = projectTagApplicationService.duplicateTag(host.currentProject(), selectedTag, newId);
        host.setSelectedTag(copy);
        host.refreshTagLibrary();
        host.scheduleAutosave();
        host.setStatusText("Tag duplicada.");
    }

    void clearSelectedTagDraft() {
        host.setTagDraftMode(true);
        host.setSelectedTagBeforeDraft(host.selectedTag());
        tagIdField.setText("");
        tagLabelField.setText("");
        tagTemplateField.setText("");
        host.tagList().clearSelection();
        selectedTagUsageLabel.setText("0 usos no manuscrito");
        selectedTagStatusLabel.setText(DesktopDraftStateFormatter.tagStatus(false, "", "", false));
        tagDetailModeLabel.setText("Nova tag");
        focusTagDraft();
        host.setStatusText("Nova tag. Preencha rotulo e texto renderizado.");
    }

    void cancelTagDraft() {
        if (!host.isTagDraftMode()) {
            return;
        }
        host.setTagDraftMode(false);
        host.setSelectedTag(host.selectedTagBeforeDraft());
        host.setSelectedTagBeforeDraft(null);
        host.refreshTagLibrary();
        host.setStatusText("Criacao de tag cancelada.");
    }

    void appendSelectedTagToProfile() {
        if (host.selectedProfile() == null || host.selectedTag() == null) {
            return;
        }
        projectTagApplicationService.addTagToProfile(host.currentProject(), host.selectedProfile(), host.selectedTag());
        host.refreshTagProfiles();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("Tag adicionada ao perfil.");
    }

    void removeSelectedTagFromProfile() {
        if (host.selectedProfile() == null || host.selectedTag() == null) {
            return;
        }
        projectTagApplicationService.removeTagFromProfile(host.currentProject(), host.selectedProfile(), host.selectedTag());
        host.refreshTagProfiles();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("Tag removida do perfil.");
    }

    void toggleSelectedTagFavorite() {
        NarrativeTag selectedTag = host.selectedTag();
        if (selectedTag == null) {
            return;
        }
        if (host.favoriteTagIds().contains(selectedTag.id())) {
            host.favoriteTagIds().remove(selectedTag.id());
            host.setStatusText("Tag removida das favoritas.");
        } else {
            host.favoriteTagIds().add(selectedTag.id());
            host.setStatusText("Tag adicionada as favoritas.");
        }
        host.updateTagProductivityLabels();
        host.refreshTagLibrary();
    }

    private void focusTagDraft() {
        SwingUtilities.invokeLater(() -> {
            tagIdField.requestFocusInWindow();
            tagIdField.selectAll();
        });
    }

    private void focusTagTemplate() {
        SwingUtilities.invokeLater(() -> {
            tagTemplateField.requestFocusInWindow();
            tagTemplateField.selectAll();
        });
    }

    private void focusTagId() {
        SwingUtilities.invokeLater(() -> {
            tagIdField.requestFocusInWindow();
            tagIdField.selectAll();
        });
    }

    private String resolveTagIdForSave(String currentId) {
        String rawValue = tagIdField.getText();
        if ((rawValue == null || rawValue.isBlank()) && currentId != null && !currentId.isBlank()) {
            return currentId;
        }
        return NarrativeTagIdPolicy.normalizeExplicitId(rawValue);
    }

    private boolean tagIdExists(String candidate) {
        return host.currentProject().getNarrativeTags().stream()
                .anyMatch(tag -> tag.id().equals(candidate));
    }

}
