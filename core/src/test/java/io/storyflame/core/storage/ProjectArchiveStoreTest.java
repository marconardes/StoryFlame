package io.storyflame.core.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.archive.ProjectArchiveLayout;
import io.storyflame.core.analysis.EmotionAnalysisService;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import io.storyflame.core.validation.ProjectValidationOperation;
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
        new EmotionAnalysisService().analyze(project);

        Path archive = store.save(project);
        Project loaded = store.open(archive);

        assertEquals(project.getTitle(), loaded.getTitle());
        assertEquals(project.getAuthor(), loaded.getAuthor());
        assertEquals(project.getChapters().size(), loaded.getChapters().size());
        assertEquals(project.getCharacters().size(), loaded.getCharacters().size());
        assertEquals(project.getNarrativeTags().size(), loaded.getNarrativeTags().size());
        assertEquals(project.getCharacterTagProfiles().size(), loaded.getCharacterTagProfiles().size());
        assertEquals(project.getEmotionAnalysis().chunkCount(), loaded.getEmotionAnalysis().chunkCount());
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
    void preservesLongSceneContentAcrossSaveAndOpen() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        String longSceneContent = ("Paragrafo de teste para editor MVP. ").repeat(8_000).trim();
        Project project = Project.blank("Editor Flow", "Ana");
        project.getChapters().add(new Chapter(
                "chapter-editor",
                "Capitulo unico",
                List.of(new Scene("scene-editor", "Cena longa", longSceneContent, null))
        ));

        Path archive = store.save(project);
        Project loaded = store.open(archive);

        assertEquals(longSceneContent, loaded.getChapters().get(0).getScenes().get(0).getContent());
    }

    @Test
    void preservesSceneSynopsisAcrossSaveAndOpen() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Project project = Project.blank("Estrutura", "Ana");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo 1",
                List.of(new Scene("scene-1", "Cena 1", "Chegada tensa ao porto.", "Texto completo.", null))
        ));

        Path archive = store.save(project);
        Project loaded = store.open(archive);

        assertEquals("Chegada tensa ao porto.", loaded.getChapters().get(0).getScenes().get(0).getSynopsis());
    }

    @Test
    void preservesChapterAndSceneOrderingAcrossSaveAndOpen() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Project project = Project.blank("Estrutura", "Ana");
        Chapter opening = new Chapter(
                "chapter-opening",
                "Opening",
                new ArrayList<>(List.of(
                        new Scene("scene-2", "Second scene", "content", null),
                        new Scene("scene-1", "First scene", "content", null)
                ))
        );
        Chapter finale = new Chapter(
                "chapter-finale",
                "Finale",
                new ArrayList<>(List.of(
                        new Scene("scene-4", "Fourth scene", "content", null),
                        new Scene("scene-3", "Third scene", "content", null)
                ))
        );
        project.getChapters().add(finale);
        project.getChapters().add(opening);

        Path archive = store.save(project);
        Project loaded = store.open(archive);

        assertEquals(List.of("chapter-finale", "chapter-opening"), loaded.getChapters().stream().map(Chapter::getId).toList());
        assertEquals(List.of("scene-4", "scene-3"), loaded.getChapters().get(0).getScenes().stream().map(Scene::getId).toList());
        assertEquals(List.of("scene-2", "scene-1"), loaded.getChapters().get(1).getScenes().stream().map(Scene::getId).toList());
    }

    @Test
    void preservesCharacterTagProfilesAcrossSaveAndOpen() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Project project = sampleProject();

        Path archive = store.save(project);
        Project loaded = store.open(archive);

        assertEquals(1, loaded.getCharacterTagProfiles().size());
        CharacterTagProfile profile = loaded.getCharacterTagProfiles().get(0);
        assertEquals("char-1", profile.getCharacterId());
        assertEquals("lia", profile.getPrefix());
        assertEquals(List.of("custom-1"), profile.getPreferredTagIds());
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
    void opensLegacyArchiveWithoutSceneSynopsisField() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Path archive = store.save(sampleProject());
        Path legacyArchive = tempDir.resolve("legacy-no-synopsis.storyflame");

        rewriteArchiveEntry(
                archive,
                legacyArchive,
                ProjectArchiveLayout.chapterFile("chapter-1"),
                """
                {"id":"chapter-1","title":"Arrival","scenes":[
                  {"id":"scene-1","title":"Docking","content":"The station lights flickered.","pointOfViewCharacterId":"char-1"},
                  {"id":"scene-2","title":"Alarm","content":"An alarm echoed across the hull.","pointOfViewCharacterId":"char-2"}
                ]}
                """
        );

        Project loaded = store.open(legacyArchive);

        assertEquals("", loaded.getChapters().get(0).getScenes().get(0).getSynopsis());
        assertEquals("The station lights flickered.", loaded.getChapters().get(0).getScenes().get(0).getContent());
    }

    @Test
    void importsArchiveIntoManagedDirectory() {
        ProjectArchiveStore sourceStore = new ProjectArchiveStore(tempDir.resolve("source"));
        ProjectArchiveStore managedStore = new ProjectArchiveStore(tempDir.resolve("managed"));
        Project sourceProject = sampleProject();
        new EmotionAnalysisService().analyze(sourceProject);
        Path sourceArchive = sourceStore.save(sourceProject);

        Path importedArchive = managedStore.importArchive(sourceArchive);
        Project importedProject = managedStore.open(importedArchive);

        assertTrue(importedArchive.startsWith(tempDir.resolve("managed")));
        assertTrue(Files.exists(importedArchive));
        assertEquals("Nebula Hearts", importedProject.getTitle());
        assertEquals(sourceProject.getNarrativeTags().size(), importedProject.getNarrativeTags().size());
        assertEquals(sourceProject.getCharacterTagProfiles().size(), importedProject.getCharacterTagProfiles().size());
        assertEquals(
                sourceProject.getNarrativeTags().stream().map(NarrativeTag::id).toList(),
                importedProject.getNarrativeTags().stream().map(NarrativeTag::id).toList()
        );
        assertEquals(
                sourceProject.getCharacterTagProfiles().get(0).getCharacterId(),
                importedProject.getCharacterTagProfiles().get(0).getCharacterId()
        );
        assertEquals(
                sourceProject.getCharacterTagProfiles().get(0).getPrefix(),
                importedProject.getCharacterTagProfiles().get(0).getPrefix()
        );
        assertNotNull(importedProject.getEmotionAnalysis());
        assertEquals(
                sourceProject.getEmotionAnalysis().chunkCount(),
                importedProject.getEmotionAnalysis().chunkCount()
        );
        assertEquals(
                sourceProject.getEmotionCache().getEntries().size(),
                importedProject.getEmotionCache().getEntries().size()
        );
    }

    @Test
    void migratesArchiveWithoutLosingNarrativeAndAnalysisData() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir.resolve("managed"));
        Project project = sampleProject();
        new EmotionAnalysisService().analyze(project);
        Path sourceArchive = store.save(project, tempDir.resolve("source").resolve("nebula.storyflame"));
        Path migratedArchive = tempDir.resolve("migrated").resolve("nebula-v2.storyflame");

        Path resultArchive = store.migrateArchive(sourceArchive, migratedArchive);
        Project migratedProject = store.open(resultArchive);

        assertEquals(migratedArchive, resultArchive);
        assertEquals(project.getTitle(), migratedProject.getTitle());
        assertEquals(project.getAuthor(), migratedProject.getAuthor());
        assertEquals(project.getChapters().size(), migratedProject.getChapters().size());
        assertEquals(project.getCharacters().size(), migratedProject.getCharacters().size());
        assertEquals(project.getNarrativeTags().size(), migratedProject.getNarrativeTags().size());
        assertEquals(project.getCharacterTagProfiles().size(), migratedProject.getCharacterTagProfiles().size());
        assertEquals(
                project.getNarrativeTags().stream().map(NarrativeTag::id).toList(),
                migratedProject.getNarrativeTags().stream().map(NarrativeTag::id).toList()
        );
        assertEquals(
                project.getCharacterTagProfiles().get(0).getPreferredTagIds(),
                migratedProject.getCharacterTagProfiles().get(0).getPreferredTagIds()
        );
        assertNotNull(migratedProject.getEmotionAnalysis());
        assertEquals(project.getEmotionAnalysis().chunkCount(), migratedProject.getEmotionAnalysis().chunkCount());
        assertEquals(project.getEmotionCache().getEntries().size(), migratedProject.getEmotionCache().getEntries().size());
    }

    @Test
    void rejectsArchiveWhenReferencedChapterEntryIsMissing() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Path archive = store.save(sampleProject());
        Path brokenArchive = tempDir.resolve("missing-chapter.storyflame");

        rewriteArchiveWithoutEntries(archive, brokenArchive, ProjectArchiveLayout.chapterFile("chapter-1"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> store.open(brokenArchive));

        assertTrue(exception.getMessage().contains("missing chapter entries chapter-1"));
    }

    @Test
    void rejectsArchiveWhenReferencedCharacterEntryIsMissing() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Path archive = store.save(sampleProject());
        Path brokenArchive = tempDir.resolve("missing-character.storyflame");

        rewriteArchiveWithoutEntries(archive, brokenArchive, ProjectArchiveLayout.characterFile("char-2"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> store.open(brokenArchive));

        assertTrue(exception.getMessage().contains("missing character entries char-2"));
    }

    @Test
    void rejectsArchiveWhenProjectDocumentJsonIsMalformed() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Path archive = store.save(sampleProject());
        Path brokenArchive = tempDir.resolve("broken-project-json.storyflame");

        rewriteArchiveEntry(archive, brokenArchive, ProjectArchiveLayout.PROJECT_FILE, "{\"id\":");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> store.open(brokenArchive));

        assertTrue(exception.getMessage().contains("Invalid archive entry: project.json"));
    }

    @Test
    void rejectsArchiveWhenChapterDocumentJsonIsMalformed() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Path archive = store.save(sampleProject());
        Path brokenArchive = tempDir.resolve("broken-chapter-json.storyflame");

        rewriteArchiveEntry(archive, brokenArchive, ProjectArchiveLayout.chapterFile("chapter-1"), "{\"id\":");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> store.open(brokenArchive));

        assertTrue(exception.getMessage().contains("Invalid archive entry: chapter document"));
    }

    @Test
    void rejectsArchiveWhenCharacterDocumentJsonIsMalformed() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Path archive = store.save(sampleProject());
        Path brokenArchive = tempDir.resolve("broken-character-json.storyflame");

        rewriteArchiveEntry(archive, brokenArchive, ProjectArchiveLayout.characterFile("char-1"), "{\"id\":");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> store.open(brokenArchive));

        assertTrue(exception.getMessage().contains("Invalid archive entry: character document"));
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

    @Test
    void exposesValidationContractsForSaveAndArchiveExport() {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Project invalidProject = invalidValidationProject();

        var saveValidation = store.validateForSave(invalidProject);
        var archiveExportValidation = store.validateForArchiveExport(invalidProject);

        assertEquals(ProjectValidationOperation.SAVE_ARCHIVE, saveValidation.operation());
        assertEquals(ProjectValidationOperation.EXPORT_ARCHIVE, archiveExportValidation.operation());
        assertFalse(saveValidation.hasBlockingIssues());
        assertFalse(archiveExportValidation.hasBlockingIssues());
        assertEquals(3, saveValidation.warningIssues().size());
        assertEquals(3, archiveExportValidation.warningIssues().size());
        assertEquals(
                saveValidation.warningIssues().stream().map(io.storyflame.core.validation.ProjectValidationIssue::code).toList(),
                archiveExportValidation.warningIssues().stream().map(io.storyflame.core.validation.ProjectValidationIssue::code).toList()
        );
    }

    private Project sampleProject() {
        Project project = Project.blank("Nebula Hearts", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", "Pilot"));
        project.getCharacters().add(new Character("char-2", "Noa", "Engineer"));
        project.getNarrativeTags().add(new NarrativeTag("custom-1", "Custom", "Descricao", "observou tudo"));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-1", "lia", List.of("custom-1")));

        List<Scene> scenes = new ArrayList<>();
        scenes.add(new Scene("scene-1", "Docking", "Chegada da tripulacao ao anel.", "The station lights flickered.", "char-1"));
        scenes.add(new Scene("scene-2", "Alarm", "Sirene interrompe a aproximacao.", "An alarm echoed across the hull.", "char-2"));
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

    private Project invalidValidationProject() {
        Project project = Project.blank("Livro invalido", "Marco");
        project.getCharacters().add(new Character("char-1", "Lia", ""));
        project.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag A", "", "a"));
        project.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag B", "", "b"));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-404", "lia", List.of("tag-1")));
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Cena", "Texto", "char-404"))
        ));
        return project;
    }

    private void createLegacyArchiveWithoutManifest(Path sourceArchive, Path legacyArchive) throws Exception {
        rewriteArchiveWithoutEntries(sourceArchive, legacyArchive, ProjectArchiveLayout.MANIFEST_FILE);
    }

    private void rewriteArchiveWithoutEntries(Path sourceArchive, Path targetArchive, String... excludedEntries) throws Exception {
        List<String> excluded = List.of(excludedEntries);
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(sourceArchive)));
             ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(targetArchive)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (excluded.contains(entry.getName())) {
                    continue;
                }
                output.putNextEntry(new ZipEntry(entry.getName()));
                zip.transferTo(output);
                output.closeEntry();
            }
        }
    }

    private void rewriteArchiveEntry(Path sourceArchive, Path targetArchive, String entryName, String replacementContent) throws Exception {
        rewriteArchiveEntry(sourceArchive, targetArchive, entryName, content -> replacementContent);
    }

    private void rewriteArchiveEntry(
            Path sourceArchive,
            Path targetArchive,
            String entryName,
            java.util.function.UnaryOperator<String> transformer
    ) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(sourceArchive)));
             ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(targetArchive)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                output.putNextEntry(new ZipEntry(entry.getName()));
                if (entryName.equals(entry.getName())) {
                    String originalContent = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    output.write(transformer.apply(originalContent).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } else {
                    zip.transferTo(output);
                }
                output.closeEntry();
            }
        }
    }
}
