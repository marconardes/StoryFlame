package io.storyflame.core.publication;

import io.storyflame.core.model.Project;
import java.nio.file.Path;
import java.util.Objects;

public record ProjectPublicationRequest(
        Project project,
        PublicationFormat format,
        Path targetPath
) {
    public ProjectPublicationRequest {
        Objects.requireNonNull(project);
        Objects.requireNonNull(format);
        Objects.requireNonNull(targetPath);
    }
}
