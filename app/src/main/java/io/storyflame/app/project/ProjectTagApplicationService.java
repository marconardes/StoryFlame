package io.storyflame.app.project;

import io.storyflame.core.model.Project;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import java.util.Objects;

public final class ProjectTagApplicationService {
    public NarrativeTag updateTag(Project project, NarrativeTag selectedTag, String updatedId, String label, String template) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedTag);
        NarrativeTag updatedTag = new NarrativeTag(updatedId, label, selectedTag.description(), template);
        int index = project.getNarrativeTags().indexOf(selectedTag);
        if (index >= 0) {
            if (!updatedId.equals(selectedTag.id())) {
                migrateTagIdReferences(project, selectedTag.id(), updatedId);
            }
            project.getNarrativeTags().set(index, updatedTag);
            project.touch();
        }
        return updatedTag;
    }

    public NarrativeTag createTag(Project project, String id, String label, String template) {
        Objects.requireNonNull(project);
        NarrativeTag newTag = new NarrativeTag(id, label, "", template);
        project.getNarrativeTags().add(newTag);
        project.touch();
        return newTag;
    }

    public NarrativeTag deleteTag(Project project, NarrativeTag selectedTag) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedTag);
        project.getNarrativeTags().remove(selectedTag);
        for (CharacterTagProfile profile : project.getCharacterTagProfiles()) {
            profile.getPreferredTagIds().removeIf(tagId -> tagId.equals(selectedTag.id()));
        }
        project.touch();
        return project.getNarrativeTags().isEmpty() ? null : project.getNarrativeTags().get(0);
    }

    public NarrativeTag duplicateTag(Project project, NarrativeTag selectedTag, String newId) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedTag);
        NarrativeTag copy = new NarrativeTag(newId, selectedTag.label(), selectedTag.description(), selectedTag.template());
        project.getNarrativeTags().add(copy);
        project.touch();
        return copy;
    }

    public CharacterTagProfile addTagToProfile(Project project, CharacterTagProfile selectedProfile, NarrativeTag selectedTag) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedProfile);
        Objects.requireNonNull(selectedTag);
        if (!selectedProfile.getPreferredTagIds().contains(selectedTag.id())) {
            selectedProfile.getPreferredTagIds().add(selectedTag.id());
            project.touch();
        }
        return selectedProfile;
    }

    public CharacterTagProfile removeTagFromProfile(Project project, CharacterTagProfile selectedProfile, NarrativeTag selectedTag) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(selectedProfile);
        Objects.requireNonNull(selectedTag);
        selectedProfile.getPreferredTagIds().removeIf(tagId -> tagId.equals(selectedTag.id()));
        project.touch();
        return selectedProfile;
    }

    private void migrateTagIdReferences(Project project, String oldId, String newId) {
        String oldToken = "{" + oldId + "}";
        String newToken = "{" + newId + "}";
        project.getCharacterTagProfiles().forEach(profile -> {
            for (int i = 0; i < profile.getPreferredTagIds().size(); i++) {
                if (profile.getPreferredTagIds().get(i).equals(oldId)) {
                    profile.getPreferredTagIds().set(i, newId);
                }
            }
        });
        project.getChapters().forEach(chapter ->
                chapter.getScenes().forEach(scene ->
                        scene.setContent(scene.getContent().replace(oldToken, newToken))));
    }
}
