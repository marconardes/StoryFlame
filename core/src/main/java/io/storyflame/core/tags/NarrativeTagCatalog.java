package io.storyflame.core.tags;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class NarrativeTagCatalog {
    private final Map<String, NarrativeTag> tagsById;

    public NarrativeTagCatalog(Collection<NarrativeTag> tags) {
        this.tagsById = new LinkedHashMap<>();
        Collection<NarrativeTag> source = tags == null ? java.util.List.of() : tags;
        for (NarrativeTag tag : source) {
            tagsById.put(tag.id(), tag);
        }
    }

    public static NarrativeTagCatalog defaultCatalog() {
        return new NarrativeTagCatalog(java.util.List.of(
                new NarrativeTag("lfp1", "Leitura de face 1", "Primeira variacao de leitura facial e percepcao."),
                new NarrativeTag("lfp2", "Leitura de face 2", "Segunda variacao de leitura facial e percepcao."),
                new NarrativeTag("emo1", "Emocao 1", "Marcador emocional base."),
                new NarrativeTag("close1", "Close narrativo", "Aproxima o foco narrativo do detalhe."),
                new NarrativeTag("beat1", "Beat de ritmo", "Pausa curta para marcar ritmo de cena.")
        ));
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
