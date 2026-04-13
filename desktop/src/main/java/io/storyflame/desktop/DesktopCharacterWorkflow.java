package io.storyflame.desktop;

import io.storyflame.app.project.ProjectCharacterApplicationService;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

final class DesktopCharacterWorkflow {
    interface Host {
        boolean isSyncingUi();
        PathState currentPathState();
        Project currentProject();
        Scene selectedScene();
        Character selectedCharacter();
        void setSelectedCharacter(Character character);
        Character selectedCharacterBeforeDraft();
        void setSelectedCharacterBeforeDraft(Character character);
        boolean isCharacterDraftMode();
        void setCharacterDraftMode(boolean value);
        CharacterTagProfile selectedProfile();
        void setSelectedProfile(CharacterTagProfile profile);
        NarrativeTag selectedTag();
        void syncProjectFromFields();
        void syncProjectFromFieldsExceptCharacter();
        void refreshCharacterLists();
        void refreshTagLibrary();
        void refreshTagProfiles();
        void refreshPointOfViewList();
        void renderSummary();
        void scheduleAutosave();
        void onProjectEdited();
        void updateIntegrityLabel();
        void setStatusText(String text);
        JFrame frame();
        void focusEditorFrame();
    }

    record PathState(boolean hasPath) {
    }

    private final Host host;
    private final ProjectCharacterApplicationService projectCharacterApplicationService;
    private final JTextField characterSearchField;
    private final JTextField characterNameField;
    private final JTextArea characterDescriptionArea;

    DesktopCharacterWorkflow(
            Host host,
            ProjectCharacterApplicationService projectCharacterApplicationService,
            JTextField characterSearchField,
            JTextField characterNameField,
            JTextArea characterDescriptionArea
    ) {
        this.host = host;
        this.projectCharacterApplicationService = projectCharacterApplicationService;
        this.characterSearchField = characterSearchField;
        this.characterNameField = characterNameField;
        this.characterDescriptionArea = characterDescriptionArea;
    }

    void onCharacterEdited() {
        if (host.isCharacterDraftMode()) {
            host.setStatusText("Preencha nome e descricao. Depois clique em Salvar personagem.");
            return;
        }
        if (host.isSyncingUi() || host.currentProject() == null || !host.currentPathState().hasPath() || host.selectedCharacter() == null) {
            return;
        }
        projectCharacterApplicationService.updateCharacter(
                host.currentProject(),
                host.selectedCharacter(),
                characterNameField.getText(),
                characterDescriptionArea.getText()
        );
        host.refreshCharacterLists();
        host.refreshTagLibrary();
        host.refreshTagProfiles();
        host.refreshPointOfViewList();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("Ficha de personagem atualizada. Revise e conclua quando estiver pronta.");
    }

    void applyCharacterNameUpdate() {
        if (host.currentProject() == null || host.selectedCharacter() == null) {
            if (!host.isCharacterDraftMode()) {
                return;
            }
        }
        if (characterNameField.getText().isBlank()) {
            host.setStatusText("Preencha o nome do personagem antes de salvar.");
            focusCharacterDraft();
            return;
        }
        if (host.isCharacterDraftMode()) {
            Character character = projectCharacterApplicationService.createCharacter(
                    host.currentProject(),
                    characterNameField.getText(),
                    characterDescriptionArea.getText()
            );
            host.setSelectedCharacter(character);
            host.setCharacterDraftMode(false);
            host.setSelectedCharacterBeforeDraft(null);
            host.refreshCharacterLists();
            host.refreshTagLibrary();
            host.refreshTagProfiles();
            host.refreshPointOfViewList();
            host.renderSummary();
            host.scheduleAutosave();
            host.refreshCharacterLists();
            host.refreshTagLibrary();
            host.refreshTagProfiles();
            host.setStatusText("Personagem criado.");
            return;
        }
        onCharacterEdited();
        host.setStatusText("Personagem salvo.");
    }

    void addCharacter() {
        Project currentProject = host.currentProject();
        if (currentProject == null) {
            return;
        }
        host.syncProjectFromFieldsExceptCharacter();
        host.setSelectedCharacterBeforeDraft(host.selectedCharacter());
        host.setSelectedCharacter(null);
        host.setCharacterDraftMode(true);
        if (!characterSearchField.getText().isBlank()) {
            characterSearchField.setText("");
        }
        characterNameField.setText("");
        characterDescriptionArea.setText("");
        host.refreshCharacterLists();
        focusCharacterDraft();
        host.setStatusText("Novo personagem. Preencha nome e descricao.");
    }

    void cancelCharacterDraft() {
        if (!host.isCharacterDraftMode()) {
            return;
        }
        host.setCharacterDraftMode(false);
        host.setSelectedCharacter(host.selectedCharacterBeforeDraft());
        host.setSelectedCharacterBeforeDraft(null);
        host.refreshCharacterLists();
        host.refreshTagProfiles();
        host.setStatusText("Criacao de personagem cancelada.");
    }

    void deleteCharacter() {
        Project currentProject = host.currentProject();
        Character selectedCharacter = host.selectedCharacter();
        if (currentProject == null || selectedCharacter == null) {
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(
                host.frame(),
                "Excluir personagem '" + DesktopProjectInsights.displayCharacterName(currentProject, host.selectedScene(), selectedCharacter) + "'?",
                "Excluir personagem",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        host.syncProjectFromFields();
        host.setSelectedCharacter(projectCharacterApplicationService.deleteCharacter(currentProject, selectedCharacter));
        host.refreshCharacterLists();
        host.refreshTagLibrary();
        host.refreshTagProfiles();
        host.refreshPointOfViewList();
        host.onProjectEdited();
    }

    void appendSelectedTagToCharacter() {
        Character selectedCharacter = host.selectedCharacter();
        if (selectedCharacter == null) {
            return;
        }
        if (host.selectedTag() == null) {
            return;
        }
        CharacterTagProfile profile = projectCharacterApplicationService.addTagToCharacter(
                host.currentProject(),
                selectedCharacter,
                host.selectedTag()
        );
        if (profile == null) {
            return;
        }
        host.setSelectedProfile(profile);
        host.refreshTagProfiles();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("Tag adicionada ao personagem.");
    }

    void removeSelectedTagFromCharacter() {
        Character selectedCharacter = host.selectedCharacter();
        if (selectedCharacter == null) {
            return;
        }
        if (host.selectedTag() == null) {
            return;
        }
        CharacterTagProfile profile = projectCharacterApplicationService.removeTagFromCharacter(
                host.currentProject(),
                selectedCharacter,
                host.selectedTag()
        );
        if (profile == null) {
            return;
        }
        host.setSelectedProfile(profile);
        host.refreshTagProfiles();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("Tag removida do personagem.");
    }

    void assignSelectedCharacterAsPointOfView() {
        Scene selectedScene = host.selectedScene();
        Character selectedCharacter = host.selectedCharacter();
        if (selectedScene == null || selectedCharacter == null) {
            return;
        }
        projectCharacterApplicationService.assignPointOfView(selectedScene, selectedCharacter);
        host.refreshPointOfViewList();
        host.updateIntegrityLabel();
        host.renderSummary();
        host.scheduleAutosave();
        host.setStatusText("POV definido a partir do personagem selecionado.");
        host.focusEditorFrame();
    }

    private void focusCharacterDraft() {
        SwingUtilities.invokeLater(() -> {
            characterNameField.requestFocusInWindow();
            characterNameField.selectAll();
        });
    }

}
