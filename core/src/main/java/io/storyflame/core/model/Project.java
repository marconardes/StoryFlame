package io.storyflame.core.model;

import io.storyflame.core.analysis.EmotionAnalysisReport;
import io.storyflame.core.analysis.EmotionCache;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Project {
    private String id;
    private String title;
    private String author;
    private Instant createdAt;
    private Instant updatedAt;
    private final List<Chapter> chapters;
    private final List<Character> characters;
    private final List<NarrativeTag> narrativeTags;
    private final List<CharacterTagProfile> characterTagProfiles;
    private EmotionAnalysisReport emotionAnalysis;
    private EmotionCache emotionCache;

    public Project() {
        this(
                UUID.randomUUID().toString(),
                "",
                "",
                Instant.now(),
                Instant.now(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                new EmotionCache()
        );
    }

    public Project(
            String id,
            String title,
            String author,
            Instant createdAt,
            Instant updatedAt,
            List<Chapter> chapters,
            List<Character> characters,
            List<NarrativeTag> narrativeTags,
            List<CharacterTagProfile> characterTagProfiles,
            EmotionAnalysisReport emotionAnalysis,
            EmotionCache emotionCache
    ) {
        Instant now = Instant.now();
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID().toString());
        this.title = Objects.requireNonNullElse(title, "");
        this.author = Objects.requireNonNullElse(author, "");
        this.createdAt = Objects.requireNonNullElse(createdAt, now);
        this.updatedAt = Objects.requireNonNullElse(updatedAt, this.createdAt);
        this.chapters = new ArrayList<>(Objects.requireNonNullElse(chapters, List.of()));
        this.characters = new ArrayList<>(Objects.requireNonNullElse(characters, List.of()));
        this.narrativeTags = new ArrayList<>(Objects.requireNonNullElse(narrativeTags, List.of()));
        this.characterTagProfiles = new ArrayList<>(Objects.requireNonNullElse(characterTagProfiles, List.of()));
        this.emotionAnalysis = emotionAnalysis;
        this.emotionCache = Objects.requireNonNullElse(emotionCache, new EmotionCache());
    }

    public static Project blank(String title, String author) {
        return new Project(UUID.randomUUID().toString(), title, author, Instant.now(), Instant.now(), List.of(), List.of(), List.of(), List.of(), null, new EmotionCache());
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
        touch();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = Objects.requireNonNullElse(author, "");
        touch();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = Objects.requireNonNullElse(createdAt, this.createdAt);
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Objects.requireNonNullElse(updatedAt, this.updatedAt);
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public List<NarrativeTag> getNarrativeTags() {
        return narrativeTags;
    }

    public List<CharacterTagProfile> getCharacterTagProfiles() {
        return characterTagProfiles;
    }

    public EmotionAnalysisReport getEmotionAnalysis() {
        return emotionAnalysis;
    }

    public void setEmotionAnalysis(EmotionAnalysisReport emotionAnalysis) {
        this.emotionAnalysis = emotionAnalysis;
    }

    public EmotionCache getEmotionCache() {
        return emotionCache;
    }

    public void setEmotionCache(EmotionCache emotionCache) {
        this.emotionCache = Objects.requireNonNullElse(emotionCache, new EmotionCache());
    }

    public void touch() {
        updatedAt = Instant.now();
    }
}
