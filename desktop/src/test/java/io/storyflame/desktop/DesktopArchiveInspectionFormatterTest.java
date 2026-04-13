package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.storage.ProjectArchiveInspection;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopArchiveInspectionFormatterTest {
    @Test
    void formatsInvalidImportFeedback() {
        ProjectArchiveInspection inspection = new ProjectArchiveInspection(false, false, 0, List.of("Pacote sem manifest.json"));

        assertTrue(DesktopArchiveInspectionFormatter.importFailureDialog(inspection).contains("Arquivo invalido."));
        assertEquals(
                "Falhou: Importacao cancelada: arquivo invalido com 1 inconsistencias.",
                DesktopArchiveInspectionFormatter.importFailureStatus(inspection)
        );
    }

    @Test
    void formatsInspectionStatusForMigration() {
        ProjectArchiveInspection inspection = new ProjectArchiveInspection(true, true, 0, List.of());

        assertTrue(DesktopArchiveInspectionFormatter.inspectionDialog(inspection).contains("Migracao necessaria: sim."));
        assertEquals(
                "Concluido com aviso: Arquivo valido. Migracao necessaria.",
                DesktopArchiveInspectionFormatter.inspectionStatus(inspection)
        );
    }

    @Test
    void formatsInspectionCancelledStatus() {
        assertEquals(
                "Concluido com aviso: Verificacao de arquivo cancelada.",
                DesktopArchiveInspectionFormatter.inspectionCancelledStatus()
        );
    }
}
