package io.storyflame.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Chapter {
    private String id;
    private String title;
    private final List<Scene> scenes;

    public Chapter() {
        this(UUID.randomUUID().toString(), "", new ArrayList<>());
    }

    public Chapter(String id, String title, List<Scene> scenes) {
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID().toString());
        this.title = Objects.requireNonNullElse(title, "");
        this.scenes = new ArrayList<>(Objects.requireNonNullElse(scenes, List.of()));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNullElse(id, this.id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNullElse(title, "");
    }

    public List<Scene> getScenes() {
        return scenes;
    }
}

