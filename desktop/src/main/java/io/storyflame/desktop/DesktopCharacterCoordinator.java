package io.storyflame.desktop;

import io.storyflame.core.character.CharacterDirectory;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.CharacterTagProfileSynchronizer;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

final class DesktopCharacterCoordinator {
    interface Host {
        Project currentProject();
        Scene selectedScene();
        Character selectedCharacter();
        void setSelectedCharacter(Character character);
        Character selectedCharacterBeforeDraft();
        void setSelectedCharacterBeforeDraft(Character character);
        CharacterTagProfile selectedProfile();
        void setSelectedProfile(CharacterTagProfile profile);
        boolean isCharacterDraftMode();
        void setCharacterDraftMode(boolean value);
        boolean isSyncingUi();
        void setSyncingUi(boolean syncingUi);
        void updateIntegrityLabel();
        void refreshTagProfiles();
        void displayNoResultsStatus(String text);
        String displayTitle(String value, String fallbackPrefix);
    }

    private final Host host;
    private final List<Character> visibleCharacters;
    private final DefaultListModel<String> characterListModel;
    private final JList<String> characterList;
    private final JTextField characterSearchField;
    private final JTextField characterNameField;
    private final JTextArea characterDescriptionArea;
    private final JLabel characterCountLabel;
    private final JLabel selectedCharacterScenesLabel;
    private final JLabel selectedCharacterPointOfViewLabel;
    private final JLabel selectedCharacterTagsLabel;
    private final JLabel characterDetailModeLabel;
    private final JLabel characterDraftHintLabel;

    DesktopCharacterCoordinator(
            Host host,
            List<Character> visibleCharacters,
            DefaultListModel<String> characterListModel,
            JList<String> characterList,
            JTextField characterSearchField,
            JTextField characterNameField,
            JTextArea characterDescriptionArea,
            JLabel characterCountLabel,
            JLabel selectedCharacterScenesLabel,
            JLabel selectedCharacterPointOfViewLabel,
            JLabel selectedCharacterTagsLabel,
            JLabel characterDetailModeLabel,
            JLabel characterDraftHintLabel
    ) {
        this.host = host;
        this.visibleCharacters = visibleCharacters;
        this.characterListModel = characterListModel;
        this.characterList = characterList;
        this.characterSearchField = characterSearchField;
        this.characterNameField = characterNameField;
        this.characterDescriptionArea = characterDescriptionArea;
        this.characterCountLabel = characterCountLabel;
        this.selectedCharacterScenesLabel = selectedCharacterScenesLabel;
        this.selectedCharacterPointOfViewLabel = selectedCharacterPointOfViewLabel;
        this.selectedCharacterTagsLabel = selectedCharacterTagsLabel;
        this.characterDetailModeLabel = characterDetailModeLabel;
        this.characterDraftHintLabel = characterDraftHintLabel;
    }

    void refreshCharacterLists() {
        boolean previousSyncingUi = host.isSyncingUi();
        host.setSyncingUi(true);
        Character previousSelection = host.selectedCharacter();
        Project currentProject = host.currentProject();
        CharacterTagProfileSynchronizer.synchronize(currentProject);

        visibleCharacters.clear();
        characterListModel.clear();
        if (currentProject == null) {
            characterNameField.setText("");
            characterDescriptionArea.setText("");
            characterCountLabel.setText("0 personagens");
            selectedCharacterScenesLabel.setText("0 cenas ligadas");
            selectedCharacterPointOfViewLabel.setText("Nao e o POV atual");
            selectedCharacterTagsLabel.setText("Tags do personagem: nenhuma");
            characterDetailModeLabel.setText("Crie um personagem para comecar a editar");
            characterDraftHintLabel.setText(DesktopDraftStateFormatter.characterDraftHint(false, "", ""));
            host.updateIntegrityLabel();
            host.setSyncingUi(previousSyncingUi);
            return;
        }

        String query = characterSearchField.getText() == null ? "" : characterSearchField.getText().trim();
        for (Character character : CharacterDirectory.search(currentProject, query)) {
            visibleCharacters.add(character);
            characterListModel.addElement(DesktopProjectInsights.displayCharacterName(currentProject, host.selectedScene(), character));
        }
        if (!query.isBlank() && visibleCharacters.isEmpty()) {
            host.displayNoResultsStatus("Nenhum personagem encontrado para a busca atual.");
        }

        characterCountLabel.setText(currentProject.getCharacters().size() + " personagens");
        if (host.isCharacterDraftMode()) {
            characterList.clearSelection();
        } else if (previousSelection != null && visibleCharacters.contains(previousSelection)) {
            characterList.setSelectedIndex(visibleCharacters.indexOf(previousSelection));
            host.setSelectedCharacter(previousSelection);
        } else if (!visibleCharacters.isEmpty()) {
            characterList.setSelectedIndex(0);
            host.setSelectedCharacter(visibleCharacters.get(0));
        } else if (!currentProject.getCharacters().contains(host.selectedCharacter())) {
            host.setSelectedCharacter(currentProject.getCharacters().isEmpty() ? null : currentProject.getCharacters().get(0));
        }

        Character selectedCharacter = host.isCharacterDraftMode() ? null : host.selectedCharacter();
        String characterName = selectedCharacter == null ? "" : selectedCharacter.getName();
        String characterDescription = selectedCharacter == null ? "" : selectedCharacter.getDescription();
        if (!host.isCharacterDraftMode() && !characterNameField.isFocusOwner() && !characterName.equals(characterNameField.getText())) {
            characterNameField.setText(characterName);
        }
        if (!host.isCharacterDraftMode() && !characterDescriptionArea.isFocusOwner() && !characterDescription.equals(characterDescriptionArea.getText())) {
            characterDescriptionArea.setText(characterDescription);
        }
        characterDetailModeLabel.setText(host.isCharacterDraftMode() || selectedCharacter == null
                ? "Novo personagem"
                : "Editando personagem");
        characterDraftHintLabel.setText(DesktopDraftStateFormatter.characterDraftHint(
                !host.isCharacterDraftMode() && selectedCharacter != null,
                host.isCharacterDraftMode() ? characterNameField.getText() : characterName,
                host.isCharacterDraftMode() ? characterDescriptionArea.getText() : characterDescription
        ));
        selectedCharacterScenesLabel.setText(selectedCharacter == null
                ? "0 cenas ligadas"
                : DesktopProjectInsights.countScenesForCharacter(currentProject, selectedCharacter) + " cenas ligadas");
        selectedCharacterPointOfViewLabel.setText(selectedCharacter == null
                ? "Nao e o POV atual"
                : (DesktopProjectInsights.isSelectedCharacterPointOfView(host.selectedScene(), selectedCharacter) ? "POV da cena atual" : "Nao e o POV atual"));
        CharacterTagProfile selectedCharacterProfile = CharacterTagProfileSynchronizer.profileForCharacter(currentProject, selectedCharacter);
        if (selectedCharacterProfile != null) {
            host.setSelectedProfile(selectedCharacterProfile);
        }
        selectedCharacterTagsLabel.setText("Tags do personagem: " + DesktopProjectInsights.formatCharacterTagSummary(selectedCharacterProfile));
        host.updateIntegrityLabel();
        host.setSyncingUi(previousSyncingUi);
    }

    void onCharacterSelected(ListSelectionEvent event) {
        Project currentProject = host.currentProject();
        if (event.getValueIsAdjusting() || host.isSyncingUi() || currentProject == null) {
            return;
        }
        int selectedIndex = characterList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= visibleCharacters.size()) {
            return;
        }
        host.setCharacterDraftMode(false);
        host.setSelectedCharacterBeforeDraft(null);
        host.setSelectedCharacter(visibleCharacters.get(selectedIndex));
        CharacterTagProfile selectedCharacterProfile = CharacterTagProfileSynchronizer.profileForCharacter(currentProject, host.selectedCharacter());
        if (selectedCharacterProfile != null) {
            host.setSelectedProfile(selectedCharacterProfile);
        }
        refreshCharacterLists();
        host.refreshTagProfiles();
    }
}
