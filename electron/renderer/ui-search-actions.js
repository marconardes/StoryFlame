window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state, status, render, utils } = window.storyflameUI;

function getRenderHandlers() {
  if (window.storyflameUI.coreActions && typeof window.storyflameUI.coreActions.buildRenderHandlers === "function") {
    return window.storyflameUI.coreActions.buildRenderHandlers();
  }
  return {};
}

function navigateToSearchResult(result) {
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
  if (window.storyflameUI.coreActions && typeof window.storyflameUI.coreActions.selectScene === "function") {
    window.storyflameUI.coreActions.selectScene(chapter.id, scene.id);
  }
  if (window.storyflameUI.coreActions && typeof window.storyflameUI.coreActions.setActiveTab === "function") {
    window.storyflameUI.coreActions.setActiveTab("editor");
  }
  status.setStatus(`Navegacao rapida: ${utils.formatSearchTarget(result.target)}`, "success");
}

function handleSearchInput() {
  if (state.searchTimer) {
    clearTimeout(state.searchTimer);
  }
  state.searchTimer = setTimeout(async () => {
    if (!state.currentSession) {
      status.setInlineStatus(dom.searchStatus, "Nenhum projeto carregado.", "error");
      render.renderSearchResults([], getRenderHandlers());
      return;
    }
    const query = dom.searchQueryInput.value || "";
    if (query.trim().length === 0) {
      status.setInlineStatus(dom.searchStatus, "Digite para buscar no manuscrito.", "neutral");
      render.renderSearchResults([], getRenderHandlers());
      return;
    }
    status.setInlineStatus(dom.searchStatus, "Buscando...", "loading");
    try {
      const results = await window.storyflame.search.run(query);
      render.renderSearchResults(results, getRenderHandlers());
      if (results.length) {
        status.setInlineStatus(dom.searchStatus, `${results.length} resultado(s) encontrados.`, "success");
      } else {
        status.setInlineStatus(dom.searchStatus, `Nenhum resultado para "${query.trim()}".`, "neutral");
      }
    } catch (error) {
      status.setInlineStatus(dom.searchStatus, error.message, "error");
      render.renderSearchResults([], getRenderHandlers());
    }
  }, 250);
}

function runAnalysis() {
  return window.storyflameUI.coreActions.withBusy("Executando analise emocional...", async () => {
    if (!state.currentSession) {
      status.setInlineStatus(dom.analysisStatus, "Abra um projeto para executar a analise.", "neutral");
      return state.currentSession;
    }
    status.setInlineStatus(dom.analysisStatus, "Executando analise emocional...", "loading");
    const report = await window.storyflame.analysis.runEmotion();
    render.renderEmotionReport(report);
    if (!report) {
      status.setInlineStatus(dom.analysisStatus, "Analise indisponivel para este projeto.", "error");
    }
    return state.currentSession;
  });
}

window.storyflameUI.searchActions = {
  navigateToSearchResult,
  handleSearchInput,
  runAnalysis
};
})();
