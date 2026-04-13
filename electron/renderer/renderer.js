const ui = window.storyflameUI;

if (!ui || !ui.app || typeof ui.app.bootstrap !== "function") {
  throw new Error("StoryFlame UI bootstrap indisponivel.");
}

ui.app.bootstrap();
