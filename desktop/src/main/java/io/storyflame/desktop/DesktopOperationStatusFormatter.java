package io.storyflame.desktop;

import io.storyflame.core.publication.PublicationFormat;
import java.nio.file.Path;

final class DesktopOperationStatusFormatter {
    private DesktopOperationStatusFormatter() {
    }

    static String creatingProject() {
        return loading("Criando projeto inicial...");
    }

    static String openingProject(Path path) {
        return loading("Abrindo projeto: " + fileName(path) + "...");
    }

    static String savingProject(Path path) {
        return loading("Salvando projeto: " + fileName(path) + "...");
    }

    static String exportingProject(Path path) {
        return loading("Exportando projeto: " + fileName(path) + "...");
    }

    static String importingProject(Path path) {
        return loading("Importando projeto: " + fileName(path) + "...");
    }

    static String inspectingArchive(Path path) {
        return loading("Verificando arquivo: " + fileName(path) + "...");
    }

    static String exportingPublication(PublicationFormat format, Path path) {
        return loading("Exportando " + DesktopPublicationExportFormatter.buttonLabel(format) + ": " + fileName(path) + "...");
    }

    static String runningEmotionAnalysis() {
        return loading("Gerando analise emocional do projeto atual...");
    }

    static String partialBackupFailure() {
        return warning("Projeto salvo, mas o backup falhou.");
    }

    static String retainedPreviousArchive(Path path) {
        return warning("Projeto salvo em " + path + " (arquivo antigo mantido)");
    }

    static String loading(String detail) {
        return "Carregando: " + detail;
    }

    static String success(String detail) {
        return "Concluido: " + detail;
    }

    static String warning(String detail) {
        return "Concluido com aviso: " + detail;
    }

    static String failure(String detail) {
        return "Falhou: " + detail;
    }

    private static String fileName(Path path) {
        if (path == null || path.getFileName() == null) {
            return "-";
        }
        return path.getFileName().toString();
    }
}
