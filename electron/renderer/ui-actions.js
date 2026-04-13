window.storyflameUI = window.storyflameUI || {};

(() => {
const coreActions = window.storyflameUI.coreActions || {};
const projectActions = window.storyflameUI.projectActions || {};
const entityActions = window.storyflameUI.entityActions || {};
const searchActions = window.storyflameUI.searchActions || {};

window.storyflameUI.actions = {
  ...coreActions,
  ...projectActions,
  ...entityActions,
  ...searchActions
};
})();
