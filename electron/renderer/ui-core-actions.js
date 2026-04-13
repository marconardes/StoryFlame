window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state, status, render, utils } = window.storyflameUI;

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

async function refreshLocalProjects() {
  try {
    const projects = await window.storyflame.projects.local();
    render.renderLocalProjects(projects, buildRenderHandlers());
  } catch (error) {
    dom.localProjects.innerHTML = `<p class='subtle'>${error.message}</p>`;
  }
}

function buildRenderHandlers() {
  return {
    onSelectScene: selectScene,
    onSelectCharacter: (characterId) => withBusy("Selecionando personagem...", () => {
      state.characterDraftMode = false;
      return window.storyflame.characters.select(characterId);
    }),
    onSelectTag: (tagId) => withBusy("Selecionando tag...", () => {
      state.tagDraftMode = false;
      return window.storyflame.tags.select(tagId);
    }),
    onSelectProfile: (characterId) => withBusy("Selecionando perfil...", () => {
      state.profileDraftMode = false;
      return window.storyflame.profiles.select(characterId);
    }),
    onNavigateSearch: navigateToSearchResult,
    onOpenLocalProject: (path) => withBusy("Abrindo projeto local...", () => {
      return window.storyflame.projects.open(path);
    }),
    onDeleteLocalProject: (path, label) => withBusy("Removendo projeto...", () => {
      if (!window.confirm(`Excluir o projeto local ${label}?`)) {
        return Promise.resolve(state.currentSession);
      }
      return window.storyflame.projects.remove(path).then((session) => {
        status.setStatus("Projeto local removido.", "success");
        return session;
      });
    })
  };
}

async function withBusy(message, action) {
  status.setStatus(message, "loading");
  setBusy(true);
  try {
    const session = await action();
    if (session) {
      render.renderSession(session, buildRenderHandlers());
    }
    await refreshLocalProjects();
  } catch (error) {
    status.setStatus(error.message, "error");
  } finally {
    setBusy(false);
  }
}

async function selectScene(chapterId, sceneId) {
  await withBusy("Carregando cena...", () => {
    return window.storyflame.session.selectScene(chapterId, sceneId);
  });
}

async function refreshSession() {
  try {
    const session = await window.storyflame.session.current();
    render.renderSession(session, buildRenderHandlers());
    return session;
  } catch (error) {
    status.setStatus(error.message, "error");
    return null;
  }
}

async function navigateToSearchResult(result) {
  if (!state.currentSession || !state.currentSession.project || !state.currentSession.project.chapters) {
    status.setStatus("Projeto indisponivel para navegacao.", "error");
    return;
  }
  const chapter = state.currentSession.project.chapters[result.chapterIndex];
  if (!chapter) {
    status.setStatus("Capitulo nao encontrado para resultado.", "error");
    return;
  }
  let scene = chapter.scenes && chapter.scenes[result.sceneIndex];
  if (result.target === "CHAPTER" && chapter.scenes && chapter.scenes.length > 0) {
    scene = chapter.scenes[0];
  }
  if (!scene) {
    status.setStatus("Cena nao encontrada para resultado.", "error");
    return;
  }
  await selectScene(chapter.id, scene.id);
  setActiveTab("editor");
  status.setStatus(`Navegacao rapida: ${utils.formatSearchTarget(result.target)}`, "success");
}

async function runAutosaveIfNeeded() {
  if (!state.currentSession || !state.isDirty || state.isBusy) {
    return;
  }
  try {
    await window.storyflame.session.updateMetadata(dom.titleInput.value, dom.authorInput.value);
    await window.storyflame.session.updateScene(
      dom.sceneTitleInput.value,
      dom.sceneSynopsisInput.value,
      dom.sceneContentInput.value
    );
    await window.storyflame.projects.save();
    state.isDirty = false;
    status.updateAutosaveLabel(`Autosave: salvo ${utils.formatTime(new Date())}.`, "success");
  } catch (error) {
    status.updateAutosaveLabel(`Autosave falhou: ${error.message}`, "error");
  }
}

window.storyflameUI.coreActions = {
  setActiveTab,
  setBusy,
  withBusy,
  refreshSession,
  refreshLocalProjects,
  selectScene,
  navigateToSearchResult,
  buildRenderHandlers,
  runAutosaveIfNeeded
};
})();
