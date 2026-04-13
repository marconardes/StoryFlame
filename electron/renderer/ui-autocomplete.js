window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state, status, utils } = window.storyflameUI;

let tagAutocompleteState = { start: -1, end: -1, items: [], activeIndex: 0 };
let forceShowAutocomplete = false;
let manualAutocompleteOpen = false;
let pendingAutocompletePosition = false;

function clearTagAutocompletePosition() {
  dom.tagAutocomplete.style.top = "";
  dom.tagAutocomplete.style.left = "";
  dom.tagAutocomplete.style.width = "";
  dom.tagAutocomplete.style.right = "";
  dom.tagAutocomplete.style.bottom = "";
}

function measureCaretPosition(textarea, position) {
  const style = window.getComputedStyle(textarea);
  const mirror = document.createElement("div");
  mirror.style.position = "absolute";
  mirror.style.visibility = "hidden";
  mirror.style.whiteSpace = "pre-wrap";
  mirror.style.wordWrap = "break-word";
  mirror.style.top = "0";
  mirror.style.left = "-9999px";
  mirror.style.boxSizing = style.boxSizing;
  mirror.style.width = `${textarea.clientWidth}px`;
  mirror.style.overflow = "hidden";

  const properties = [
    "borderTopWidth",
    "borderRightWidth",
    "borderBottomWidth",
    "borderLeftWidth",
    "paddingTop",
    "paddingRight",
    "paddingBottom",
    "paddingLeft",
    "fontStyle",
    "fontVariant",
    "fontWeight",
    "fontStretch",
    "fontSize",
    "lineHeight",
    "fontFamily",
    "letterSpacing",
    "textTransform",
    "textIndent",
    "textDecoration",
    "tabSize"
  ];

  properties.forEach((property) => {
    mirror.style[property] = style[property];
  });

  const value = textarea.value || "";
  mirror.textContent = value.slice(0, position);
  const marker = document.createElement("span");
  marker.textContent = value.slice(position) || ".";
  mirror.appendChild(marker);
  document.body.appendChild(mirror);

  const coordinates = {
    top: marker.offsetTop,
    left: marker.offsetLeft
  };

  document.body.removeChild(mirror);
  return coordinates;
}

function positionTagAutocomplete() {
  if (dom.tagAutocomplete.classList.contains("hidden") || !dom.sceneContentInput) {
    clearTagAutocompletePosition();
    return;
  }

  const textarea = dom.sceneContentInput;
  const container = dom.tagAutocomplete.parentElement;
  if (!container) {
    return;
  }

  const containerRect = container.getBoundingClientRect();
  const textareaRect = textarea.getBoundingClientRect();
  const caretPos = Math.max(0, textarea.selectionStart || 0);
  const caret = measureCaretPosition(textarea, caretPos);
  const style = window.getComputedStyle(textarea);
  const lineHeight = Number.parseFloat(style.lineHeight) || Number.parseFloat(style.fontSize) * 1.2 || 18;
  const horizontalPadding = 12;
  const verticalGap = 8;
  const popupWidth = Math.min(Math.max(280, Math.round(textareaRect.width * 0.58)), Math.max(280, container.clientWidth - 16));

  let left = textareaRect.left - containerRect.left + caret.left - textarea.scrollLeft + horizontalPadding;
  let top = textareaRect.top - containerRect.top + caret.top - textarea.scrollTop + lineHeight + verticalGap;

  dom.tagAutocomplete.style.width = `${popupWidth}px`;
  dom.tagAutocomplete.style.left = `${Math.max(0, Math.min(left, Math.max(0, container.clientWidth - popupWidth - 8)))}px`;
  dom.tagAutocomplete.style.top = `${Math.max(0, top)}px`;
  dom.tagAutocomplete.style.right = "auto";
  dom.tagAutocomplete.style.bottom = "auto";

  const popupRect = dom.tagAutocomplete.getBoundingClientRect();
  const containerBottom = containerRect.top + container.clientHeight;
  const popupBottom = popupRect.bottom;
  if (popupBottom > containerBottom && top - dom.tagAutocomplete.offsetHeight - verticalGap > 0) {
    const aboveTop = textareaRect.top - containerRect.top + caret.top - textarea.scrollTop - dom.tagAutocomplete.offsetHeight - verticalGap;
    dom.tagAutocomplete.style.top = `${Math.max(0, aboveTop)}px`;
  }
}

function renderTagAutocomplete(items) {
  if (!items.length) {
    dom.tagAutocomplete.classList.add("hidden");
    dom.tagAutocomplete.replaceChildren();
    clearTagAutocompletePosition();
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
    label.textContent = `{${tag.id || tag.label || "tag"}}`;
    const detail = document.createElement("span");
    detail.textContent = tag.label || "sem rotulo";
    item.appendChild(label);
    item.appendChild(detail);
    item.addEventListener("mousedown", (event) => {
      event.preventDefault();
    });
    item.addEventListener("click", () => {
      applyTagAutocomplete(tag);
    });
    dom.tagAutocomplete.appendChild(item);
  });
  pendingAutocompletePosition = true;
  requestAnimationFrame(() => {
    if (pendingAutocompletePosition) {
      positionTagAutocomplete();
      pendingAutocompletePosition = false;
    }
  });
}

function updateTagAutocomplete() {
  if (!dom.sceneContentInput || !state.currentSession || !state.currentSession.scene) {
    dom.tagAutocomplete.classList.add("hidden");
    clearTagAutocompletePosition();
    return;
  }
  const caretPos = dom.sceneContentInput.selectionStart;
  const token = utils.extractTagToken(dom.sceneContentInput.value, caretPos);
  if (!token && !manualAutocompleteOpen) {
    dom.tagAutocomplete.classList.add("hidden");
    clearTagAutocompletePosition();
    tagAutocompleteState = { start: -1, end: -1, items: [], activeIndex: 0 };
    return;
  }

  const query = token ? token.query : "";
  const suggestions = utils.buildTagSuggestions(state.currentSession, query);
  if (!manualAutocompleteOpen && !forceShowAutocomplete && query.trim().length < 2) {
    dom.tagAutocomplete.classList.add("hidden");
    clearTagAutocompletePosition();
    tagAutocompleteState = { start: -1, end: -1, items: [], activeIndex: 0 };
    return;
  }
  tagAutocompleteState = {
    start: token ? token.start : caretPos,
    end: token ? token.end : caretPos,
    items: suggestions,
    activeIndex: 0
  };
  renderTagAutocomplete(suggestions);
  forceShowAutocomplete = false;
  manualAutocompleteOpen = false;
}

function applyTagAutocomplete(tag) {
  if (!tag || tagAutocompleteState.start < 0) {
    return;
  }
  const value = dom.sceneContentInput.value || "";
  const before = value.slice(0, tagAutocompleteState.start);
  const after = value.slice(tagAutocompleteState.end);
  const insertion = `{${tag.id || tag.label || "tag"}} `;
  const nextValue = `${before}${insertion}${after}`;
  const nextCaret = before.length + insertion.length;
  dom.sceneContentInput.value = nextValue;
  dom.sceneContentInput.setSelectionRange(nextCaret, nextCaret);
  dom.tagAutocomplete.classList.add("hidden");
  clearTagAutocompletePosition();
  tagAutocompleteState = { start: -1, end: -1, items: [], activeIndex: 0 };
  forceShowAutocomplete = false;
  manualAutocompleteOpen = false;
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

function showTagAutocomplete() {
  forceShowAutocomplete = true;
  manualAutocompleteOpen = true;
  updateTagAutocomplete();
}

function refreshTagAutocompletePosition() {
  positionTagAutocomplete();
}

window.storyflameUI.autocomplete = {
  updateTagAutocomplete,
  applyTagAutocomplete,
  moveAutocompleteSelection,
  applyActiveTagAutocomplete,
  showTagAutocomplete,
  refreshTagAutocompletePosition
};
})();
