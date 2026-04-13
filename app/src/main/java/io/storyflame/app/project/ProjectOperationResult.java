package io.storyflame.app.project;

import io.storyflame.core.model.Project;
import java.nio.file.Path;
import java.util.Objects;

public record ProjectOperationResult(Project project, Path path, ProjectValidationDto validation, String message) {
    public ProjectOperationResult {
        project = Objects.requireNonNull(project);
        path = Objects.requireNonNull(path);
        validation = Objects.requireNonNull(validation);
        message = Objects.requireNonNullElse(message, "");
    }
}
