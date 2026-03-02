package io.storyflame.core.archive;

public record ProjectManifest(
        String format,
        int version,
        String appVersion,
        String createdAt
) {
    public static ProjectManifest initial(String createdAt) {
        return new ProjectManifest("storyflame-zip", ProjectArchiveLayout.SPEC_VERSION_NUMBER, "0.2.0-SNAPSHOT", createdAt);
    }

    public static ProjectManifest legacy(String createdAt) {
        return new ProjectManifest("storyflame-zip", 0, "0.0.0-legacy", createdAt);
    }
}
