package io.storyflame.core.model;

import java.util.Objects;
import java.util.UUID;

public final class Character {
    private String id;
    private String name;
    private String description;

    public Character() {
        this(UUID.randomUUID().toString(), "", "");
    }

    public Character(String id, String name, String description) {
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID().toString());
        this.name = Objects.requireNonNullElse(name, "");
        this.description = Objects.requireNonNullElse(description, "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNullElse(id, this.id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
    }
}

