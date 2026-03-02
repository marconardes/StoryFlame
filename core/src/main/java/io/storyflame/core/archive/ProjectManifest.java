package io.storyflame.core.archive;

public record ProjectManifest(
        String format,
        int version,
        String appVersion,
        String createdAt
) {
    public static ProjectManifest initial(String createdAt) {
        return new ProjectManifest("storyflame-zip", 1, "0.2.0-SNAPSHOT", createdAt);
    }
}

