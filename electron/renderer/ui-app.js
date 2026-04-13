window.storyflameUI = window.storyflameUI || {};

(() => {
const { bindings, state, coreActions, render, status, dom } = window.storyflameUI;

async function initApp() {
  coreActions.setActiveTab(state.activeTab);
  try {
    const base = await window.storyflame.bridge.baseDirectory();
    dom.baseDirectory.textContent = base.path;
  } catch (error) {
    dom.baseDirectory.textContent = error.message;
  }
  await coreActions.refreshLocalProjects();
  const session = await coreActions.refreshSession();
  if (session) {
    try {
      const report = await window.storyflame.analysis.currentEmotion();
      render.renderEmotionReport(report);
    } catch (error) {
      status.setInlineStatus(dom.analysisStatus, error.message, "error");
    }
  }
  if (state.autosaveTimer) {
    clearInterval(state.autosaveTimer);
  }
  state.autosaveTimer = setInterval(coreActions.runAutosaveIfNeeded, 90000);
}

function bootstrap() {
  bindings.initBindings();
  if (document.readyState === "loading") {
    window.addEventListener("DOMContentLoaded", () => {
      initApp();
    }, { once: true });
    return;
  }
  initApp();
}

window.storyflameUI.app = {
  initApp,
  bootstrap
};
})();
