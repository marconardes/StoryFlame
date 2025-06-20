package br.com.marconardes.storyflame.swing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import br.com.marconardes.storyflame.swing.model.Character;

public class Project {
    private String id;
    private String name;
    private String creationDate;
    private List<Chapter> chapters;
    private List<Character> characters;
    private Map<String, Integer> dailyWordCounts = new HashMap<>();
    private int dailyWritingGoal = 0;
    private int totalWritingGoal = 0;
    private String passwordHash;

    // Constructor
    public Project(String name, String creationDate) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.creationDate = creationDate;
        this.chapters = new ArrayList<>();
        this.characters = new ArrayList<>();
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

    public List<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(List<Character> characters) {
        this.characters = characters;
    }

    // Helper method to add a chapter
    public void addChapter(Chapter chapter) {
        this.chapters.add(chapter);
    }

    // Helper method to remove a chapter
    public void removeChapter(Chapter chapter) {
        this.chapters.removeIf(c -> c.getId().equals(chapter.getId()));
    }

    public void addCharacter(Character character) {
        if (this.characters == null) {
            this.characters = new ArrayList<>();
        }
        this.characters.add(character);
    }

    public void removeCharacter(Character character) {
        if (this.characters != null && character != null) {
            this.characters.removeIf(c -> c.getId().equals(character.getId()));
        }
    }

    public void updateCharacter(Character updatedCharacter) {
        if (this.characters != null && updatedCharacter != null) {
            for (int i = 0; i < this.characters.size(); i++) {
                if (this.characters.get(i).getId().equals(updatedCharacter.getId())) {
                    this.characters.set(i, updatedCharacter);
                    return;
                }
            }
        }
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return name; // For display in JList or similar components
    }
}
