package io.storyflame.core.storage;

import com.google.gson.Gson;
import io.storyflame.core.archive.ProjectArchiveLayout;
import io.storyflame.core.archive.ProjectManifest;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import io.storyflame.core.tags.NarrativeTagCatalog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ProjectArchiveStore {
    private final Gson gson;
    private final Path baseDirectory;
    private final ProjectArchiveInspector archiveInspector;

    public ProjectArchiveStore(Path baseDirectory) {
        this.baseDirectory = Objects.requireNonNull(baseDirectory);
        this.gson = JsonMapperFactory.create();
        this.archiveInspector = new ProjectArchiveInspector();
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public Project createProject(String title, String author) {
        Project project = Project.blank(title, author);
        project.getNarrativeTags().addAll(NarrativeTagCatalog.defaultCatalog().all());
        return project;
    }

    public Path save(Project project) {
        return save(project, ProjectStoragePaths.suggestedArchivePath(baseDirectory, project));
    }

    public Path save(Project project, Path path) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(path);
        project.touch();
        try {
            Files.createDirectories(path.getParent());
            try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
                writeJson(zip, ProjectArchiveLayout.MANIFEST_FILE, ProjectManifest.initial(Instant.now().toString()));
                writeJson(zip, ProjectArchiveLayout.PROJECT_FILE, ProjectDocument.from(project));
                writeJson(zip, ProjectArchiveLayout.NARRATIVE_TAGS_FILE, new ArrayList<>(project.getNarrativeTags()));
                writeJson(zip, ProjectArchiveLayout.CHARACTER_TAG_PROFILES_FILE, new ArrayList<>(project.getCharacterTagProfiles()));
                zip.putNextEntry(new ZipEntry(ProjectArchiveLayout.CHAPTERS_DIRECTORY));
                zip.closeEntry();
                zip.putNextEntry(new ZipEntry(ProjectArchiveLayout.CHARACTERS_DIRECTORY));
                zip.closeEntry();

                for (Chapter chapter : project.getChapters()) {
                    writeJson(zip, ProjectArchiveLayout.chapterFile(chapter.getId()), ChapterDocument.from(chapter));
                }
                for (Character character : project.getCharacters()) {
                    writeJson(zip, ProjectArchiveLayout.characterFile(character.getId()), character);
                }
            }
            return path;
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to save project archive: " + path, exception);
        }
    }

    public Path exportArchive(Project project, Path targetPath) {
        return save(project, targetPath);
    }

    public ProjectArchiveInspection inspect(Path path) {
        return archiveInspector.inspect(path);
    }

    public Project open(Path path) {
        Objects.requireNonNull(path);
        ProjectArchiveInspection inspection = inspect(path);
        if (!inspection.valid()) {
            throw new IllegalStateException("Project archive is invalid: " + String.join("; ", inspection.issues()));
        }
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            ProjectDocument projectDocument = null;
            Map<String, Chapter> chapters = new LinkedHashMap<>();
            Map<String, Character> characters = new LinkedHashMap<>();
            List<NarrativeTag> narrativeTags = List.of();
            List<CharacterTagProfile> characterTagProfiles = List.of();
            ProjectManifest manifest = null;
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory()) {
                    continue;
                }
                if (ProjectArchiveLayout.MANIFEST_FILE.equals(name)) {
                    manifest = readManifest(zip);
                } else if (ProjectArchiveLayout.PROJECT_FILE.equals(name)) {
                    projectDocument = readProject(zip);
                } else if (ProjectArchiveLayout.NARRATIVE_TAGS_FILE.equals(name)) {
                    narrativeTags = readNarrativeTags(zip);
                } else if (ProjectArchiveLayout.CHARACTER_TAG_PROFILES_FILE.equals(name)) {
                    characterTagProfiles = readCharacterTagProfiles(zip);
                } else if (name.startsWith(ProjectArchiveLayout.CHAPTERS_DIRECTORY)) {
                    ChapterDocument document = readChapter(zip);
                    chapters.put(document.id(), document.toModel());
                } else if (name.startsWith(ProjectArchiveLayout.CHARACTERS_DIRECTORY)) {
                    Character character = readCharacter(zip);
                    characters.put(character.getId(), character);
                }
            }
            if (projectDocument == null) {
                throw new IllegalStateException("Project archive is missing project.json");
            }
            normalizeManifest(manifest);
            return projectDocument.toModel(chapters, characters, narrativeTags, characterTagProfiles);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to open project archive: " + path, exception);
        }
    }

    public Path importArchive(Path sourcePath) {
        Project project = open(sourcePath);
        Path targetPath = ProjectStoragePaths.suggestedArchivePath(baseDirectory, project);
        return save(project, targetPath);
    }

    public Path migrateArchive(Path sourcePath, Path targetPath) {
        Project project = open(sourcePath);
        return save(project, targetPath);
    }

    public List<Path> listProjects() {
        if (Files.notExists(baseDirectory)) {
            return List.of();
        }
        try {
            try (var stream = Files.list(baseDirectory)) {
                return stream
                        .filter(path -> path.getFileName().toString().endsWith(".storyflame"))
                        .sorted(Comparator.comparing(Path::getFileName))
                        .toList();
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to list projects in " + baseDirectory, exception);
        }
    }

    private void writeJson(ZipOutputStream zip, String entryName, Object value) throws IOException {
        zip.putNextEntry(new ZipEntry(entryName));
        Writer writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
        gson.toJson(value, writer);
        writer.flush();
        zip.closeEntry();
    }

    private ProjectManifest readManifest(ZipInputStream zip) {
        try {
            Reader reader = new InputStreamReader(zip, StandardCharsets.UTF_8);
            return gson.fromJson(reader, ProjectManifest.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid manifest.json", exception);
        }
    }

    private void normalizeManifest(ProjectManifest manifest) {
        if (manifest == null) {
            return;
        }
        if (manifest.version() > ProjectArchiveLayout.SPEC_VERSION_NUMBER) {
            throw new IllegalStateException("Unsupported project archive version: " + manifest.version());
        }
    }

    private ProjectDocument readProject(ZipInputStream zip) {
        Reader reader = new InputStreamReader(zip, StandardCharsets.UTF_8);
        return gson.fromJson(reader, ProjectDocument.class);
    }

    private ChapterDocument readChapter(ZipInputStream zip) {
        Reader reader = new InputStreamReader(zip, StandardCharsets.UTF_8);
        return gson.fromJson(reader, ChapterDocument.class);
    }

    private Character readCharacter(ZipInputStream zip) {
        Reader reader = new InputStreamReader(zip, StandardCharsets.UTF_8);
        return gson.fromJson(reader, Character.class);
    }

    private List<NarrativeTag> readNarrativeTags(ZipInputStream zip) {
        Reader reader = new InputStreamReader(zip, StandardCharsets.UTF_8);
        NarrativeTag[] tags = gson.fromJson(reader, NarrativeTag[].class);
        return tags == null ? List.of() : List.of(tags);
    }

    private List<CharacterTagProfile> readCharacterTagProfiles(ZipInputStream zip) {
        Reader reader = new InputStreamReader(zip, StandardCharsets.UTF_8);
        CharacterTagProfile[] profiles = gson.fromJson(reader, CharacterTagProfile[].class);
        return profiles == null ? List.of() : List.of(profiles);
    }

    private record ProjectDocument(
            String id,
            String title,
            String author,
            Instant createdAt,
            Instant updatedAt,
            List<String> chapterIds,
            List<String> characterIds
    ) {
        static ProjectDocument from(Project project) {
            return new ProjectDocument(
                    project.getId(),
                    project.getTitle(),
                    project.getAuthor(),
                    project.getCreatedAt(),
                    project.getUpdatedAt(),
                    project.getChapters().stream().map(Chapter::getId).toList(),
                    project.getCharacters().stream().map(Character::getId).toList()
            );
        }

        Project toModel(
                Map<String, Chapter> chaptersById,
                Map<String, Character> charactersById,
                List<NarrativeTag> narrativeTags,
                List<CharacterTagProfile> characterTagProfiles
        ) {
            List<Chapter> orderedChapters = new ArrayList<>();
            for (String chapterId : chapterIds) {
                Chapter chapter = chaptersById.get(chapterId);
                if (chapter != null) {
                    orderedChapters.add(chapter);
                }
            }
            List<Character> orderedCharacters = new ArrayList<>();
            for (String characterId : characterIds) {
                Character character = charactersById.get(characterId);
                if (character != null) {
                    orderedCharacters.add(character);
                }
            }
            return new Project(id, title, author, createdAt, updatedAt, orderedChapters, orderedCharacters, narrativeTags, characterTagProfiles);
        }
    }

    private record ChapterDocument(String id, String title, List<Scene> scenes) {
        static ChapterDocument from(Chapter chapter) {
            return new ChapterDocument(chapter.getId(), chapter.getTitle(), new ArrayList<>(chapter.getScenes()));
        }

        Chapter toModel() {
            return new Chapter(id, title, scenes);
        }
    }
}
