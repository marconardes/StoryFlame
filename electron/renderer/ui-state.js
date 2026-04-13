window.storyflameUI = window.storyflameUI || {};

(() => {
window.storyflameUI.state = {
  currentSession: null,
  activeTab: "editor",
  characterDraftMode: false,
  tagDraftMode: false,
  searchTimer: null,
  profileDraftMode: false,
  isBusy: false,
  isDirty: false,
  autosaveTimer: null
};
})();
