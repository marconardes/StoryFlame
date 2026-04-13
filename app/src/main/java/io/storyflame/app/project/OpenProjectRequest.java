package io.storyflame.app.project;

import java.nio.file.Path;
import java.util.Objects;

public record OpenProjectRequest(Path path) {
    public OpenProjectRequest {
        path = Objects.requireNonNull(path);
    }
}
