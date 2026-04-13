package io.storyflame.desktop;

import io.storyflame.core.storage.ProjectArchiveInspection;

final class DesktopArchiveInspectionFormatter {
    private DesktopArchiveInspectionFormatter() {
    }

    static String importFailureDialog(ProjectArchiveInspection inspection) {
        return "Arquivo invalido.\n\n" + detailsBlock(inspection);
    }

    static String inspectionCancelledStatus() {
        return DesktopOperationStatusFormatter.warning("Verificacao de arquivo cancelada.");
    }

    static String importFailureStatus(ProjectArchiveInspection inspection) {
        if (inspection == null || inspection.issues().isEmpty()) {
            return DesktopOperationStatusFormatter.failure("Importacao cancelada: arquivo invalido.");
        }
        return DesktopOperationStatusFormatter.failure(
                "Importacao cancelada: arquivo invalido com " + inspection.issues().size() + " inconsistencias."
        );
    }

    static String inspectionDialog(ProjectArchiveInspection inspection) {
        String status = inspection.valid() ? "Arquivo valido." : "Arquivo invalido.";
        String migration = inspection.requiresMigration()
                ? "\nMigracao necessaria: sim."
                : "\nMigracao necessaria: nao.";
        return status + migration + "\n\n" + detailsBlock(inspection);
    }

    static String inspectionStatus(ProjectArchiveInspection inspection) {
        if (!inspection.valid()) {
            return DesktopOperationStatusFormatter.failure("Arquivo invalido.");
        }
        return inspection.requiresMigration()
                ? DesktopOperationStatusFormatter.warning("Arquivo valido. Migracao necessaria.")
                : DesktopOperationStatusFormatter.success("Arquivo valido. Nenhuma migracao necessaria.");
    }

    private static String detailsBlock(ProjectArchiveInspection inspection) {
        if (inspection == null || inspection.issues().isEmpty()) {
            return "Sem inconsistencias detectadas.";
        }
        return "Inconsistencias detectadas:\n- " + String.join("\n- ", inspection.issues());
    }
}
