package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Project;
import io.storyflame.core.analysis.EmotionAnalysisService;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Scene;
import io.storyflame.core.model.Character;
import io.storyflame.core.publication.PublicationExportService;
import io.storyflame.core.publication.PublicationFormat;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectAutosaveService;
import io.storyflame.core.storage.ProjectBackupService;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import javax.swing.JList;
import java.util.stream.Stream;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assumptions;

class StoryFlameDesktopAppSwingTest {
    private StoryFlameDesktopApp app;
    private FrameFixture window;

    @BeforeAll
    static void installThreadChecks() {
        FailOnThreadViolationRepaintManager.install();
    }

    @AfterEach
    void tearDown() {
        if (window != null) {
            window.cleanUp();
        }
        if (app != null) {
            GuiActionRunner.execute(() -> {
                app.closeWindowForTests();
                return null;
            });
        }
    }

    @Test
    void bootsWithInitialProjectReadyForEditing(@TempDir Path tempDir) {
        ProjectArchiveStore store = launchApp(tempDir);

        Pause.pause(new Condition("initial project loaded") {
            @Override
            public boolean test() {
                return window.label("statusLabel").text().startsWith("Concluido: Projeto criado em ");
            }
        }, 10000);

        assertTrue(window.label("statusLabel").text().startsWith("Concluido: Projeto criado em "));
        window.textBox("titleField").requireText("Novo Projeto");
        window.textBox("authorField").requireText("Autor");
        window.textBox("sceneTitleField").requireText("Cena 1");
        window.textBox("sceneSynopsisArea").requireText("");
        window.textBox("sceneEditorArea").requireText("");
        assertTrue(window.textBox("sceneEditorArea").target().isEditable());
        window.button("publishButton").requireVisible();
    }

    @Test
    void savesEditedProjectToArchive(@TempDir Path tempDir) throws IOException {
        ProjectArchiveStore store = launchApp(tempDir);

        waitForStatusPrefix("Concluido: Projeto criado em ");
        window.textBox("titleField").deleteText().enterText("Projeto de teste");
        window.textBox("authorField").deleteText().enterText("Autora Swing");
        window.textBox("sceneTitleField").deleteText().enterText("Cena de abertura");
        window.textBox("sceneSynopsisArea").enterText("A heroina decide entrar no conflito.");
        window.textBox("sceneEditorArea").enterText("A heroina respirou fundo antes de entrar na sala.");

        window.button("saveButton").click();
        waitForStatusPrefix("Concluido: Projeto salvo em ");

        Path savedArchive = findLatestArchive(tempDir.resolve("projects"));
        Project savedProject = store.open(savedArchive);

        assertTrue(window.label("statusLabel").text().startsWith("Concluido: Projeto salvo em "));
        org.junit.jupiter.api.Assertions.assertEquals("Projeto de teste", savedProject.getTitle());
        org.junit.jupiter.api.Assertions.assertEquals("Autora Swing", savedProject.getAuthor());
        org.junit.jupiter.api.Assertions.assertEquals("Cena de abertura", savedProject.getChapters().get(0).getScenes().get(0).getTitle());
        org.junit.jupiter.api.Assertions.assertEquals(
                "A heroina decide entrar no conflito.",
                savedProject.getChapters().get(0).getScenes().get(0).getSynopsis()
        );
        assertTrue(savedProject.getChapters().get(0).getScenes().get(0).getContent().contains("heroina respirou fundo"));
    }

    @Test
    void runsEmotionAnalysisAndUpdatesAnalysisPanel(@TempDir Path tempDir) {
        launchApp(tempDir);

        waitForStatusPrefix("Concluido: Projeto criado em ");
        window.textBox("sceneEditorArea").enterText("Ela sorriu aliviada e sentiu calma ao ver o sol nascer.");
        window.tabbedPane("mainTabs").selectTab("Analise");
        window.robot().waitForIdle();
        GuiActionRunner.execute(() -> {
            app.runEmotionAnalysisForTests();
            return null;
        });

        Pause.pause(new Condition("analysis completed") {
            @Override
            public boolean test() {
                return window.label("statusLabel").text().startsWith("Concluido: Leitura emocional atualizada com ");
            }
        }, 10000);

        assertTrue(window.label("emotionSentimentLabel").text().startsWith("Clima geral: "));
        assertTrue(window.label("emotionDominantLabel").text().startsWith("Emocao mais presente: "));
        assertTrue(window.label("emotionChunkCountLabel").text().contains("trechos avaliados"));
        assertTrue(window.textBox("analysisArea").text().contains("Leitura heuristica offline"));
        assertTrue(window.textBox("analysisArea").text().contains("Panorama emocional do manuscrito"));
    }

    @Test
    void opensExistingProjectAndRefreshesEditor(@TempDir Path tempDir) {
        ProjectArchiveStore store = launchApp(tempDir);
        waitForStatusPrefix("Concluido: Projeto criado em ");

        Project sourceProject = store.createProject("Projeto aberto", "Leitora");
        sourceProject.getCharacters().add(new Character("char-1", "Lia", "Pilota"));
        sourceProject.getNarrativeTags().add(new NarrativeTag("tag-chegada", "Chegada", "", "aportou"));
        sourceProject.getCharacterTagProfiles().add(new CharacterTagProfile("char-1", "lia", java.util.List.of("tag-chegada")));
        sourceProject.getChapters().add(new Chapter(null, "Capitulo aberto", java.util.List.of(
                new Scene(null, "Cena aberta", "Resumo rapido da cena aberta.", "Texto vindo do arquivo aberto com {tag-chegada}.", "char-1")
        )));
        Path archivePath = store.save(sourceProject, tempDir.resolve("fixtures").resolve("projeto-aberto.storyflame"));

        GuiActionRunner.execute(() -> {
            app.openProjectForTests(archivePath);
            return null;
        });

        waitForStatusPrefix("Concluido: Projeto aberto de ");
        window.textBox("titleField").requireText("Projeto aberto");
        window.textBox("authorField").requireText("Leitora");
        window.textBox("sceneTitleField").requireText("Cena aberta");
        window.textBox("sceneSynopsisArea").requireText("Resumo rapido da cena aberta.");
        window.textBox("sceneEditorArea").requireText("Texto vindo do arquivo aberto com {tag-chegada}.");
        assertTrue(window.label("sceneContextSynopsisLabel").text().contains("Resumo rapido da cena aberta."));
        assertTrue(window.label("sceneContextCharactersLabel").text().contains("Lia"));
        assertTrue(window.label("sceneContextTagsLabel").text().contains("tag-chegada"));
        assertTrue(window.label("sceneContextIntegrityLabel").text().contains("0 referencias quebradas no POV"));
        window.tabbedPane("mainTabs").selectTab("Estrutura");
        GuiActionRunner.execute(() -> {
            @SuppressWarnings("unchecked")
            JList<String> list = (JList<String>) window.list("sceneList").target();
            String sceneOutline = list.getModel().getElementAt(0);
            assertTrue(sceneOutline.contains("Resumo rapido da cena aberta."));
            assertTrue(sceneOutline.contains("POV: Lia"));
            return null;
        });
    }

    @Test
    void importsExternalArchiveAndLoadsImportedProject(@TempDir Path tempDir) {
        launchApp(tempDir);
        Pause.pause(new Condition("initial project title ready") {
            @Override
            public boolean test() {
                return "Novo Projeto".equals(window.textBox("titleField").text());
            }
        }, 10000);

        Path externalBase = tempDir.resolve("external");
        ProjectArchiveStore externalStore = new ProjectArchiveStore(externalBase.resolve("projects"));
        Project externalProject = Project.blank("Projeto importado", "Autora externa");
        externalProject.getChapters().add(new Chapter("chapter-imported", "Capitulo importado", java.util.List.of(
                new Scene("scene-imported", "Cena importada", "Conteudo do pacote externo.", null)
        )));
        Path externalArchive = externalStore.save(externalProject, externalBase.resolve("pacote-externo.storyflame"));

        GuiActionRunner.execute(() -> {
            app.importProjectArchiveForTests(externalArchive);
            return null;
        });

        Pause.pause(new Condition("project imported") {
            @Override
            public boolean test() {
                return "Projeto importado".equals(window.textBox("titleField").text());
            }
        }, 10000);
        window.textBox("titleField").requireText("Projeto importado");
        window.textBox("authorField").requireText("Autora externa");
        window.textBox("sceneTitleField").requireText("Cena importada");
        Pause.pause(new Condition("scene imported content ready") {
            @Override
            public boolean test() {
                return "Conteudo do pacote externo.".equals(window.textBox("sceneEditorArea").text());
            }
        }, 10000);
        window.textBox("sceneEditorArea").requireText("Conteudo do pacote externo.");
    }

    @Test
    void exportsPublishableMarkdownWithoutChooser(@TempDir Path tempDir) throws IOException {
        launchApp(tempDir);
        waitForStatusPrefix("Concluido: Projeto criado em ");

        window.textBox("titleField").deleteText().enterText("Livro Publicado");
        window.textBox("authorField").deleteText().enterText("Autor Markdown");
        window.textBox("sceneTitleField").deleteText().enterText("Cena publicavel");
        window.textBox("sceneEditorArea").enterText("Texto final para publicacao em markdown.");

        Path targetPath = tempDir.resolve("exports").resolve("livro-publicado.md");
        GuiActionRunner.execute(() -> {
            app.exportPublishableForTests(PublicationFormat.MARKDOWN, targetPath);
            return null;
        });

        waitForStatusPrefix("Concluido: Manuscrito publicado em Markdown para ");
        assertTrue(Files.exists(targetPath));
        String exportedText = Files.readString(targetPath);
        assertTrue(window.label("statusLabel").text().startsWith("Concluido: Manuscrito publicado em Markdown para "));
        assertTrue(exportedText.contains("# Livro Publicado"));
        assertTrue(exportedText.contains("_Por Autor Markdown_"));
        assertTrue(exportedText.contains("## Capitulo 1"));
        assertTrue(exportedText.contains("### Cena publicavel"));
        assertTrue(exportedText.contains("Texto final para publicacao em markdown."));
    }

    @Test
    void editsExistingCharacterAndRefreshesCharacterPanel(@TempDir Path tempDir) {
        ProjectArchiveStore store = launchApp(tempDir);
        waitForStatusPrefix("Concluido: Projeto criado em ");

        Path archivePath = createCharacterAndTagFixture(store, tempDir.resolve("fixtures").resolve("character-tag.storyflame"));
        GuiActionRunner.execute(() -> {
            app.openProjectForTests(archivePath);
            return null;
        });

        waitForStatusPrefix("Concluido: Projeto aberto de ");
        window.tabbedPane("mainTabs").selectTab("Personagens");
        window.list("characterList").selectItem(0);
        window.textBox("characterNameField").deleteText().enterText("Lia Revisada");
        window.textBox("characterDescriptionArea").deleteText().enterText("Pilota revisada para regressao Swing.");

        Pause.pause(new Condition("character list refreshed") {
            @Override
            public boolean test() {
                return window.list("characterList").contents()[0].contains("Lia Revisada");
            }
        }, 10000);

        assertTrue(window.label("characterDetailModeLabel").text().contains("Editando personagem"));
        assertTrue(window.textBox("characterDescriptionArea").text().contains("regressao Swing"));
        assertTrue(window.label("statusLabel").text().contains("personagem") || window.label("statusLabel").text().contains("Personagem"));
    }

    @Test
    void editsExistingTagAndRefreshesTagPanel(@TempDir Path tempDir) {
        ProjectArchiveStore store = launchApp(tempDir);
        waitForStatusPrefix("Concluido: Projeto criado em ");

        Path archivePath = createCharacterAndTagFixture(store, tempDir.resolve("fixtures").resolve("character-tag.storyflame"));
        GuiActionRunner.execute(() -> {
            app.openProjectForTests(archivePath);
            return null;
        });

        waitForStatusPrefix("Concluido: Projeto aberto de ");
        window.tabbedPane("mainTabs").selectTab("Tags");
        window.list("tagList").selectItem(0);
        window.textBox("tagLabelField").deleteText().enterText("Nova Rotulo");
        window.textBox("tagTemplateField").deleteText().enterText("texto renderizado revisado");

        Pause.pause(new Condition("tag list refreshed") {
            @Override
            public boolean test() {
                return window.list("tagList").contents()[0].contains("Nova Rotulo");
            }
        }, 10000);

        assertTrue(window.textBox("tagLabelField").text().contains("Nova Rotulo"));
        assertTrue(window.label("statusLabel").text().contains("Tag atualizada."));
    }

    @Test
    void createsCharacterFromInlineDraftWithoutModal(@TempDir Path tempDir) {
        launchApp(tempDir);
        waitForInitialProjectReady();

        window.tabbedPane("mainTabs").selectTab("Personagens");
        window.robot().waitForIdle();
        int initialCharacterCount = window.list("characterList").contents().length;
        window.button("addCharacterButton").click();

        Pause.pause(new Condition("character draft mode visible") {
            @Override
            public boolean test() {
                return "Novo personagem".equals(window.label("characterDetailModeLabel").text());
            }
        }, 10000);

        org.junit.jupiter.api.Assertions.assertEquals(initialCharacterCount, window.list("characterList").contents().length);
        window.textBox("characterNameField").requireFocused();
        window.textBox("characterNameField").enterText("Mara");
        window.textBox("characterDescriptionArea").enterText("Personagem criada sem modal.");
        window.button("saveCharacterButton").click();

        Pause.pause(new Condition("character saved in list") {
            @Override
            public boolean test() {
                return java.util.Arrays.stream(window.list("characterList").contents()).anyMatch(item -> item.contains("Mara"));
            }
        }, 10000);

        assertTrue(window.label("characterDraftHintLabel").text().contains("Salvar personagem"));
        assertTrue(window.label("characterDetailModeLabel").text().contains("Editando personagem"));
    }

    @Test
    void createsTagFromInlineDraftWithoutModal(@TempDir Path tempDir) {
        launchApp(tempDir);
        waitForInitialProjectReady();

        window.tabbedPane("mainTabs").selectTab("Tags");
        window.robot().waitForIdle();
        int initialTagCount = window.list("tagList").contents().length;
        window.button("addTagButton").click();

        Pause.pause(new Condition("tag draft mode visible") {
            @Override
            public boolean test() {
                return "Nova tag".equals(window.label("tagDetailModeLabel").text());
            }
        }, 10000);

        org.junit.jupiter.api.Assertions.assertEquals(initialTagCount, window.list("tagList").contents().length);
        window.textBox("tagIdField").requireFocused();
        window.textBox("tagIdField").enterText("alia1");
        window.textBox("tagLabelField").enterText("Aliada");
        window.textBox("tagTemplateField").enterText("aliada determinada");
        window.button("saveTagButton").click();

        Pause.pause(new Condition("tag saved in list") {
            @Override
            public boolean test() {
                return java.util.Arrays.stream(window.list("tagList").contents()).anyMatch(item -> item.contains("Aliada"));
            }
        }, 10000);

        assertTrue(window.label("tagDraftHintLabel").text().contains("Salvar tag"));
        assertTrue(window.label("tagDetailModeLabel").text().contains("Editando tag"));
    }

    private ProjectArchiveStore launchApp(Path tempDir) {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "AssertJ-Swing requires a graphical environment");

        ProjectArchiveStore store = new ProjectArchiveStore(tempDir.resolve("projects"));
        ProjectAutosaveService autosaveService = new ProjectAutosaveService(store, Duration.ofSeconds(2));
        ProjectBackupService backupService = new ProjectBackupService(
                tempDir.resolve("backups"),
                2,
                Duration.ofMinutes(5)
        );

        app = GuiActionRunner.execute(() -> new StoryFlameDesktopApp(
                store,
                autosaveService,
                backupService,
                new PublicationExportService(),
                new EmotionAnalysisService()
        ));
        window = new FrameFixture(GuiActionRunner.execute(app::showWindowForTests));
        window.robot().waitForIdle();
        return store;
    }

    private void waitForStatusPrefix(String prefix) {
        Pause.pause(new Condition("status starts with " + prefix) {
            @Override
            public boolean test() {
                return window.label("statusLabel").text().startsWith(prefix);
            }
        }, 10000);
    }

    private void waitForInitialProjectReady() {
        Pause.pause(new Condition("initial project title ready") {
            @Override
            public boolean test() {
                return "Novo Projeto".equals(window.textBox("titleField").text())
                        && "Cena 1".equals(window.textBox("sceneTitleField").text());
            }
        }, 10000);
    }

    private Path findLatestArchive(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith(".storyflame"))
                    .max(Comparator.comparingLong(this::lastModified))
                    .orElseThrow();
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect archive timestamp", exception);
        }
    }

    private Path createCharacterAndTagFixture(ProjectArchiveStore store, Path archivePath) {
        Project project = Project.blank("Projeto personagens e tags", "Teste");
        project.getCharacters().add(new Character("char-1", "Lia", "Pilota original."));
        project.getNarrativeTags().add(new NarrativeTag("tag-1", "Rotulo inicial", "Descricao", "texto renderizado"));
        project.getCharacterTagProfiles().add(new CharacterTagProfile("char-1", "lia", java.util.List.of("tag-1")));
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo 1",
                java.util.List.of(new Scene("scene-1", "Cena 1", "Texto com {tag-1}", "char-1"))
        ));
        return store.save(project, archivePath);
    }
}
