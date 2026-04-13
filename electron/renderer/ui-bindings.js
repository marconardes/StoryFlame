window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state, status, render, actions, autocomplete } = window.storyflameUI;

function initBindings() {
  dom.createButton.addEventListener("click", actions.handleCreateProject);
  dom.openButton.addEventListener("click", actions.handleOpenProject);
  dom.saveButton.addEventListener("click", actions.handleSaveProject);
  dom.toolbarCreateButton.addEventListener("click", actions.handleCreateProject);
  dom.toolbarOpenButton.addEventListener("click", actions.handleOpenProject);
  dom.toolbarSaveButton.addEventListener("click", actions.handleSaveProject);

  dom.saveSceneButton.addEventListener("click", actions.saveSceneToSession);

  dom.addChapterButton.addEventListener("click", actions.addChapter);
  dom.removeChapterButton.addEventListener("click", actions.removeChapter);
  dom.moveChapterUpButton.addEventListener("click", () => actions.moveChapter(-1));
  dom.moveChapterDownButton.addEventListener("click", () => actions.moveChapter(1));

  dom.addSceneButton.addEventListener("click", actions.addScene);
  if (dom.structureEmptyAction) {
    dom.structureEmptyAction.addEventListener("click", actions.addChapter);
  }
  dom.removeSceneButton.addEventListener("click", actions.removeScene);
  dom.moveSceneUpButton.addEventListener("click", () => actions.moveScene(-1));
  dom.moveSceneDownButton.addEventListener("click", () => actions.moveScene(1));

  dom.addCharacterButton.addEventListener("click", actions.startNewCharacter);
  if (dom.characterEmptyAction) {
    dom.characterEmptyAction.addEventListener("click", actions.startNewCharacter);
  }
  dom.saveCharacterButton.addEventListener("click", actions.saveCharacter);
  dom.deleteCharacterButton.addEventListener("click", actions.deleteCharacter);

  dom.addTagButton.addEventListener("click", actions.startNewTag);
  if (dom.tagEmptyAction) {
    dom.tagEmptyAction.addEventListener("click", actions.startNewTag);
  }
  dom.saveTagButton.addEventListener("click", actions.saveTag);
  dom.deleteTagButton.addEventListener("click", actions.deleteTag);

  dom.saveProfilePrefixButton.addEventListener("click", actions.saveProfilePrefix);
  if (dom.profileEmptyAction) {
    dom.profileEmptyAction.addEventListener("click", () => {
      status.setStatus("Selecione um personagem para carregar o perfil.", "loading");
    });
  }
  dom.addTagToProfileButton.addEventListener("click", actions.addTagToProfile);
  dom.removeTagFromProfileButton.addEventListener("click", actions.removeTagFromProfile);

  dom.tabButtons.forEach((button) => {
    button.addEventListener("click", () => actions.setActiveTab(button.dataset.tab));
  });

  dom.searchQueryInput.addEventListener("input", actions.handleSearchInput);
  if (dom.searchEmptyAction) {
    dom.searchEmptyAction.addEventListener("click", () => {
      dom.searchQueryInput.focus();
    });
  }

  dom.runAnalysisButton.addEventListener("click", actions.runAnalysis);
  if (dom.analysisEmptyAction) {
    dom.analysisEmptyAction.addEventListener("click", () => {
      dom.runAnalysisButton.click();
    });
  }

  dom.sceneContentInput.addEventListener("input", () => {
    autocomplete.updateTagAutocomplete();
  });

  dom.sceneContentInput.addEventListener("click", () => {
    autocomplete.updateTagAutocomplete();
  });

  dom.sceneContentInput.addEventListener("mouseup", () => {
    autocomplete.updateTagAutocomplete();
  });

  dom.sceneContentInput.addEventListener("scroll", () => {
    autocomplete.refreshTagAutocompletePosition();
  });

  dom.sceneContentInput.addEventListener("keydown", (event) => {
    if (event.ctrlKey && event.code === "Space") {
      event.preventDefault();
      autocomplete.showTagAutocomplete();
      return;
    }
    if (dom.tagAutocomplete.classList.contains("hidden")) {
      return;
    }
    if (event.key === "ArrowDown") {
      event.preventDefault();
      autocomplete.moveAutocompleteSelection(1);
    } else if (event.key === "ArrowUp") {
      event.preventDefault();
      autocomplete.moveAutocompleteSelection(-1);
    } else if (event.key === "Enter") {
      event.preventDefault();
      autocomplete.applyActiveTagAutocomplete();
    } else if (event.key === "Escape") {
      dom.tagAutocomplete.classList.add("hidden");
    }
  });

  dom.sceneContentInput.addEventListener("blur", () => {
    dom.tagAutocomplete.classList.add("hidden");
  });

  dom.sceneTitleSelect.addEventListener("change", () => {
    if (!dom.sceneTitleSelect.value) {
      return;
    }
    const [chapterId, sceneId] = dom.sceneTitleSelect.value.split("::");
    actions.selectScene(chapterId, sceneId);
  });

  window.addEventListener("resize", () => {
    autocomplete.refreshTagAutocompletePosition();
  });

  [
    dom.titleInput,
    dom.authorInput,
    dom.sceneSynopsisInput,
    dom.sceneContentInput,
    dom.characterNameInput,
    dom.characterDescriptionInput,
    dom.tagIdInput,
    dom.tagLabelInput,
    dom.tagTemplateInput,
    dom.profilePrefixInput
  ].forEach((field) => {
    if (!field) {
      return;
    }
    field.addEventListener("input", status.markDirty);
  });
}

window.storyflameUI.bindings = {
  initBindings
};
})();
