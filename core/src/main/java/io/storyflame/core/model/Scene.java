package io.storyflame.core.model;

import java.util.Objects;
import java.util.UUID;

public final class Scene {
    private String id;
    private String title;
    private String content;
    private String pointOfViewCharacterId;

    public Scene() {
        this(UUID.randomUUID().toString(), "", "", null);
    }

    public Scene(String id, String title, String content, String pointOfViewCharacterId) {
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID().toString());
        this.title = Objects.requireNonNullElse(title, "");
        this.content = Objects.requireNonNullElse(content, "");
        this.pointOfViewCharacterId = pointOfViewCharacterId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = Objects.requireNonNullElse(content, "");
    }

    public String getPointOfViewCharacterId() {
        return pointOfViewCharacterId;
    }

    public void setPointOfViewCharacterId(String pointOfViewCharacterId) {
        this.pointOfViewCharacterId = pointOfViewCharacterId;
    }
}

