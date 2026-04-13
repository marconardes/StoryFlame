window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state } = window.storyflameUI;

function setStatus(message, tone = "neutral") {
  dom.statusMessage.textContent = message;
  dom.statusMessage.className = `status-text status-${tone}`;
}

function setInlineStatus(element, message, tone = "neutral", baseClass = "editor-subtle") {
  element.textContent = message;
  element.className = `status-text ${baseClass} status-${tone}`;
}

function updateAutosaveLabel(text, tone = "neutral") {
  dom.autosaveLabel.textContent = text;
  dom.autosaveLabel.className = `status-pill status-${tone}`;
}

function markDirty() {
  if (!state.currentSession) {
    return;
  }
  state.isDirty = true;
  updateAutosaveLabel("Autosave: alteracoes pendentes.", "loading");
}

function applyEmptyState(element, baseClass, message) {
  element.className = `${baseClass} empty-state`;
  const messageElement = element.querySelector("p");
  if (messageElement) {
    messageElement.textContent = message;
    return;
  }
  element.textContent = message;
}

function updateEmptyActionVisibility(listElement, actionButton, shouldShow) {
  if (!listElement || !actionButton) {
    return;
  }
  actionButton.hidden = !shouldShow;
  if (shouldShow) {
    listElement.classList.add("has-action");
  } else {
    listElement.classList.remove("has-action");
  }
}

window.storyflameUI.status = {
  setStatus,
  setInlineStatus,
  updateAutosaveLabel,
  markDirty,
  applyEmptyState,
  updateEmptyActionVisibility
};
})();
