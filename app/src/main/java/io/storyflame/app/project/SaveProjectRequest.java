package io.storyflame.app.project;

import io.storyflame.core.model.Project;
import java.nio.file.Path;
import java.util.Objects;

public record SaveProjectRequest(Project project, Path path) {
    public SaveProjectRequest {
        project = Objects.requireNonNull(project);
    }
}
