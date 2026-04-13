package io.storyflame.core.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.storage.ProjectArchiveStore;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NarrativeTagCatalogTest {
    @TempDir
    Path tempDir;

    @Test
    void defaultCatalogIncludesStandardDialogueTags() {
        NarrativeTagCatalog catalog = NarrativeTagCatalog.defaultCatalog();

        assertDialogueTag(catalog, "fala1", "disse");
        assertDialogueTag(catalog, "perg1", "perguntou");
        assertDialogueTag(catalog, "suss1", "sussurrou");
        assertDialogueTag(catalog, "grit1", "gritou");
        assertDialogueTag(catalog, "murm1", "murmurou");
        assertDialogueTag(catalog, "intr1", "interrompeu");
    }

    @Test
    void createProjectSeedsDialogueTagsInNewProjects() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);

        Set<String> tagIds = store.createProject("Dialogos", "Autor").getNarrativeTags().stream()
                .map(NarrativeTag::id)
                .collect(Collectors.toSet());

        assertTrue(tagIds.containsAll(Set.of("fala1", "perg1", "suss1", "grit1", "murm1", "intr1")));
    }

    private static void assertDialogueTag(NarrativeTagCatalog catalog, String tagId, String expectedTemplate) {
        NarrativeTag tag = catalog.resolve(tagId);
        assertNotNull(tag);
        assertEquals(expectedTemplate, tag.template());
    }
}
