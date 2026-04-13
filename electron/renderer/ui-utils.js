window.storyflameUI = window.storyflameUI || {};

(() => {
function formatScore(value) {
  if (typeof value !== "number") {
    return "0.00";
  }
  return value.toFixed(2);
}

function formatEmotion(value) {
  if (!value) {
    return "Neutro";
  }
  const map = {
    JOY: "Alegria",
    SADNESS: "Tristeza",
    ANGER: "Raiva",
    FEAR: "Medo",
    TENSION: "Tensao",
    CALM: "Calma",
    NEUTRAL: "Neutro"
  };
  return map[value] || value;
}

function formatSentiment(value) {
  if (!value) {
    return "Neutro";
  }
  const map = {
    POSITIVE: "Positivo",
    NEGATIVE: "Negativo",
    NEUTRAL: "Neutro"
  };
  return map[value] || value;
}

function formatSearchTarget(target) {
  if (target === "CHAPTER") {
    return "Capitulo";
  }
  if (target === "SCENE_TITLE") {
    return "Cena";
  }
  return "Texto";
}

function formatTime(date) {
  return date.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}

function extractTagToken(text, caretPos) {
  if (!text || caretPos <= 0) {
    return null;
  }
  const start = text.lastIndexOf("{", caretPos - 1);
  if (start < 0) {
    return null;
  }
  const end = text.indexOf("}", start + 1);
  if (end !== -1 && end < caretPos) {
    return null;
  }
  const token = text.slice(start, caretPos);
  if (!token.startsWith("{")) {
    return null;
  }
  return { query: token.slice(1), start, end: caretPos };
}

function buildTagSuggestions(session, query) {
  if (!session || !session.project) {
    return [];
  }
  const normalized = (query || "").trim().toLowerCase();
  const tags = session.project.tags || session.project.narrativeTags || [];
  return tags
    .filter((tag) => {
      if (!normalized) {
        return true;
      }
      const idMatch = (tag.id || "").toLowerCase().includes(normalized);
      const labelMatch = (tag.label || "").toLowerCase().includes(normalized);
      return idMatch || labelMatch;
    })
    .slice(0, 6);
}

window.storyflameUI.utils = {
  formatScore,
  formatEmotion,
  formatSentiment,
  formatSearchTarget,
  formatTime,
  extractTagToken,
  buildTagSuggestions
};
})();
