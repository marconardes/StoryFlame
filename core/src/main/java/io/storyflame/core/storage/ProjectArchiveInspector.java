package io.storyflame.core.storage;

import com.google.gson.Gson;
import io.storyflame.core.archive.ProjectArchiveLayout;
import io.storyflame.core.archive.ProjectManifest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ProjectArchiveInspector {
    private final Gson gson;

    public ProjectArchiveInspector() {
        this.gson = JsonMapperFactory.create();
    }

    public ProjectArchiveInspection inspect(Path path) {
        Set<String> entryNames = new HashSet<>();
        List<String> issues = new ArrayList<>();
        ProjectManifest manifest = null;
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entryNames.add(entry.getName());
                if (ProjectArchiveLayout.MANIFEST_FILE.equals(entry.getName())) {
                    manifest = readManifest(zip);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to inspect project archive: " + path, exception);
        }

        if (!entryNames.contains(ProjectArchiveLayout.PROJECT_FILE)) {
            issues.add("Pacote sem project.json");
        }
        if (!entryNames.contains(ProjectArchiveLayout.NARRATIVE_TAGS_FILE)) {
            issues.add("Pacote sem narrative_tags.json");
        }
        if (!entryNames.contains(ProjectArchiveLayout.CHARACTER_TAG_PROFILES_FILE)) {
            issues.add("Pacote sem character_tag_profiles.json");
        }
        if (!entryNames.contains(ProjectArchiveLayout.CHAPTERS_DIRECTORY)) {
            issues.add("Pacote sem diretorio chapters/");
        }
        if (!entryNames.contains(ProjectArchiveLayout.CHARACTERS_DIRECTORY)) {
            issues.add("Pacote sem diretorio characters/");
        }

        boolean requiresMigration = manifest == null;
        int detectedVersion = manifest == null ? 0 : manifest.version();
        if (manifest != null) {
            if (!"storyflame-zip".equals(manifest.format())) {
                issues.add("Formato de manifest invalido: " + manifest.format());
            }
            if (manifest.version() > ProjectArchiveLayout.SPEC_VERSION_NUMBER) {
                issues.add("Versao do pacote nao suportada: " + manifest.version());
            }
            if (manifest.version() < ProjectArchiveLayout.SPEC_VERSION_NUMBER) {
                requiresMigration = true;
            }
        }

        return new ProjectArchiveInspection(issues.isEmpty(), requiresMigration, detectedVersion, List.copyOf(issues));
    }

    private ProjectManifest readManifest(ZipInputStream zip) {
        try {
            Reader reader = new InputStreamReader(zip, StandardCharsets.UTF_8);
            return gson.fromJson(reader, ProjectManifest.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid manifest.json", exception);
        }
    }
}
