package io.storyflame.core.tags;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NarrativeTagCatalog {
    private static final String DEFAULT_RESOURCE = "/narrative_tags.json";
    private final Map<String, NarrativeTag> tagsById;

    public NarrativeTagCatalog(Collection<NarrativeTag> tags) {
        this.tagsById = new LinkedHashMap<>();
        Collection<NarrativeTag> source = tags == null ? java.util.List.of() : tags;
        for (NarrativeTag tag : source) {
            tagsById.put(tag.id(), tag);
        }
    }

    public static NarrativeTagCatalog defaultCatalog() {
        try (Reader reader = new InputStreamReader(
                NarrativeTagCatalog.class.getResourceAsStream(DEFAULT_RESOURCE),
                StandardCharsets.UTF_8
        )) {
            Gson gson = new GsonBuilder().create();
            NarrativeTag[] tags = gson.fromJson(reader, NarrativeTag[].class);
            return new NarrativeTagCatalog(tags == null ? java.util.List.of() : java.util.List.of(tags));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load default narrative tag catalog", exception);
        }
    }

    public boolean contains(String tagId) {
        return resolve(tagId) != null;
    }

    public NarrativeTag resolve(String tagId) {
        if (tagId == null || tagId.isBlank()) {
            return null;
        }
        return tagsById.get(tagId.trim().toLowerCase());
    }

    public Collection<NarrativeTag> all() {
        return java.util.List.copyOf(tagsById.values());
    }
}
