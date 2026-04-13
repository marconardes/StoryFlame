const { contextBridge, ipcRenderer } = require("electron");

async function bridgeFetch(route, options = {}) {
  const port = await ipcRenderer.invoke("storyflame:get-bridge-port");
  if (!port) {
    throw new Error("Bridge local indisponivel.");
  }
  const response = await fetch(`http://127.0.0.1:${port}${route}`, {
    headers: {
      "Content-Type": "application/json"
    },
    ...options
  });
  const payload = await response.json();
  if (!response.ok || payload.status === "error") {
    const detail = payload.errors && payload.errors.length ? payload.errors.join("\n") : payload.message;
    throw new Error(detail || "Falha na operacao do StoryFlame.");
  }
  return payload.data;
}

contextBridge.exposeInMainWorld("storyflame", {
  session: {
    current() {
      return bridgeFetch("/api/session");
    },
    updateMetadata(title, author) {
      return bridgeFetch("/api/session/metadata", {
        method: "POST",
        body: JSON.stringify({ title, author })
      });
    },
    selectScene(chapterId, sceneId) {
      return bridgeFetch("/api/session/select-scene", {
        method: "POST",
        body: JSON.stringify({ chapterId, sceneId })
      });
    },
    updateScene(title, synopsis, content) {
      return bridgeFetch("/api/session/scene", {
        method: "POST",
        body: JSON.stringify({ title, synopsis, content })
      });
    }
  },
  characters: {
    select(characterId) {
      return bridgeFetch("/api/characters/select", {
        method: "POST",
        body: JSON.stringify({ characterId })
      });
    },
    create(name, description) {
      return bridgeFetch("/api/characters/create", {
        method: "POST",
        body: JSON.stringify({ name, description })
      });
    },
    update(name, description) {
      return bridgeFetch("/api/characters/update", {
        method: "POST",
        body: JSON.stringify({ name, description })
      });
    },
    remove() {
      return bridgeFetch("/api/characters/delete", {
        method: "POST",
        body: JSON.stringify({})
      });
    }
  },
  tags: {
    select(tagId) {
      return bridgeFetch("/api/tags/select", {
        method: "POST",
        body: JSON.stringify({ tagId })
      });
    },
    create(id, label, template) {
      return bridgeFetch("/api/tags/create", {
        method: "POST",
        body: JSON.stringify({ id, label, template })
      });
    },
    update(id, label, template) {
      return bridgeFetch("/api/tags/update", {
        method: "POST",
        body: JSON.stringify({ id, label, template })
      });
    },
    remove() {
      return bridgeFetch("/api/tags/delete", {
        method: "POST",
        body: JSON.stringify({})
      });
    }
  },
  search: {
    run(query) {
      return bridgeFetch("/api/search/run", {
        method: "POST",
        body: JSON.stringify({ query })
      });
    }
  },
  analysis: {
    runEmotion() {
      return bridgeFetch("/api/analysis/emotion/run", {
        method: "POST",
        body: JSON.stringify({})
      });
    },
    currentEmotion() {
      return bridgeFetch("/api/analysis/emotion/current");
    }
  },
  profiles: {
    select(characterId) {
      return bridgeFetch("/api/profiles/select", {
        method: "POST",
        body: JSON.stringify({ characterId })
      });
    },
    updatePrefix(prefix) {
      return bridgeFetch("/api/profiles/prefix", {
        method: "POST",
        body: JSON.stringify({ prefix })
      });
    },
    addTag(tagId) {
      return bridgeFetch("/api/profiles/tags/add", {
        method: "POST",
        body: JSON.stringify({ tagId })
      });
    },
    removeTag(tagId) {
      return bridgeFetch("/api/profiles/tags/remove", {
        method: "POST",
        body: JSON.stringify({ tagId })
      });
    }
  },
  structure: {
    addChapter() {
      return bridgeFetch("/api/structure/chapter/add", {
        method: "POST",
        body: JSON.stringify({})
      });
    },
    removeChapter() {
      return bridgeFetch("/api/structure/chapter/remove", {
        method: "POST",
        body: JSON.stringify({})
      });
    },
    moveChapter(offset) {
      return bridgeFetch("/api/structure/chapter/move", {
        method: "POST",
        body: JSON.stringify({ offset })
      });
    },
    addScene() {
      return bridgeFetch("/api/structure/scene/add", {
        method: "POST",
        body: JSON.stringify({})
      });
    },
    removeScene() {
      return bridgeFetch("/api/structure/scene/remove", {
        method: "POST",
        body: JSON.stringify({})
      });
    },
    moveScene(offset) {
      return bridgeFetch("/api/structure/scene/move", {
        method: "POST",
        body: JSON.stringify({ offset })
      });
    }
  },
  projects: {
    local() {
      return bridgeFetch("/api/projects/local");
    },
    create(title, author) {
      return bridgeFetch("/api/projects/create", {
        method: "POST",
        body: JSON.stringify({ title, author })
      });
    },
    open(path) {
      return bridgeFetch("/api/projects/open", {
        method: "POST",
        body: JSON.stringify({ path })
      });
    },
    save() {
      return bridgeFetch("/api/projects/save", {
        method: "POST",
        body: JSON.stringify({})
      });
    },
    remove(path) {
      return bridgeFetch("/api/projects/delete", {
        method: "POST",
        body: JSON.stringify({ path })
      });
    },
    async pickArchiveAndOpen() {
      const path = await ipcRenderer.invoke("storyflame:open-project-dialog");
      if (!path) {
        return null;
      }
      return this.open(path);
    }
  },
  bridge: {
    baseDirectory() {
      return bridgeFetch("/api/base-directory");
    }
  }
});
