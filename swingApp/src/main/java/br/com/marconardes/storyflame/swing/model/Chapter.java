package br.com.marconardes.storyflame.swing.model;

import java.util.UUID;

public class Chapter {
    private String id;
    private String title;
    private int order;
    private String content;
    private String summary;

    // Constructor
    public Chapter(String title, int order) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.order = order;
        this.content = "";
        this.summary = "";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return title; // For display in JList or similar components
    }
}
