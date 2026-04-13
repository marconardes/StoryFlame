package io.storyflame.core.analysis;

import java.util.ArrayList;
import java.util.List;

public final class EmotionCache {
    private final List<EmotionCacheEntry> entries;

    public EmotionCache() {
        this.entries = new ArrayList<>();
    }

    public EmotionCache(List<EmotionCacheEntry> entries) {
        this.entries = new ArrayList<>(entries == null ? List.of() : entries);
    }

    public List<EmotionCacheEntry> getEntries() {
        return entries;
    }

    public EmotionChunkAnalysis find(String sceneId, String contentHash) {
        for (EmotionCacheEntry entry : entries) {
            if (entry.sceneId().equals(sceneId) && entry.contentHash().equals(contentHash)) {
                return entry.analysis();
            }
        }
        return null;
    }

    public void put(String sceneId, String contentHash, EmotionChunkAnalysis analysis) {
        entries.removeIf(entry -> entry.sceneId().equals(sceneId));
        entries.add(new EmotionCacheEntry(sceneId, contentHash, analysis));
    }
}
