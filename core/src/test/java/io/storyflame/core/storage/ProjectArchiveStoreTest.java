package io.storyflame.core.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.archive.ProjectArchiveLayout;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectArchiveStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsProjectWithoutLosingStructure() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Project project = sampleProject();

        Path archive = store.save(project);
        Project loaded = store.open(archive);

        assertEquals(project.getTitle(), loaded.getTitle());
        assertEquals(project.getAuthor(), loaded.getAuthor());
        assertEquals(project.getChapters().size(), loaded.getChapters().size());
        assertEquals(project.getCharacters().size(), loaded.getCharacters().size());
        assertEquals(project.getNarrativeTags().size(), loaded.getNarrativeTags().size());
        assertEquals(project.getCharacterTagProfiles().size(), loaded.getCharacterTagProfiles().size());
        assertEquals(
                project.getChapters().get(0).getScenes().get(0).getContent(),
                loaded.getChapters().get(0).getScenes().get(0).getContent()
        );
    }

    @Test
    void listsSavedProjectsInBaseDirectory() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        store.save(Project.blank("First Book", "Ana"));
        store.save(Project.blank("Second Book", "Ana"));

        List<Path> projects = store.listProjects();

        assertEquals(2, projects.size());
        assertTrue(projects.get(0).getFileName().toString().endsWith(".storyflame"));
    }

    @Test
    void supportsLargeProjects() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Project project = largeProject();

        Path archive = store.save(project);
        assertTrue(Files.size(archive) > 0);

        Project loaded = store.open(archive);
        assertEquals(120, loaded.getChapters().size());
        assertEquals(80, loaded.getCharacters().size());
        assertEquals(12, loaded.getChapters().get(0).getScenes().size());
        assertFalse(loaded.getChapters().get(119).getScenes().get(11).getContent().isBlank());
    }

    @Test
    void inspectsArchiveAndFlagsLegacyManifestMigration() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Path archive = store.save(sampleProject());
        Path legacyArchive = tempDir.resolve("legacy.storyflame");
        createLegacyArchiveWithoutManifest(archive, legacyArchive);

        ProjectArchiveInspection inspection = store.inspect(legacyArchive);
        Project loaded = store.open(legacyArchive);

        assertTrue(inspection.valid());
        assertTrue(inspection.requiresMigration());
        assertEquals(0, inspection.detectedVersion());
        assertEquals("Nebula Hearts", loaded.getTitle());
    }

    @Test
    void importsArchiveIntoManagedDirectory() {
        ProjectArchiveStore sourceStore = new ProjectArchiveStore(tempDir.resolve("source"));
        ProjectArchiveStore managedStore = new ProjectArchiveStore(tempDir.resolve("managed"));
        Path sourceArchive = sourceStore.save(sampleProject());

        Path importedArchive = managedStore.importArchive(sourceArchive);

        assertTrue(importedArchive.startsWith(tempDir.resolve("managed")));
        assertTrue(Files.exists(importedArchive));
        assertEquals("Nebula Hearts", managedStore.open(importedArchive).getTitle());
    }

    @Test
    void createsRotatingBackups() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Project project = sampleProject();
        Path archive = store.save(project);
        ProjectBackupService backupService = new ProjectBackupService(tempDir.resolve("backups"), 2, Duration.ZERO);

        Path firstBackup = backupService.createBackup(archive, project);
        Thread.sleep(1100);
        Path secondBackup = backupService.createBackup(archive, project);
        Thread.sleep(1100);
        Path thirdBackup = backupService.createBackup(archive, project);

        assertNotNull(firstBackup);
        assertNotNull(secondBackup);
        assertNotNull(thirdBackup);
        try (var stream = Files.list(ProjectStoragePaths.backupDirectory(tempDir.resolve("backups"), project))) {
            assertEquals(2, stream.count());
        }
    }

    private Project sampleProject() {
        Project project = Project.blank("Nebula Hearts", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", "Pilot"));
        project.getCharacters().add(new Character("char-2", "Noa", "Engineer"));
        project.getNarrativeTags().add(new NarrativeTag("custom-1", "Custom", "Descricao", "observou tudo"));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-1", "lia", List.of("custom-1")));

        List<Scene> scenes = new ArrayList<>();
        scenes.add(new Scene("scene-1", "Docking", "The station lights flickered.", "char-1"));
        scenes.add(new Scene("scene-2", "Alarm", "An alarm echoed across the hull.", "char-2"));
        project.getChapters().add(new Chapter("chapter-1", "Arrival", scenes));
        return project;
    }

    private Project largeProject() {
        Project project = Project.blank("Massive Archive", "Stress Runner");
        for (int characterIndex = 0; characterIndex < 80; characterIndex++) {
            project.getCharacters().add(new Character("char-" + characterIndex, "Character " + characterIndex, "Role " + characterIndex));
        }
        for (int chapterIndex = 0; chapterIndex < 120; chapterIndex++) {
            List<Scene> scenes = new ArrayList<>();
            for (int sceneIndex = 0; sceneIndex < 12; sceneIndex++) {
                scenes.add(new Scene(
                        "scene-%d-%d".formatted(chapterIndex, sceneIndex),
                        "Scene %d.%d".formatted(chapterIndex, sceneIndex),
                        ("Long body " + chapterIndex + "-" + sceneIndex + " ").repeat(120),
                        "char-" + (sceneIndex % 10)
                ));
            }
            project.getChapters().add(new Chapter("chapter-" + chapterIndex, "Chapter " + chapterIndex, scenes));
        }
        return project;
    }

    private void createLegacyArchiveWithoutManifest(Path sourceArchive, Path legacyArchive) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(sourceArchive)));
             ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(legacyArchive)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (ProjectArchiveLayout.MANIFEST_FILE.equals(entry.getName())) {
                    continue;
                }
                output.putNextEntry(new ZipEntry(entry.getName()));
                zip.transferTo(output);
                output.closeEntry();
            }
        }
    }
}
