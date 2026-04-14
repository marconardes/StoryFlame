package io.storyflame.app.project;

import java.nio.file.Path;
import java.util.Objects;

public record PublicationOperationResult(Path path, ProjectValidationDto validation, String message) {
    public PublicationOperationResult {
        path = Objects.requireNonNull(path);
        validation = Objects.requireNonNull(validation);
        message = Objects.requireNonNullElse(message, "");
    }
}
