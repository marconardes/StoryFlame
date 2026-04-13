package io.storyflame.core.publication;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.NarrativeTag;
import io.storyflame.core.validation.ProjectValidationOperation;
import io.storyflame.core.validation.ProjectValidationResult;
import java.io.BufferedInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PublicationExportServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void exportsTxtWithExpandedTemplates() throws Exception {
        PublicationExportService service = new PublicationExportService();
        Path target = tempDir.resolve("book.txt");

        service.export(new ProjectPublicationRequest(sampleProject(), PublicationFormat.TXT, target));

        String content = Files.readString(target, StandardCharsets.UTF_8);
        assertTrue(content.contains("Lin Fei"));
        assertTrue(content.contains("sentiu a emoção crescer no peito"));
    }

    @Test
    void exportsMarkdown() throws Exception {
        PublicationExportService service = new PublicationExportService();
        Path target = tempDir.resolve("book.md");

        service.export(new ProjectPublicationRequest(sampleProject(), PublicationFormat.MARKDOWN, target));

        String content = Files.readString(target, StandardCharsets.UTF_8);
        assertTrue(content.contains("# Contato Novo"));
        assertTrue(content.contains("## Capitulo 1"));
    }

    @Test
    void exportsPdf() throws Exception {
        PublicationExportService service = new PublicationExportService();
        Path target = tempDir.resolve("book.pdf");

        service.export(new ProjectPublicationRequest(sampleProject(), PublicationFormat.PDF, target));

        byte[] bytes = Files.readAllBytes(target);
        String header = new String(bytes, 0, 4, StandardCharsets.US_ASCII);
        assertTrue("%PDF".equals(header));
    }

    @Test
    void exportsPdfWithoutTrailingBlankPage() throws Exception {
        PublicationExportService service = new PublicationExportService();
        Path target = tempDir.resolve("book-no-blank-page.pdf");

        service.export(new ProjectPublicationRequest(sampleProject(), PublicationFormat.PDF, target));

        try (PDDocument document = Loader.loadPDF(target.toFile())) {
            assertTrue(document.getNumberOfPages() == 2);
        }
    }

    @Test
    void exportsEpubBaseStructure() throws Exception {
        PublicationExportService service = new PublicationExportService();
        Path target = tempDir.resolve("book.epub");

        service.export(new ProjectPublicationRequest(sampleProject(), PublicationFormat.EPUB, target));

        boolean hasMimetype = false;
        boolean hasBook = false;
        boolean hasPackage = false;
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(target)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("mimetype".equals(entry.getName())) {
                    hasMimetype = true;
                }
                if ("OEBPS/book.xhtml".equals(entry.getName())) {
                    hasBook = true;
                }
                if ("OEBPS/content.opf".equals(entry.getName())) {
                    hasPackage = true;
                }
            }
        }
        assertTrue(hasMimetype);
        assertTrue(hasBook);
        assertTrue(hasPackage);
    }

    @Test
    void exportsEpubNavigationWithChapterAnchors() throws Exception {
        PublicationExportService service = new PublicationExportService();
        Path target = tempDir.resolve("book-nav.epub");

        service.export(new ProjectPublicationRequest(fullManuscriptProject(), PublicationFormat.EPUB, target));

        String nav = null;
        String book = null;
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(target)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("OEBPS/nav.xhtml".equals(entry.getName())) {
                    nav = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
                if ("OEBPS/book.xhtml".equals(entry.getName())) {
                    book = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        String finalNav = nav;
        String finalBook = book;

        assertAll(
                () -> assertTrue(finalNav != null && finalNav.contains("Capitulo 1 - Chegada")),
                () -> assertTrue(finalNav != null && finalNav.contains("book.xhtml#chapter-1")),
                () -> assertTrue(finalNav != null && finalNav.contains("Capitulo 2 - A Promessa")),
                () -> assertTrue(finalBook != null && finalBook.contains("<h2 id=\"chapter-1\">Capitulo 1 - Chegada</h2>")),
                () -> assertTrue(finalBook != null && finalBook.contains("<h2 id=\"chapter-2\">Capitulo 2 - A Promessa</h2>"))
        );
    }

    @Test
    void exportsFullManuscriptFixtureToTxtAndMarkdown() throws Exception {
        PublicationExportService service = new PublicationExportService();
        Project manuscript = fullManuscriptProject();
        Path txtTarget = tempDir.resolve("manuscrito-completo.txt");
        Path markdownTarget = tempDir.resolve("manuscrito-completo.md");

        service.export(new ProjectPublicationRequest(manuscript, PublicationFormat.TXT, txtTarget));
        service.export(new ProjectPublicationRequest(manuscript, PublicationFormat.MARKDOWN, markdownTarget));

        String txt = Files.readString(txtTarget, StandardCharsets.UTF_8);
        String markdown = Files.readString(markdownTarget, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(txt.contains("A Cidade Depois da Chuva")),
                () -> assertTrue(txt.contains("Capitulo 1 - Chegada")),
                () -> assertTrue(txt.contains("Capitulo 2 - A Promessa")),
                () -> assertTrue(txt.contains("Lin Fei")),
                () -> assertTrue(txt.contains("houve uma pausa curta")),
                () -> assertTrue(txt.contains("A plataforma tremia sob o peso das malas e do atraso.")),
                () -> assertTrue(txt.contains("Quando a chuva recuou, a cidade pareceu devolver a respiracao.")),
                () -> assertTrue(markdown.contains("# A Cidade Depois da Chuva")),
                () -> assertTrue(markdown.contains("## Capitulo 1 - Chegada")),
                () -> assertTrue(markdown.contains("## Capitulo 2 - A Promessa")),
                () -> assertTrue(markdown.contains("### Cena 3 - O telhado")),
                () -> assertTrue(markdown.contains("Lin Fei")),
                () -> assertTrue(markdown.length() > 700)
        );
    }

    @Test
    void blocksPublicationExportWhenValidationFindsBlockingIssues() {
        PublicationExportService service = new PublicationExportService();
        Project invalidProject = Project.blank("Livro invalido", "Marco");
        invalidProject.getCharacters().add(new Character("char-1", "Lia", ""));
        invalidProject.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag A", "", "a"));
        invalidProject.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag B", "", "b"));
        invalidProject.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Cena", "Texto", "char-404"))
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.export(new ProjectPublicationRequest(invalidProject, PublicationFormat.PDF, tempDir.resolve("invalid.pdf")))
        );

        assertTrue(exception.getMessage().contains("Publication export blocked"));
    }

    @Test
    void validateReturnsBlockingIssuesBeforeExport() {
        PublicationExportService service = new PublicationExportService();
        Project invalidProject = Project.blank("Livro invalido", "Marco");
        invalidProject.getCharacters().add(new Character("char-1", "Lia", ""));
        invalidProject.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag A", "", "a"));
        invalidProject.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag B", "", "b"));
        invalidProject.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Cena", "Texto", "char-404"))
        ));

        ProjectValidationResult validation = service.validate(
                new ProjectPublicationRequest(invalidProject, PublicationFormat.MARKDOWN, tempDir.resolve("invalid.md"))
        );

        assertEquals(ProjectValidationOperation.EXPORT_PUBLICATION, validation.operation());
        assertTrue(validation.hasBlockingIssues());
        assertEquals("duplicate-tag-id", validation.blockingIssues().get(0).code());
        assertEquals(1, validation.warningIssues().size());
        assertEquals("broken-point-of-view", validation.warningIssues().get(0).code());
    }

    @Test
    void validateReturnsOnlyWarningsWhenPublicationHasNoBlockingIssue() {
        PublicationExportService service = new PublicationExportService();
        Project warningOnlyProject = Project.blank("Livro com avisos", "Marco");
        warningOnlyProject.getCharacters().add(new Character("char-1", "Lia", ""));
        warningOnlyProject.getNarrativeTags().add(new NarrativeTag("tag-1", "Tag A", "", "a"));
        warningOnlyProject.getCharacterTagProfiles().add(new io.storyflame.core.tags.CharacterTagProfile("char-404", "lia", List.of("tag-1")));
        warningOnlyProject.getChapters().add(new Chapter(
                "chapter-1",
                "Inicio",
                List.of(new Scene("scene-1", "Cena", "Texto", "char-404"))
        ));

        ProjectValidationResult validation = service.validate(
                new ProjectPublicationRequest(warningOnlyProject, PublicationFormat.MARKDOWN, tempDir.resolve("warning-only.md"))
        );

        assertEquals(ProjectValidationOperation.EXPORT_PUBLICATION, validation.operation());
        assertFalse(validation.hasBlockingIssues());
        assertEquals(2, validation.warningIssues().size());
    }

    private Project sampleProject() {
        Project project = Project.blank("Contato Novo", "Marco");
        project.getNarrativeTags().addAll(List.of(
                new NarrativeTag("lf", "Lin Fei", "__character_tag__:char-1", "Lin Fei"),
                new NarrativeTag("emo1", "Emocao 1", "", "sentiu a emoção crescer no peito"),
                new NarrativeTag("beat1", "Beat 1", "", "houve uma pausa curta")
        ));
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo 1",
                List.of(new Scene("scene-1", "Cena 1", "{lf} {emo1} {beat1}", "char-1"))
        ));
        return project;
    }

    private Project fullManuscriptProject() {
        Project project = Project.blank("A Cidade Depois da Chuva", "Marina Duarte");
        project.getNarrativeTags().addAll(List.of(
                new NarrativeTag("lf", "Lin Fei", "__character_tag__:char-1", "Lin Fei"),
                new NarrativeTag("beat1", "Beat 1", "", "houve uma pausa curta"),
                new NarrativeTag("emo1", "Emocao 1", "", "sentiu a emoção crescer no peito")
        ));
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo 1 - Chegada",
                List.of(
                        new Scene(
                                "scene-1",
                                "Cena 1 - A estacao",
                                """
                                {lf} desceu do trem ainda sem saber se a carta tinha chegado antes dela.

                                A plataforma tremia sob o peso das malas e do atraso. {beat1}

                                Ninguem parecia notar a cidade escorrendo pelas goteiras da cobertura.
                                """,
                                "char-1"
                        ),
                        new Scene(
                                "scene-2",
                                "Cena 2 - A pensao",
                                """
                                O quarto era menor do que nas fotos, mas cabia inteiro dentro da promessa que ela fizera a si mesma.

                                Quando fechou a porta, {lf} {emo1}.
                                """,
                                "char-1"
                        )
                )
        ));
        project.getChapters().add(new Chapter(
                "chapter-2",
                "Capitulo 2 - A Promessa",
                List.of(
                        new Scene(
                                "scene-3",
                                "Cena 3 - O telhado",
                                """
                                No telhado, a cidade parecia menos hostil e mais cansada.

                                Quando a chuva recuou, a cidade pareceu devolver a respiracao.

                                {lf} leu a carta tres vezes antes de aceitar que ainda havia tempo.
                                """,
                                "char-1"
                        ),
                        new Scene(
                                "scene-4",
                                "",
                                """
                                Ela guardou a carta no bolso do casaco e desceu as escadas sem olhar para tras.

                                Havia medo, havia atraso, mas tambem havia caminho.
                                """,
                                "char-1"
                        )
                )
        ));
        return project;
    }
}
