package br.com.marconardes.storyflame.swing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class Project {
    private String id;
    private String name;
    private String creationDate;
    private List<Chapter> chapters;
    private Map<String, Integer> dailyWordCounts = new HashMap<>();
    private int dailyWritingGoal = 0;
    private int totalWritingGoal = 0;

    // Constructor
    public Project(String name, String creationDate) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.creationDate = creationDate;
        this.chapters = new ArrayList<>();
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

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    // Helper method to add a chapter
    public void addChapter(Chapter chapter) {
        this.chapters.add(chapter);
    }

    // Helper method to remove a chapter
    public void removeChapter(Chapter chapter) {
        this.chapters.removeIf(c -> c.getId().equals(chapter.getId()));
    }

    public Map<String, Integer> getDailyWordCounts() {
        return dailyWordCounts;
    }

    public void setDailyWordCounts(Map<String, Integer> dailyWordCounts) {
        this.dailyWordCounts = dailyWordCounts;
    }

    public int getDailyWritingGoal() {
        return dailyWritingGoal;
    }

    public void setDailyWritingGoal(int dailyWritingGoal) {
        this.dailyWritingGoal = dailyWritingGoal;
    }

    public int getTotalWritingGoal() {
        return totalWritingGoal;
    }

    public void setTotalWritingGoal(int totalWritingGoal) {
        this.totalWritingGoal = totalWritingGoal;
    }

    @Override
    public String toString() {
        return name; // For display in JList or similar components
    }
}
