package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.storyflame.core.publication.PublicationFormat;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DesktopOperationStatusFormatterTest {
    @Test
    void formatsContextualBusyMessages() {
        assertEquals("Carregando: Criando projeto inicial...", DesktopOperationStatusFormatter.creatingProject());
        assertEquals(
                "Carregando: Abrindo projeto: livro.storyflame...",
                DesktopOperationStatusFormatter.openingProject(Path.of("/tmp/livro.storyflame"))
        );
        assertEquals(
                "Carregando: Exportando EPUB (.epub): livro.epub...",
                DesktopOperationStatusFormatter.exportingPublication(PublicationFormat.EPUB, Path.of("/tmp/livro.epub"))
        );
    }

    @Test
    void formatsOutcomeStates() {
        assertEquals("Concluido: Projeto salvo em /tmp/livro.storyflame", DesktopOperationStatusFormatter.success("Projeto salvo em /tmp/livro.storyflame"));
        assertEquals("Concluido com aviso: Projeto salvo, mas o backup falhou.", DesktopOperationStatusFormatter.partialBackupFailure());
        assertEquals("Falhou: Nao foi possivel abrir o projeto.", DesktopOperationStatusFormatter.failure("Nao foi possivel abrir o projeto."));
    }
}
