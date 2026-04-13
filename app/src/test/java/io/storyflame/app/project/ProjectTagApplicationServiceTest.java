package io.storyflame.app.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProjectTagApplicationServiceTest {
    private final ProjectTagApplicationService service = new ProjectTagApplicationService();

    @Test
    void createUpdateDeleteAndDuplicateTag() {
        Project project = Project.blank("Projeto", "Autor");

        NarrativeTag created = service.createTag(project, "test1", "Tag", "tpl");
        NarrativeTag updated = service.updateTag(project, created, "test2", "Nova", "tpl2");
        NarrativeTag copy = service.duplicateTag(project, updated, "test3");
        NarrativeTag nextSelected = service.deleteTag(project, updated);

        assertEquals("test3", project.getNarrativeTags().get(0).id());
        assertEquals("test3", copy.id());
        assertNotNull(nextSelected);
        assertEquals(1, project.getNarrativeTags().size());
    }

    @Test
    void updateTagMigratesProfileAndSceneReferences() {
        Project project = Project.blank("Projeto", "Autor");
        NarrativeTag created = service.createTag(project, "test1", "Tag", "tpl");
        CharacterTagProfile profile = new CharacterTagProfile("char1", "", List.of("test1"));
        project.getCharacterTagProfiles().add(profile);
        project.getChapters().add(new io.storyflame.core.model.Chapter(null, "Cap", List.of(new Scene(null, "Cena", "", "{test1}", null))));

        service.updateTag(project, created, "test2", "Nova", "tpl2");

        assertEquals("test2", profile.getPreferredTagIds().get(0));
        assertTrue(project.getChapters().get(0).getScenes().get(0).getContent().contains("{test2}"));
    }

    @Test
    void addAndRemoveTagFromProfile() {
        Project project = Project.blank("Projeto", "Autor");
        CharacterTagProfile profile = new CharacterTagProfile("char1", "", List.of());
        NarrativeTag tag = new NarrativeTag("test1", "Tag", "", "tpl");

        service.addTagToProfile(project, profile, tag);
        assertFalse(profile.getPreferredTagIds().isEmpty());

        service.removeTagFromProfile(project, profile, tag);
        assertTrue(profile.getPreferredTagIds().isEmpty());
    }
}
