package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.storyflame.core.publication.PublicationFormat;
import io.storyflame.core.publication.PublicationPreset;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DesktopPublicationExportFormatterTest {
    @Test
    void mapsPublicationPresetOptionsToPresets() {
        assertEquals(
                PublicationPreset.EDITORIAL_PROOF,
                DesktopPublicationExportFormatter.presetFromOption("Prova editorial -> PDF (.pdf)")
        );
        assertEquals(
                PublicationPreset.QUICK_REVIEW,
                DesktopPublicationExportFormatter.presetFromOption("Revisao rapida -> Markdown (.md)")
        );
        assertNull(DesktopPublicationExportFormatter.presetFromOption("Desconhecido"));
    }

    @Test
    void mapsPublicationOptionsToFormats() {
        assertEquals(
                PublicationFormat.PDF,
                DesktopPublicationExportFormatter.formatFromOption("PDF (.pdf)")
        );
        assertEquals(
                PublicationFormat.EPUB,
                DesktopPublicationExportFormatter.formatFromOption("EPUB (.epub)")
        );
        assertNull(DesktopPublicationExportFormatter.formatFromOption("Desconhecido"));
    }

    @Test
    void formatsPublicationStatusesWithSingleVocabulary() {
        assertEquals(
                "Concluido com aviso: Publicacao do manuscrito cancelada.",
                DesktopPublicationExportFormatter.cancelledStatus()
        );
        assertEquals(
                "Concluido: Preset selecionado: Prova editorial (PDF).",
                DesktopPublicationExportFormatter.presetChosenStatus(PublicationPreset.EDITORIAL_PROOF)
        );
        assertEquals(
                "Concluido: Manuscrito publicado em PDF para /tmp/livro.pdf com formatacao editorial basica.",
                DesktopPublicationExportFormatter.exportedMessage(PublicationFormat.PDF, Path.of("/tmp/livro.pdf"))
        );
        assertEquals(
                "Concluido com aviso: Manuscrito publicado em EPUB para /tmp/livro.epub com formatacao editorial basica. Nao foi possivel abrir o arquivo.",
                DesktopPublicationExportFormatter.exportedOpenFailedMessage(PublicationFormat.EPUB, Path.of("/tmp/livro.epub"))
        );
    }
}
