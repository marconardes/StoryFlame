window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state, status, render } = window.storyflameUI;

function setActiveTab(tabId) {
  state.activeTab = tabId;
  dom.tabButtons.forEach((button) => {
    button.classList.toggle("active", button.dataset.tab === tabId);
  });
  dom.tabPanels.forEach((panel) => {
    panel.classList.toggle("active", panel.dataset.tabPanel === tabId);
  });
}

function setBusy(busy) {
  state.isBusy = busy;
  [
    dom.createButton,
    dom.openButton,
    dom.saveButton,
    dom.toolbarCreateButton,
    dom.toolbarOpenButton,
    dom.toolbarSaveButton,
    dom.saveSceneButton,
    dom.addCharacterButton,
    dom.deleteCharacterButton,
    dom.saveCharacterButton,
    dom.addTagButton,
    dom.deleteTagButton,
    dom.saveTagButton,
    dom.runAnalysisButton,
    dom.saveProfilePrefixButton,
    dom.addTagToProfileButton,
    dom.removeTagFromProfileButton,
    dom.addChapterButton,
    dom.removeChapterButton,
    dom.moveChapterUpButton,
    dom.moveChapterDownButton,
    dom.addSceneButton,
    dom.removeSceneButton,
    dom.moveSceneUpButton,
    dom.moveSceneDownButton
  ].forEach((button) => {
    if (button) {
      button.disabled = busy;
    }
  });
}

async function withBusy(message, action) {
  status.setStatus(message, "loading");
  setBusy(true);
  try {
    const session = await action();
    if (session) {
      const handlers = window.storyflameUI.coreActions?.buildRenderHandlers?.();
      if (handlers && render?.renderSession) {
        render.renderSession(session, handlers);
      }
    }
    if (window.storyflameUI.coreActions?.refreshLocalProjects) {
      await window.storyflameUI.coreActions.refreshLocalProjects();
    }
  } catch (error) {
    status.setStatus(error.message, "error");
  } finally {
    setBusy(false);
  }
}

function startNewCharacter() {
  state.characterDraftMode = true;
  setActiveTab("characters");
  dom.characterSelectionLabel.textContent = "Novo personagem";
  dom.characterNameInput.value = "";
  dom.characterDescriptionInput.value = "";
  dom.characterScenesInfo.textContent = "0 cenas ligadas";
  dom.characterPointOfViewInfo.textContent = "Nao e o POV atual";
  status.setStatus("Novo personagem. Preencha nome e descricao.", "loading");
}

function saveCharacter() {
  return withBusy(
    state.characterDraftMode || !state.currentSession?.characterSelection?.characterId
      ? "Criando personagem..."
      : "Salvando personagem...",
    async () => {
      if (state.characterDraftMode || !state.currentSession?.characterSelection?.characterId) {
        state.characterDraftMode = false;
        const session = await window.storyflame.characters.create(
          dom.characterNameInput.value,
          dom.characterDescriptionInput.value
        );
        status.setStatus("Personagem criado.", "success");
        return session;
      }
      const session = await window.storyflame.characters.update(
        dom.characterNameInput.value,
        dom.characterDescriptionInput.value
      );
      status.setStatus("Personagem salvo.", "success");
      return session;
    }
  );
}

function deleteCharacter() {
  return withBusy("Removendo personagem...", () => {
    if (!window.confirm("Excluir o personagem selecionado?")) {
      return Promise.resolve(state.currentSession);
    }
    state.characterDraftMode = false;
    return window.storyflame.characters.remove().then((session) => {
      status.setStatus("Personagem removido.", "success");
      return session;
    });
  });
}

function startNewTag() {
  state.tagDraftMode = true;
  setActiveTab("tags");
  dom.tagSelectionLabel.textContent = "Nova tag";
  dom.tagIdInput.value = "";
  dom.tagLabelInput.value = "";
  dom.tagTemplateInput.value = "";
  dom.tagUsageInfo.textContent = "0 usos no manuscrito";
  status.setStatus("Nova tag. Preencha id, rotulo e texto renderizado.", "loading");
}

function saveTag() {
  return withBusy(
    state.tagDraftMode || !state.currentSession?.tagSelection?.tagId ? "Criando tag..." : "Salvando tag...",
    async () => {
      if (state.tagDraftMode || !state.currentSession?.tagSelection?.tagId) {
        state.tagDraftMode = false;
        const session = await window.storyflame.tags.create(
          dom.tagIdInput.value,
          dom.tagLabelInput.value,
          dom.tagTemplateInput.value
        );
        status.setStatus("Tag criada.", "success");
        return session;
      }
      const session = await window.storyflame.tags.update(
        dom.tagIdInput.value,
        dom.tagLabelInput.value,
        dom.tagTemplateInput.value
      );
      status.setStatus("Tag salva.", "success");
      return session;
    }
  );
}

function deleteTag() {
  return withBusy("Removendo tag...", () => {
    if (!window.confirm("Excluir a tag selecionada?")) {
      return Promise.resolve(state.currentSession);
    }
    state.tagDraftMode = false;
    return window.storyflame.tags.remove().then((session) => {
      status.setStatus("Tag removida.", "success");
      return session;
    });
  });
}

function saveProfilePrefix() {
  return withBusy("Salvando prefixo...", async () => {
    if (!state.currentSession?.profileSelection?.characterId) {
      status.setStatus("Selecione um perfil antes de salvar.", "error");
      return state.currentSession;
    }
    state.profileDraftMode = false;
    const session = await window.storyflame.profiles.updatePrefix(dom.profilePrefixInput.value);
    status.setStatus("Prefixo do perfil salvo.", "success");
    return session;
  });
}

function addTagToProfile() {
  return withBusy("Adicionando tag ao perfil...", () => {
    if (!state.currentSession?.profileSelection?.characterId || !state.currentSession?.tagSelection?.tagId) {
      status.setStatus("Selecione perfil e tag.", "error");
      return Promise.resolve(state.currentSession);
    }
    return window.storyflame.profiles.addTag(state.currentSession.tagSelection.tagId).then((session) => {
      status.setStatus("Tag adicionada ao perfil.", "success");
      return session;
    });
  });
}

function removeTagFromProfile() {
  return withBusy("Removendo tag do perfil...", () => {
    if (!state.currentSession?.profileSelection?.characterId || !state.currentSession?.tagSelection?.tagId) {
      status.setStatus("Selecione perfil e tag.", "error");
      return Promise.resolve(state.currentSession);
    }
    return window.storyflame.profiles.removeTag(state.currentSession.tagSelection.tagId).then((session) => {
      status.setStatus("Tag removida do perfil.", "success");
      return session;
    });
  });
}

window.storyflameUI.entityActions = {
  startNewCharacter,
  saveCharacter,
  deleteCharacter,
  startNewTag,
  saveTag,
  deleteTag,
  saveProfilePrefix,
  addTagToProfile,
  removeTagFromProfile
};
})();
