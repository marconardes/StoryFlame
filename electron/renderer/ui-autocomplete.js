window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state, status, utils } = window.storyflameUI;

let tagAutocompleteState = { start: -1, end: -1, items: [], activeIndex: 0 };

function renderTagAutocomplete(items) {
  if (!items.length) {
    dom.tagAutocomplete.classList.add("hidden");
    dom.tagAutocomplete.replaceChildren();
    return;
  }
  dom.tagAutocomplete.classList.remove("hidden");
  dom.tagAutocomplete.replaceChildren();
  items.forEach((tag, index) => {
    const item = document.createElement("div");
    item.className = "tag-autocomplete-item";
    if (index === tagAutocompleteState.activeIndex) {
      item.classList.add("active");
    }
    const label = document.createElement("strong");
    label.textContent = `#${tag.id || tag.label || "tag"}`;
    const detail = document.createElement("span");
    detail.textContent = tag.label || "sem rotulo";
    item.appendChild(label);
    item.appendChild(detail);
    item.addEventListener("click", () => {
      applyTagAutocomplete(tag);
    });
    dom.tagAutocomplete.appendChild(item);
  });
}

function updateTagAutocomplete() {
  if (!dom.sceneContentInput || !state.currentSession) {
    dom.tagAutocomplete.classList.add("hidden");
    return;
  }
  const caretPos = dom.sceneContentInput.selectionStart;
  const token = utils.extractTagToken(dom.sceneContentInput.value, caretPos);
  if (!token) {
    dom.tagAutocomplete.classList.add("hidden");
    tagAutocompleteState = { start: -1, end: -1, items: [], activeIndex: 0 };
    return;
  }
  const suggestions = utils.buildTagSuggestions(state.currentSession, token.query);
  tagAutocompleteState = {
    start: token.start,
    end: token.end,
    items: suggestions,
    activeIndex: 0
  };
  renderTagAutocomplete(suggestions);
}

function applyTagAutocomplete(tag) {
  if (!tag || tagAutocompleteState.start < 0) {
    return;
  }
  const value = dom.sceneContentInput.value || "";
  const before = value.slice(0, tagAutocompleteState.start);
  const after = value.slice(tagAutocompleteState.end);
  const insertion = `#${tag.id || tag.label || "tag"} `;
  const nextValue = `${before}${insertion}${after}`;
  const nextCaret = before.length + insertion.length;
  dom.sceneContentInput.value = nextValue;
  dom.sceneContentInput.setSelectionRange(nextCaret, nextCaret);
  dom.tagAutocomplete.classList.add("hidden");
  tagAutocompleteState = { start: -1, end: -1, items: [], activeIndex: 0 };
  status.markDirty();
}

function moveAutocompleteSelection(delta) {
  if (!tagAutocompleteState.items.length) {
    return;
  }
  const nextIndex = Math.max(
    0,
    Math.min(tagAutocompleteState.items.length - 1, tagAutocompleteState.activeIndex + delta)
  );
  tagAutocompleteState = {
    ...tagAutocompleteState,
    activeIndex: nextIndex
  };
  renderTagAutocomplete(tagAutocompleteState.items);
}

function applyActiveTagAutocomplete() {
  if (!tagAutocompleteState.items.length) {
    return;
  }
  applyTagAutocomplete(tagAutocompleteState.items[tagAutocompleteState.activeIndex]);
}

window.storyflameUI.autocomplete = {
  updateTagAutocomplete,
  applyTagAutocomplete,
  moveAutocompleteSelection,
  applyActiveTagAutocomplete
};
})();
