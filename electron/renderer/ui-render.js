window.storyflameUI = window.storyflameUI || {};

(() => {
const { dom, state, status, utils } = window.storyflameUI;

function renderSession(session, handlers) {
  state.currentSession = session;
  if (!session) {
    dom.projectTitle.textContent = "Nenhum projeto carregado";
    dom.projectMeta.textContent = "Crie ou abra um projeto para iniciar o fluxo Electron.";
    dom.structureCount.textContent = "0 capitulos";
    dom.chapterCountBadge.textContent = "0 capitulos";
    dom.sceneCountBadge.textContent = "0 cenas";
    status.applyEmptyState(dom.structureTree, "structure", "Nenhuma estrutura carregada.");
    dom.validationBadge.textContent = "Sem validacao";
    dom.validationBadge.className = "badge";
    dom.validationList.innerHTML = "<li class=\"validation-item validation-ok\">Nenhuma validacao carregada.</li>";
    dom.sceneSelectionLabel.textContent = "Nenhuma cena selecionada.";
    renderSceneTitleSelect(null);
    dom.sceneSynopsisInput.value = "";
    dom.sceneContentInput.value = "";
    dom.characterCountBadge.textContent = "0 personagens";
    status.applyEmptyState(dom.characterList, "character-list", "Nenhum personagem carregado.");
    dom.characterSelectionLabel.textContent = "Nenhum personagem selecionado.";
    dom.characterNameInput.value = "";
    dom.characterDescriptionInput.value = "";
    dom.characterScenesInfo.textContent = "0 cenas ligadas";
    dom.characterPointOfViewInfo.textContent = "Nao e o POV atual";
    dom.tagCountBadge.textContent = "0 tags";
    status.applyEmptyState(dom.tagList, "character-list", "Nenhuma tag carregada.");
    dom.tagSelectionLabel.textContent = "Nenhuma tag selecionada.";
    dom.tagIdInput.value = "";
    dom.tagLabelInput.value = "";
    dom.tagTemplateInput.value = "";
    dom.tagUsageInfo.textContent = "0 usos no manuscrito";
    dom.profileSelectionLabel.textContent = "Nenhum perfil selecionado.";
    status.applyEmptyState(dom.profileList, "character-list", "Nenhum perfil carregado.");
    dom.profilePrefixInput.value = "";
    dom.profileTagsInfo.textContent = "Tags do perfil: nenhuma";
    dom.searchCountBadge.textContent = "0 resultados";
    status.applyEmptyState(dom.searchResults, "search-results", "Nenhum resultado ainda.");
    status.setInlineStatus(dom.searchStatus, "Digite para buscar no manuscrito.", "neutral");
    status.setInlineStatus(dom.analysisStatus, "Abra um projeto para executar a analise.", "neutral");
    dom.analysisSentimentBadge.textContent = "Clima geral: sem analise";
    dom.analysisDominantBadge.textContent = "Emocao mais presente: sem analise";
    dom.analysisChunkBadge.textContent = "0 trechos avaliados";
    status.applyEmptyState(dom.analysisResults, "analysis-results", "Nenhum relatorio emocional carregado.");
    dom.runAnalysisButton.disabled = true;
    dom.tagAutocomplete.classList.add("hidden");
    status.updateAutosaveLabel("Autosave: aguardando.", "neutral");
    dom.contextSummary.textContent = "Projeto, estrutura e editor aguardando sessao ativa.";
    status.updateEmptyActionVisibility(dom.structureTree, dom.structureEmptyAction, true);
    status.updateEmptyActionVisibility(dom.characterList, dom.characterEmptyAction, true);
    status.updateEmptyActionVisibility(dom.tagList, dom.tagEmptyAction, true);
    status.updateEmptyActionVisibility(dom.profileList, dom.profileEmptyAction, true);
    status.updateEmptyActionVisibility(dom.searchResults, dom.searchEmptyAction, true);
    status.updateEmptyActionVisibility(dom.analysisResults, dom.analysisEmptyAction, true);
    return;
  }

  dom.titleInput.value = session.project.title || "";
  dom.authorInput.value = session.project.author || "";
  dom.projectTitle.textContent = session.project.title || "Projeto sem titulo";
  dom.projectMeta.textContent = `${session.project.author || "Autor"} • ${session.project.chapterCount} capitulos • ${session.project.sceneCount} cenas`;
  status.setStatus(session.message || "Sessao carregada.", "success");
  dom.structureCount.textContent = `${session.project.chapterCount} capitulos`;
  dom.chapterCountBadge.textContent = `${session.project.chapterCount} capitulos`;
  dom.sceneCountBadge.textContent = `${session.project.sceneCount} cenas`;
  renderStructure(session.project.chapters || [], handlers);
  renderValidation(session.validation);
  renderSceneTitleSelect(session);
  renderScene(session);
  renderCharacters(session.project.characters || [], session.characterSelection, handlers);
  const tags = session.project.tags || session.project.narrativeTags || [];
  renderTags(tags, session.tagSelection, handlers);
  renderProfiles(session.project.profiles || [], session.profileSelection, tags, handlers);
  dom.runAnalysisButton.disabled = false;
  dom.contextSummary.textContent = session.selection && session.scene
    ? `Capitulo ativo: ${session.selection.chapterId} • Cena ativa: ${session.selection.sceneId}`
    : "Sessao carregada sem cena selecionada.";
}

function renderStructure(chapters, handlers) {
  if (!chapters.length) {
    status.applyEmptyState(dom.structureTree, "structure", "Estrutura vazia.");
    status.updateEmptyActionVisibility(dom.structureTree, dom.structureEmptyAction, true);
    return;
  }

  status.updateEmptyActionVisibility(dom.structureTree, dom.structureEmptyAction, false);
  dom.structureTree.className = "structure";
  const list = document.createElement("ol");
  chapters.forEach((chapter) => {
    const chapterItem = document.createElement("li");
    chapterItem.innerHTML = `<strong>${chapter.title || "Capitulo"}</strong>`;
    const scenes = document.createElement("ul");
    chapter.scenes.forEach((scene) => {
      const sceneItem = document.createElement("li");
      const synopsis = scene.synopsis ? ` <span>${scene.synopsis}</span>` : "";
      sceneItem.dataset.chapterId = chapter.id;
      sceneItem.dataset.sceneId = scene.id;
      sceneItem.tabIndex = 0;
      if (
        state.currentSession &&
        state.currentSession.selection &&
        state.currentSession.selection.chapterId === chapter.id &&
        state.currentSession.selection.sceneId === scene.id
      ) {
        sceneItem.classList.add("selected");
      }
      sceneItem.innerHTML = `${scene.title || "Cena"}${synopsis}`;
      sceneItem.addEventListener("click", () => handlers.onSelectScene(chapter.id, scene.id));
      sceneItem.addEventListener("keydown", (event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          handlers.onSelectScene(chapter.id, scene.id);
        }
      });
      scenes.appendChild(sceneItem);
    });
    chapterItem.appendChild(scenes);
    list.appendChild(chapterItem);
  });
  dom.structureTree.replaceChildren(list);
}

function renderScene(session) {
  const scene = session.scene;
  if (!scene) {
    dom.sceneSelectionLabel.textContent = "Nenhuma cena selecionada.";
    dom.sceneSynopsisInput.value = "";
    dom.sceneContentInput.value = "";
    dom.sceneTitleSelect.value = "";
    return;
  }

  const chapter = (session.project.chapters || []).find(
    (item) => item.id === session.selection.chapterId
  );
  dom.sceneSelectionLabel.textContent = `${chapter ? chapter.title : "Capitulo"} / ${scene.title || "Cena"}`;
  dom.sceneTitleSelect.value = `${session.selection.chapterId}::${session.selection.sceneId}`;
  dom.sceneSynopsisInput.value = scene.synopsis || "";
  dom.sceneContentInput.value = scene.content || "";
}

function renderSceneTitleSelect(session) {
  const select = dom.sceneTitleSelect;
  if (!select) {
    return;
  }

  select.innerHTML = "";

  if (!session || !session.project || !session.project.chapters || session.project.chapters.length === 0) {
    const option = document.createElement("option");
    option.value = "";
    option.textContent = "Nenhuma cena disponivel";
    option.selected = true;
    option.disabled = true;
    select.appendChild(option);
    select.disabled = true;
    return;
  }

  const placeholder = document.createElement("option");
  placeholder.value = "";
  placeholder.textContent = "Selecione uma cena";
  placeholder.disabled = true;
  select.appendChild(placeholder);

  session.project.chapters.forEach((chapter) => {
    (chapter.scenes || []).forEach((scene) => {
      const option = document.createElement("option");
      option.value = `${chapter.id}::${scene.id}`;
      option.textContent = `${chapter.title || "Capitulo"} / ${scene.title || "Cena"}`;
      select.appendChild(option);
    });
  });

  const selectedValue = session.selection
    ? `${session.selection.chapterId}::${session.selection.sceneId}`
    : "";
  select.value = selectedValue;
  select.disabled = false;
  if (!select.value) {
    placeholder.selected = true;
  }
}

function renderValidation(validation) {
  dom.validationList.innerHTML = "";
  if (!validation || !validation.issues || validation.issues.length === 0) {
    dom.validationBadge.textContent = "Sem bloqueios";
    dom.validationBadge.className = "badge badge-ok";
    const item = document.createElement("li");
    item.className = "validation-item validation-ok";
    item.textContent = "Nenhum problema de validacao para save.";
    dom.validationList.appendChild(item);
    return;
  }

  const hasBlocking = validation.issues.some((issue) => issue.severity === "BLOCKING");
  dom.validationBadge.textContent = hasBlocking ? "Com bloqueios" : "Com alertas";
  dom.validationBadge.className = hasBlocking ? "badge badge-warn" : "badge badge-neutral";
  validation.issues.forEach((issue) => {
    const item = document.createElement("li");
    item.className = `validation-item ${validationClassName(issue.severity)}`;
    item.textContent = `[${issue.severity}] ${issue.message}`;
    dom.validationList.appendChild(item);
  });
}

function validationClassName(severity) {
  if (severity === "BLOCKING") {
    return "validation-blocking";
  }
  if (severity === "WARNING") {
    return "validation-warning";
  }
  return "validation-info";
}

function renderCharacters(characters, characterSelection, handlers) {
  dom.characterCountBadge.textContent = `${characters.length} personagens`;
  if (!characters.length) {
    status.applyEmptyState(dom.characterList, "character-list", "Nenhum personagem carregado.");
    status.updateEmptyActionVisibility(dom.characterList, dom.characterEmptyAction, true);
    dom.characterSelectionLabel.textContent = "Nenhum personagem selecionado.";
    if (!state.characterDraftMode) {
      dom.characterNameInput.value = "";
      dom.characterDescriptionInput.value = "";
    }
    dom.characterScenesInfo.textContent = "0 cenas ligadas";
    dom.characterPointOfViewInfo.textContent = "Nao e o POV atual";
    return;
  }

  status.updateEmptyActionVisibility(dom.characterList, dom.characterEmptyAction, false);
  dom.characterList.className = "character-list";
  const list = document.createElement("div");
  list.className = "character-list-items";
  const selectedCharacter = characters.find((item) => {
    return characterSelection && item.id === characterSelection.characterId;
  }) || characters[0];

  characters.forEach((character) => {
    const button = document.createElement("button");
    button.className = "character-list-button list-item";
    if (selectedCharacter && selectedCharacter.id === character.id) {
      button.classList.add("selected");
    }
    button.textContent = character.name || "Personagem sem nome";
    button.addEventListener("click", () => handlers.onSelectCharacter(character.id));
    list.appendChild(button);
  });
  dom.characterList.replaceChildren(list);

  if (!state.characterDraftMode) {
    dom.characterSelectionLabel.textContent = selectedCharacter.name || "Personagem sem nome";
    dom.characterNameInput.value = selectedCharacter.name || "";
    dom.characterDescriptionInput.value = selectedCharacter.description || "";
    dom.characterScenesInfo.textContent = `${selectedCharacter.linkedSceneCount} cenas ligadas`;
    dom.characterPointOfViewInfo.textContent = selectedCharacter.selectedPointOfView
      ? "POV da cena atual"
      : "Nao e o POV atual";
  }
}

function renderTags(tags, tagSelection, handlers) {
  dom.tagCountBadge.textContent = `${tags.length} tags`;
  if (!tags.length) {
    status.applyEmptyState(dom.tagList, "character-list", "Nenhuma tag carregada.");
    status.updateEmptyActionVisibility(dom.tagList, dom.tagEmptyAction, true);
    dom.tagSelectionLabel.textContent = "Nenhuma tag selecionada.";
    if (!state.tagDraftMode) {
      dom.tagIdInput.value = "";
      dom.tagLabelInput.value = "";
      dom.tagTemplateInput.value = "";
    }
    dom.tagUsageInfo.textContent = "0 usos no manuscrito";
    return;
  }

  status.updateEmptyActionVisibility(dom.tagList, dom.tagEmptyAction, false);
  dom.tagList.className = "character-list";
  const list = document.createElement("div");
  list.className = "character-list-items";
  const selectedTag = tags.find((item) => {
    return tagSelection && item.id === tagSelection.tagId;
  }) || tags[0];

  tags.forEach((tag) => {
    const button = document.createElement("button");
    button.className = "character-list-button list-item";
    if (selectedTag && selectedTag.id === tag.id) {
      button.classList.add("selected");
    }
    button.textContent = `${tag.id} | ${tag.label || "Sem rotulo"}`;
    button.addEventListener("click", () => handlers.onSelectTag(tag.id));
    list.appendChild(button);
  });
  dom.tagList.replaceChildren(list);

  if (!state.tagDraftMode) {
    dom.tagSelectionLabel.textContent = selectedTag.label || selectedTag.id;
    dom.tagIdInput.value = selectedTag.id || "";
    dom.tagLabelInput.value = selectedTag.label || "";
    dom.tagTemplateInput.value = selectedTag.template || "";
    dom.tagUsageInfo.textContent = `${selectedTag.usageCount} usos no manuscrito`;
  }
}

function renderProfiles(profiles, profileSelection, tags, handlers) {
  if (!profiles.length) {
    status.applyEmptyState(dom.profileList, "character-list", "Nenhum perfil carregado.");
    status.updateEmptyActionVisibility(dom.profileList, dom.profileEmptyAction, true);
    dom.profileSelectionLabel.textContent = "Nenhum perfil selecionado.";
    if (!state.profileDraftMode) {
      dom.profilePrefixInput.value = "";
    }
    dom.profileTagsInfo.textContent = "Tags do perfil: nenhuma";
    return;
  }

  status.updateEmptyActionVisibility(dom.profileList, dom.profileEmptyAction, false);
  dom.profileList.className = "character-list";
  const list = document.createElement("div");
  list.className = "character-list-items";
  const selectedProfile = profiles.find((item) => {
    return profileSelection && item.characterId === profileSelection.characterId;
  }) || profiles[0];

  profiles.forEach((profile) => {
    const button = document.createElement("button");
    button.className = "character-list-button list-item";
    if (selectedProfile && selectedProfile.characterId === profile.characterId) {
      button.classList.add("selected");
    }
    button.textContent = profile.characterName || "Personagem";
    button.addEventListener("click", () => handlers.onSelectProfile(profile.characterId));
    list.appendChild(button);
  });
  dom.profileList.replaceChildren(list);

  if (!state.profileDraftMode) {
    dom.profileSelectionLabel.textContent = selectedProfile.characterName || "Personagem";
    dom.profilePrefixInput.value = selectedProfile.prefix || "";
    const tagMap = new Map((tags || []).map((tag) => [tag.id, tag.label || tag.id]));
    const tagLabels = (selectedProfile.preferredTagIds || []).map((id) => tagMap.get(id) || id);
    dom.profileTagsInfo.textContent = tagLabels.length
      ? `Tags do perfil: ${tagLabels.join(", ")}`
      : "Tags do perfil: nenhuma";
  }
}

function renderSearchResults(results, handlers) {
  if (!results || results.length === 0) {
    dom.searchCountBadge.textContent = "0 resultados";
    status.applyEmptyState(dom.searchResults, "search-results", "Nenhum resultado encontrado.");
    status.updateEmptyActionVisibility(dom.searchResults, dom.searchEmptyAction, true);
    return;
  }

  status.updateEmptyActionVisibility(dom.searchResults, dom.searchEmptyAction, false);
  dom.searchCountBadge.textContent = `${results.length} resultados`;
  dom.searchResults.className = "search-results";
  const list = document.createElement("div");
  list.className = "search-results-list";
  results.forEach((result) => {
    const button = document.createElement("button");
    button.className = "search-result-button list-item";
    button.textContent = `${utils.formatSearchTarget(result.target)}: ${result.title || "-"} | ${result.excerpt || ""}`;
    button.addEventListener("click", () => handlers.onNavigateSearch(result));
    list.appendChild(button);
  });
  dom.searchResults.replaceChildren(list);
}

function renderEmotionReport(report) {
  if (!report) {
    status.setInlineStatus(dom.analysisStatus, "Analise ainda nao executada.", "neutral");
    dom.analysisSentimentBadge.textContent = "Clima geral: sem analise";
    dom.analysisDominantBadge.textContent = "Emocao mais presente: sem analise";
    dom.analysisChunkBadge.textContent = "0 trechos avaliados";
    status.applyEmptyState(dom.analysisResults, "analysis-results", "Nenhum relatorio emocional carregado.");
    status.updateEmptyActionVisibility(dom.analysisResults, dom.analysisEmptyAction, true);
    return;
  }

  status.setInlineStatus(
    dom.analysisStatus,
    report.generatedAt ? `Analise gerada em ${report.generatedAt}` : "Analise gerada.",
    "success"
  );
  dom.analysisSentimentBadge.textContent = `Clima geral: ${utils.formatSentiment(report.overallSentiment)}`;
  dom.analysisDominantBadge.textContent = `Emocao mais presente: ${utils.formatEmotion(report.dominantEmotion)}`;
  dom.analysisChunkBadge.textContent = `${report.chunkCount} trechos avaliados`;

  status.updateEmptyActionVisibility(dom.analysisResults, dom.analysisEmptyAction, false);
  dom.analysisResults.className = "analysis-results";
  const container = document.createElement("div");
  container.className = "analysis-results-list";

  const scoreList = document.createElement("div");
  scoreList.className = "analysis-score-list";
  (report.averageScores || []).forEach((score) => {
    const item = document.createElement("div");
    item.className = "analysis-score";
    item.textContent = `${utils.formatEmotion(score.label)}: ${utils.formatScore(score.score)}`;
    scoreList.appendChild(item);
  });
  container.appendChild(scoreList);

  (report.chunks || []).forEach((chunk) => {
    const card = document.createElement("div");
    card.className = "analysis-chunk";
    const header = document.createElement("div");
    header.className = "analysis-chunk-header";
    header.textContent = `${chunk.chapterTitle || "Capitulo"} • ${chunk.sceneTitle || "Cena"} • ${utils.formatEmotion(chunk.dominantEmotion)}`;
    const excerpt = document.createElement("p");
    excerpt.textContent = chunk.excerpt || "";
    const footer = document.createElement("div");
    footer.className = "analysis-chunk-footer";
    footer.textContent = `${utils.formatSentiment(chunk.sentiment)} • ${chunk.wordCount} palavras`;
    card.appendChild(header);
    card.appendChild(excerpt);
    card.appendChild(footer);
    container.appendChild(card);
  });

  dom.analysisResults.replaceChildren(container);
}

function renderLocalProjects(projects, handlers) {
  dom.localProjects.innerHTML = "";
  if (!projects || !projects.length) {
    dom.localProjects.innerHTML = "<p class='subtle'>Nenhum projeto local encontrado.</p>";
    return;
  }

  const list = document.createElement("div");
  list.className = "local-project-list";
  projects.forEach((project) => {
    const row = document.createElement("div");
    row.className = "local-project-row";
    const openButton = document.createElement("button");
    openButton.className = "local-project-button list-item";
    openButton.textContent = project.label;
    openButton.addEventListener("click", () => handlers.onOpenLocalProject(project.path, project.label));
    const deleteButton = document.createElement("button");
    deleteButton.className = "local-project-delete btn-danger";
    deleteButton.textContent = "Excluir";
    deleteButton.addEventListener("click", () => handlers.onDeleteLocalProject(project.path, project.label));
    row.appendChild(openButton);
    row.appendChild(deleteButton);
    list.appendChild(row);
  });
  dom.localProjects.appendChild(list);
}

window.storyflameUI.render = {
  renderSession,
  renderStructure,
  renderScene,
  renderValidation,
  renderCharacters,
  renderTags,
  renderProfiles,
  renderSearchResults,
  renderEmotionReport,
  renderLocalProjects
};
})();
