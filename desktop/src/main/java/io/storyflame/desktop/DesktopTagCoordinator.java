package io.storyflame.desktop;

import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.CharacterTagProfileSynchronizer;
import io.storyflame.core.tags.NarrativeTag;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

final class DesktopTagCoordinator {
    interface Host {
        Project currentProject();
        Scene selectedScene();
        Character selectedCharacter();
        void setSelectedCharacter(Character character);
        NarrativeTag selectedTag();
        void setSelectedTag(NarrativeTag tag);
        NarrativeTag selectedTagBeforeDraft();
        void setSelectedTagBeforeDraft(NarrativeTag tag);
        CharacterTagProfile selectedProfile();
        void setSelectedProfile(CharacterTagProfile profile);
        boolean isTagDraftMode();
        void setTagDraftMode(boolean value);
        boolean isSyncingUi();
        void setSyncingUi(boolean syncingUi);
        String displayTitle(String value, String fallbackPrefix);
        List<NarrativeTag> sortedTagsForProductivity(List<NarrativeTag> source, String normalizedQuery);
        void updateTagLibraryIssuesLabel();
        void refreshCharacterLists();
    }

    private final Host host;
    private final List<NarrativeTag> visibleTags;
    private final List<CharacterTagProfile> visibleProfiles;
    private final DefaultListModel<String> tagListModel;
    private final DefaultListModel<String> profileListModel;
    private final JList<String> tagList;
    private final JList<String> profileList;
    private final JTextField tagSearchField;
    private final JTextField tagIdField;
    private final JTextField tagLabelField;
    private final JTextField tagTemplateField;
    private final JTextField profilePrefixField;
    private final JTextField profilePreferredTagsField;
    private final JLabel tagLibraryIssuesLabel;
    private final JLabel selectedTagUsageLabel;
    private final JLabel selectedTagStatusLabel;
    private final JLabel tagDetailModeLabel;
    private final JLabel tagDraftHintLabel;
    private final JLabel selectedProfileCharacterLabel;
    private final JLabel selectedProfileStatusLabel;
    private final JLabel selectedCharacterTagsLabel;

    DesktopTagCoordinator(
            Host host,
            List<NarrativeTag> visibleTags,
            List<CharacterTagProfile> visibleProfiles,
            DefaultListModel<String> tagListModel,
            DefaultListModel<String> profileListModel,
            JList<String> tagList,
            JList<String> profileList,
            JTextField tagSearchField,
            JTextField tagIdField,
            JTextField tagLabelField,
            JTextField tagTemplateField,
            JTextField profilePrefixField,
            JTextField profilePreferredTagsField,
            JLabel tagLibraryIssuesLabel,
            JLabel selectedTagUsageLabel,
            JLabel selectedTagStatusLabel,
            JLabel tagDetailModeLabel,
            JLabel tagDraftHintLabel,
            JLabel selectedProfileCharacterLabel,
            JLabel selectedProfileStatusLabel,
            JLabel selectedCharacterTagsLabel
    ) {
        this.host = host;
        this.visibleTags = visibleTags;
        this.visibleProfiles = visibleProfiles;
        this.tagListModel = tagListModel;
        this.profileListModel = profileListModel;
        this.tagList = tagList;
        this.profileList = profileList;
        this.tagSearchField = tagSearchField;
        this.tagIdField = tagIdField;
        this.tagLabelField = tagLabelField;
        this.tagTemplateField = tagTemplateField;
        this.profilePrefixField = profilePrefixField;
        this.profilePreferredTagsField = profilePreferredTagsField;
        this.tagLibraryIssuesLabel = tagLibraryIssuesLabel;
        this.selectedTagUsageLabel = selectedTagUsageLabel;
        this.selectedTagStatusLabel = selectedTagStatusLabel;
        this.tagDetailModeLabel = tagDetailModeLabel;
        this.tagDraftHintLabel = tagDraftHintLabel;
        this.selectedProfileCharacterLabel = selectedProfileCharacterLabel;
        this.selectedProfileStatusLabel = selectedProfileStatusLabel;
        this.selectedCharacterTagsLabel = selectedCharacterTagsLabel;
    }

    void refreshTagLibrary() {
        boolean previousSyncingUi = host.isSyncingUi();
        host.setSyncingUi(true);
        NarrativeTag previousSelection = host.selectedTag();

        visibleTags.clear();
        tagListModel.clear();
        if (host.currentProject() == null) {
            tagIdField.setText("");
            tagLabelField.setText("");
            tagTemplateField.setText("");
            tagLibraryIssuesLabel.setText("0 inconsistencias de tags");
            selectedTagUsageLabel.setText("0 usos no manuscrito");
            selectedTagStatusLabel.setText(DesktopDraftStateFormatter.tagStatus(false, "", "", false));
            tagDetailModeLabel.setText("Crie uma tag para comecar a editar");
            tagDraftHintLabel.setText(DesktopDraftStateFormatter.tagDraftHint(false, "", "", ""));
            host.setSyncingUi(previousSyncingUi);
            return;
        }

        String query = tagSearchField.getText() == null ? "" : tagSearchField.getText().trim().toLowerCase();
        for (NarrativeTag tag : host.sortedTagsForProductivity(host.currentProject().getNarrativeTags(), query)) {
            String haystack = (tag.id() + "\n" + tag.label() + "\n" + tag.description() + "\n" + tag.template()).toLowerCase();
            if (!query.isBlank() && !haystack.contains(query)) {
                continue;
            }
            visibleTags.add(tag);
            tagListModel.addElement(tag.id() + " | " + host.displayTitle(tag.label(), "Sem rotulo"));
        }

        if (host.isTagDraftMode()) {
            tagList.clearSelection();
        } else if (previousSelection != null && visibleTags.contains(previousSelection)) {
            host.setSelectedTag(previousSelection);
            tagList.setSelectedIndex(visibleTags.indexOf(previousSelection));
        } else if (!visibleTags.isEmpty()) {
            host.setSelectedTag(visibleTags.get(0));
            tagList.setSelectedIndex(0);
        } else if (!host.currentProject().getNarrativeTags().contains(host.selectedTag())) {
            host.setSelectedTag(host.currentProject().getNarrativeTags().isEmpty() ? null : host.currentProject().getNarrativeTags().get(0));
        }

        NarrativeTag selectedTag = host.isTagDraftMode() ? null : host.selectedTag();
        String tagId = selectedTag == null ? "" : selectedTag.id();
        String tagLabel = selectedTag == null ? "" : selectedTag.label();
        String tagTemplate = selectedTag == null ? "" : selectedTag.template();
        if (!host.isTagDraftMode() && !tagIdField.isFocusOwner() && !tagId.equals(tagIdField.getText())) {
            tagIdField.setText(tagId);
        }
        if (!host.isTagDraftMode() && !tagLabelField.isFocusOwner() && !tagLabel.equals(tagLabelField.getText())) {
            tagLabelField.setText(tagLabel);
        }
        if (!host.isTagDraftMode() && !tagTemplateField.isFocusOwner() && !tagTemplate.equals(tagTemplateField.getText())) {
            tagTemplateField.setText(tagTemplate);
        }
        tagDetailModeLabel.setText(host.isTagDraftMode() || selectedTag == null ? "Nova tag" : "Editando tag");
        tagDraftHintLabel.setText(DesktopDraftStateFormatter.tagDraftHint(
                !host.isTagDraftMode() && selectedTag != null,
                host.isTagDraftMode() ? tagIdField.getText() : tagId,
                host.isTagDraftMode() ? tagLabelField.getText() : tagLabel,
                host.isTagDraftMode() ? tagTemplateField.getText() : tagTemplate
        ));
        selectedTagUsageLabel.setText(selectedTag == null
                ? "0 usos no manuscrito"
                : DesktopProjectInsights.countTagUsage(host.currentProject(), selectedTag.id()) + " usos no manuscrito");
        selectedTagStatusLabel.setText(DesktopDraftStateFormatter.tagStatus(
                selectedTag != null,
                tagLabel,
                tagTemplate,
                selectedTag != null && CharacterTagProfileSynchronizer.isCharacterOwnedTag(selectedTag)
        ));
        host.updateTagLibraryIssuesLabel();
        host.setSyncingUi(previousSyncingUi);
    }

    void refreshTagProfiles() {
        boolean previousSyncingUi = host.isSyncingUi();
        host.setSyncingUi(true);
        CharacterTagProfileSynchronizer.synchronize(host.currentProject());

        visibleProfiles.clear();
        profileListModel.clear();
        if (host.currentProject() == null) {
            profilePrefixField.setText("");
            profilePreferredTagsField.setText("");
            selectedProfileCharacterLabel.setText("Nenhum perfil selecionado");
            selectedProfileStatusLabel.setText("Sem inconsistencias");
            host.setSyncingUi(previousSyncingUi);
            return;
        }

        for (CharacterTagProfile profile : host.currentProject().getCharacterTagProfiles()) {
            visibleProfiles.add(profile);
            profileListModel.addElement(DesktopProjectInsights.formatProfileLabel(host.currentProject(), host.selectedScene(), profile));
        }

        if (host.selectedProfile() != null && visibleProfiles.contains(host.selectedProfile())) {
            profileList.setSelectedIndex(visibleProfiles.indexOf(host.selectedProfile()));
        } else if (!visibleProfiles.isEmpty()) {
            host.setSelectedProfile(visibleProfiles.get(0));
            profileList.setSelectedIndex(0);
        } else {
            host.setSelectedProfile(null);
        }

        CharacterTagProfile selectedProfile = host.selectedProfile();
        String profilePrefix = selectedProfile == null ? "" : selectedProfile.getPrefix();
        String preferredTags = selectedProfile == null ? "" : String.join(", ", selectedProfile.getPreferredTagIds());
        if (!profilePrefixField.isFocusOwner() && !profilePrefix.equals(profilePrefixField.getText())) {
            profilePrefixField.setText(profilePrefix);
        }
        if (!profilePreferredTagsField.isFocusOwner() && !preferredTags.equals(profilePreferredTagsField.getText())) {
            profilePreferredTagsField.setText(preferredTags);
        }
        selectedProfileCharacterLabel.setText(selectedProfile == null
                ? "Nenhum perfil selecionado"
                : DesktopProjectInsights.formatProfileLabel(host.currentProject(), host.selectedScene(), selectedProfile));
        selectedProfileStatusLabel.setText(selectedProfile == null
                ? "Sem inconsistencias"
                : DesktopProjectInsights.profileStatusText(host.currentProject(), selectedProfile));
        CharacterTagProfile selectedCharacterProfile = CharacterTagProfileSynchronizer.profileForCharacter(host.currentProject(), host.selectedCharacter());
        selectedCharacterTagsLabel.setText("Tags do personagem: " + DesktopProjectInsights.formatCharacterTagSummary(selectedCharacterProfile));
        host.updateTagLibraryIssuesLabel();
        host.setSyncingUi(previousSyncingUi);
    }

    void onTagSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || host.isSyncingUi() || host.currentProject() == null) {
            return;
        }
        int selectedIndex = tagList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visibleTags.size()) {
            return;
        }
        host.setTagDraftMode(false);
        host.setSelectedTagBeforeDraft(null);
        host.setSelectedTag(visibleTags.get(selectedIndex));
        refreshTagLibrary();
    }

    void onProfileSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || host.isSyncingUi() || host.currentProject() == null) {
            return;
        }
        int selectedIndex = profileList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visibleProfiles.size()) {
            return;
        }
        CharacterTagProfile selectedProfile = visibleProfiles.get(selectedIndex);
        host.setSelectedProfile(selectedProfile);
        host.setSelectedCharacter(DesktopProjectInsights.findCharacterById(host.currentProject(), selectedProfile.getCharacterId()));
        host.refreshCharacterLists();
        refreshTagProfiles();
    }
}
