package br.com.marconardes.storyflame.swing.model;

import java.util.UUID;

public class Character {
    private String id;
    private String name;
    private String nickname;
    private String description; // Physical appearance, etc.
    private String history;     // Backstory
    private String traits;      // Personality, skills, flaws
    private String relationships; // Connections to other characters or entities
    private String notes;       // Miscellaneous information

    // Constructor
    public Character(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.nickname = "";
        this.description = "";
        this.history = "";
        this.traits = "";
        this.relationships = "";
        this.notes = "";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getTraits() {
        return traits;
    }

    public void setTraits(String traits) {
        this.traits = traits;
    }

    public String getRelationships() {
        return relationships;
    }

    public void setRelationships(String relationships) {
        this.relationships = relationships;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return name != null ? name : "Unnamed Character"; // For display in JList or similar components
    }
}
