package io.storyflame.app.electron;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.app.project.ProjectApplicationService;
import io.storyflame.app.project.ProjectCharacterApplicationService;
import io.storyflame.app.project.ProjectEditorApplicationService;
import io.storyflame.app.project.ProjectTagApplicationService;
import io.storyflame.core.analysis.EmotionAnalysisService;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectBackupService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ElectronBridgeSessionServiceTest {
    @Test
    void createsAndSavesProjectSession(@TempDir Path tempDir) throws Exception {
        ElectronBridgeSessionService service = newService(tempDir);

        var createResult = service.createProject("Projeto Electron", "Autora");

        assertEquals("Projeto Electron", createResult.session().project().title());
        assertEquals(1, createResult.session().project().chapterCount());
        assertTrue(Files.exists(Path.of(createResult.session().path())));
        assertEquals("Cena 1", createResult.session().scene().title());

        service.updateMetadata("Projeto Electron Revisado", "Autora Revisada");
        service.updateSceneDraft("Cena revisada", "Sinopse revisada", "Conteudo revisado");
        var saveResult = service.saveProject();

        assertEquals("Projeto Electron Revisado", saveResult.session().project().title());
        assertEquals("Autora Revisada", saveResult.session().project().author());
        assertEquals("Cena revisada", saveResult.session().scene().title());
        assertEquals("Sinopse revisada", saveResult.session().scene().synopsis());
        assertTrue(saveResult.session().validation() != null);
    }

    @Test
    void opensExistingProjectSession(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);
        var createResult = service.createProject("Projeto Base", "Autor");

        ElectronBridgeSessionService reopenedService = newService(tempDir);
        var openResult = reopenedService.openProject(createResult.session().path());

        assertEquals("Projeto Base", openResult.session().project().title());
        assertNotNull(openResult.session().project().chapters());
        assertEquals(1, openResult.session().project().chapters().get(0).scenes().size());
    }

    @Test
    void selectsSceneAndUpdatesDraft(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);
        var createResult = service.createProject("Projeto Navegavel", "Autor");
        var chapter = createResult.session().project().chapters().get(0);
        var scene = chapter.scenes().get(0);

        var selectResult = service.selectScene(chapter.id(), scene.id());
        var updateResult = service.updateSceneDraft("Nova Cena", "Nova sinopse", "Novo conteudo");

        assertEquals(scene.id(), selectResult.session().selection().sceneId());
        assertEquals("Nova Cena", updateResult.session().scene().title());
        assertEquals("Nova sinopse", updateResult.session().scene().synopsis());
        assertEquals("Novo conteudo", updateResult.session().scene().content());
    }

    @Test
    void supportsStructureCrud(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);
        service.createProject("Projeto Estrutura", "Autor");

        var addChapterResult = service.addChapter();
        assertEquals(2, addChapterResult.session().project().chapterCount());
        assertEquals("Capitulo 2", addChapterResult.session().project().chapters().get(1).title());

        var moveChapterResult = service.moveChapter(-1);
        assertEquals("Capitulo 2", moveChapterResult.session().project().chapters().get(0).title());

        var addSceneResult = service.addScene();
        assertEquals(2, addSceneResult.session().project().chapters().get(0).scenes().size());
        assertEquals("Cena 2", addSceneResult.session().scene().title());

        var moveSceneResult = service.moveScene(-1);
        assertEquals("Cena 2", moveSceneResult.session().project().chapters().get(0).scenes().get(0).title());

        var removeSceneResult = service.removeScene();
        assertEquals(1, removeSceneResult.session().project().chapters().get(0).scenes().size());

        var removeChapterResult = service.removeChapter();
        assertEquals(1, removeChapterResult.session().project().chapterCount());
    }

    @Test
    void listsLocalProjects(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);

        service.createProject("Projeto Um", "Autora");
        service.createProject("Projeto Dois", "Autora");

        var projects = service.localProjects();

        assertEquals(2, projects.size());
        assertTrue(projects.stream().anyMatch(project -> project.label().contains("projeto-um")));
        assertTrue(projects.stream().anyMatch(project -> project.label().contains("projeto-dois")));
    }

    @Test
    void deletesLocalProject(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);

        var created = service.createProject("Projeto Removivel", "Autora");
        var projects = service.localProjects();
        assertTrue(projects.stream().anyMatch(project -> project.path().equals(created.session().path())));

        service.deleteLocalProject(created.session().path());
        var updated = service.localProjects();
        assertTrue(updated.stream().noneMatch(project -> project.path().equals(created.session().path())));
    }

    @Test
    void supportsCharacterCrud(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);
        service.createProject("Projeto Personagens", "Autora");

        var created = service.createCharacter("Lina", "Protagonista.");
        assertEquals(1, created.session().project().characters().size());
        assertEquals("Lina", created.session().project().characters().get(0).name());
        assertEquals("Lina", created.session().project().characters().stream()
                .filter(character -> character.id().equals(created.session().characterSelection().characterId()))
                .findFirst()
                .orElseThrow()
                .name());

        var updated = service.updateCharacter("Lina Moreau", "Protagonista revisada.");
        assertEquals("Lina Moreau", updated.session().project().characters().get(0).name());
        assertEquals("Protagonista revisada.", updated.session().project().characters().get(0).description());

        var deleted = service.deleteCharacter();
        assertEquals(0, deleted.session().project().characters().size());
    }

    @Test
    void supportsTagCrud(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);
        var initial = service.createProject("Projeto Tags", "Autora");
        int initialCount = initial.session().project().tags().size();

        var created = service.createTag("falc1", "Falha", "Texto renderizado");
        assertEquals(initialCount + 1, created.session().project().tags().size());
        assertTrue(created.session().project().tags().stream().anyMatch(tag -> tag.id().equals("falc1")));
        assertEquals("falc1", created.session().tagSelection().tagId());

        var updated = service.updateTag("falc2", "Falha revisada", "Texto revisado");
        assertTrue(updated.session().project().tags().stream().anyMatch(tag ->
                tag.id().equals("falc2") && tag.label().equals("Falha revisada")));
        assertEquals("falc2", updated.session().tagSelection().tagId());

        var deleted = service.deleteTag();
        assertEquals(initialCount, deleted.session().project().tags().size());
    }

    @Test
    void supportsProfileTagAssignment(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);
        service.createProject("Projeto Perfis", "Autora");

        service.createCharacter("Lina", "Protagonista");
        var tag = service.createTag("falc1", "Falha", "Texto renderizado");
        service.selectProfile(service.currentSession().session().project().profiles().get(0).characterId());

        var updated = service.addTagToProfile(tag.session().tagSelection().tagId());
        assertTrue(updated.session().project().profiles().get(0).preferredTagIds().contains("falc1"));
    }

    @Test
    void supportsSearch(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);
        service.createProject("Projeto Busca", "Autora");

        service.updateSceneDraft("Cena de Busca", "Sinopse", "Conteudo com palavra-chave.");
        var results = service.search("palavra-chave");

        assertTrue(results.stream().anyMatch(result -> "SCENE_CONTENT".equals(result.target())));
    }

    @Test
    void runsEmotionAnalysis(@TempDir Path tempDir) {
        ElectronBridgeSessionService service = newService(tempDir);
        service.createProject("Projeto Analise", "Autora");
        service.updateSceneDraft("Cena Analise", "Sinopse", "Texto com intensidade emocional.");

        var report = service.runEmotionAnalysis();

        assertNotNull(report);
        assertTrue(report.chunkCount() >= 1);
    }

    private ElectronBridgeSessionService newService(Path tempDir) {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir.resolve("projects"));
        ProjectBackupService backups = new ProjectBackupService(tempDir.resolve("backups"), 4, Duration.ZERO);
        return new ElectronBridgeSessionService(
                new ProjectApplicationService(store, backups),
                new ProjectCharacterApplicationService(),
                new ProjectEditorApplicationService(),
                new io.storyflame.app.project.ProjectStructureApplicationService(),
                new ProjectTagApplicationService(),
                new EmotionAnalysisService()
        );
    }
}
