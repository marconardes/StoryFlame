window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state, status, utils } = window.storyflameUI;

function handleCreateProject() {
  return window.storyflameUI.coreActions.withBusy("Criando projeto...", () => {
    return window.storyflame.projects.create(dom.titleInput.value, dom.authorInput.value);
  });
}

function handleOpenProject() {
  return window.storyflameUI.coreActions.withBusy("Abrindo projeto...", () => {
    return window.storyflame.projects.pickArchiveAndOpen();
  });
}

function handleSaveProject() {
  return window.storyflameUI.coreActions.withBusy("Salvando projeto...", async () => {
    await window.storyflame.session.updateMetadata(dom.titleInput.value, dom.authorInput.value);
    await window.storyflame.session.updateScene(
      dom.sceneTitleInput.value,
      dom.sceneSynopsisInput.value,
      dom.sceneContentInput.value
    );
    const result = await window.storyflame.projects.save();
    state.isDirty = false;
    status.updateAutosaveLabel(`Autosave: salvo manualmente ${utils.formatTime(new Date())}.`, "success");
    status.setStatus("Projeto salvo.", "success");
    return result;
  });
}

function saveSceneToSession() {
  return window.storyflameUI.coreActions.withBusy("Atualizando cena...", async () => {
    await window.storyflame.session.updateMetadata(dom.titleInput.value, dom.authorInput.value);
    const result = await window.storyflame.session.updateScene(
      dom.sceneTitleInput.value,
      dom.sceneSynopsisInput.value,
      dom.sceneContentInput.value
    );
    state.isDirty = true;
    status.updateAutosaveLabel("Autosave: alteracoes pendentes.", "loading");
    status.setStatus("Cena atualizada na sessao.", "success");
    return result;
  });
}

function addChapter() {
  return window.storyflameUI.coreActions.withBusy("Criando capitulo...", () => {
    return window.storyflame.structure.addChapter().then((session) => {
      status.setStatus("Capitulo criado.", "success");
      return session;
    });
  });
}

function removeChapter() {
  return window.storyflameUI.coreActions.withBusy("Removendo capitulo...", () => {
    if (!window.confirm("Excluir o capitulo selecionado?")) {
      return Promise.resolve(state.currentSession);
    }
    return window.storyflame.structure.removeChapter().then((session) => {
      status.setStatus("Capitulo removido.", "success");
      return session;
    });
  });
}

function moveChapter(direction) {
  return window.storyflameUI.coreActions.withBusy("Movendo capitulo...", () => {
    return window.storyflame.structure.moveChapter(direction);
  });
}

function addScene() {
  return window.storyflameUI.coreActions.withBusy("Criando cena...", () => {
    return window.storyflame.structure.addScene().then((session) => {
      status.setStatus("Cena criada.", "success");
      return session;
    });
  });
}

function removeScene() {
  return window.storyflameUI.coreActions.withBusy("Removendo cena...", () => {
    if (!window.confirm("Excluir a cena selecionada?")) {
      return Promise.resolve(state.currentSession);
    }
    return window.storyflame.structure.removeScene().then((session) => {
      status.setStatus("Cena removida.", "success");
      return session;
    });
  });
}

function moveScene(direction) {
  return window.storyflameUI.coreActions.withBusy("Movendo cena...", () => {
    return window.storyflame.structure.moveScene(direction);
  });
}

window.storyflameUI.projectActions = {
  handleCreateProject,
  handleOpenProject,
  handleSaveProject,
  saveSceneToSession,
  addChapter,
  removeChapter,
  moveChapter,
  addScene,
  removeScene,
  moveScene
};
})();
