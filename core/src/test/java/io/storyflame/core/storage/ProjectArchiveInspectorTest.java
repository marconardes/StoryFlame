package io.storyflame.core.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.archive.ProjectArchiveLayout;
import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectArchiveInspectorTest {
    @TempDir
    Path tempDir;

    @Test
    void flagsArchiveWithoutProjectDocumentAsInvalid() throws Exception {
        Path archive = tempDir.resolve("missing-project.storyflame");
        writeArchive(archive, Map.of(
                ProjectArchiveLayout.MANIFEST_FILE, """
                        {"format":"storyflame-zip","version":1,"appVersion":"0.2.0-SNAPSHOT","createdAt":"2026-03-29T00:00:00Z"}
                        """,
                ProjectArchiveLayout.NARRATIVE_TAGS_FILE, "[]",
                ProjectArchiveLayout.CHARACTER_TAG_PROFILES_FILE, "[]"
        ));

        ProjectArchiveInspection inspection = new ProjectArchiveInspector().inspect(archive);

        assertFalse(inspection.valid());
        assertTrue(inspection.issues().contains("Pacote sem project.json"));
    }

    @Test
    void flagsUnsupportedManifestVersionAsInvalid() throws Exception {
        Path archive = tempDir.resolve("future-version.storyflame");
        writeArchive(archive, Map.of(
                ProjectArchiveLayout.MANIFEST_FILE, """
                        {"format":"storyflame-zip","version":99,"appVersion":"9.9.9","createdAt":"2026-03-29T00:00:00Z"}
                        """,
                ProjectArchiveLayout.PROJECT_FILE, """
                        {"id":"project-1","title":"Livro","author":"Ana","createdAt":"2026-03-29T00:00:00Z","updatedAt":"2026-03-29T00:00:00Z","chapterIds":[],"characterIds":[]}
                        """,
                ProjectArchiveLayout.NARRATIVE_TAGS_FILE, "[]",
                ProjectArchiveLayout.CHARACTER_TAG_PROFILES_FILE, "[]"
        ));

        ProjectArchiveInspection inspection = new ProjectArchiveInspector().inspect(archive);

        assertFalse(inspection.valid());
        assertEquals(99, inspection.detectedVersion());
        assertTrue(inspection.issues().contains("Versao do pacote nao suportada: 99"));
    }

    @Test
    void rejectsMalformedManifestJson() throws Exception {
        Path archive = tempDir.resolve("broken-manifest.storyflame");
        writeArchive(archive, Map.of(
                ProjectArchiveLayout.MANIFEST_FILE, "{\"format\":",
                ProjectArchiveLayout.PROJECT_FILE, """
                        {"id":"project-1","title":"Livro","author":"Ana","createdAt":"2026-03-29T00:00:00Z","updatedAt":"2026-03-29T00:00:00Z","chapterIds":[],"characterIds":[]}
                        """,
                ProjectArchiveLayout.NARRATIVE_TAGS_FILE, "[]",
                ProjectArchiveLayout.CHARACTER_TAG_PROFILES_FILE, "[]"
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new ProjectArchiveInspector().inspect(archive)
        );

        assertTrue(exception.getMessage().contains("Invalid manifest.json"));
    }

    private void writeArchive(Path archive, Map<String, String> fileEntries) throws Exception {
        try (ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(archive)))) {
            output.putNextEntry(new ZipEntry(ProjectArchiveLayout.CHAPTERS_DIRECTORY));
            output.closeEntry();
            output.putNextEntry(new ZipEntry(ProjectArchiveLayout.CHARACTERS_DIRECTORY));
            output.closeEntry();
            for (Map.Entry<String, String> entry : fileEntries.entrySet()) {
                output.putNextEntry(new ZipEntry(entry.getKey()));
                output.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                output.closeEntry();
            }
        }
    }
}
