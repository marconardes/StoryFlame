package io.storyflame.android;

import io.storyflame.core.model.Project;

final class AndroidProjectOverviewFormatter {
    private AndroidProjectOverviewFormatter() {
    }

    static String emptyState() {
        return "Toque em \"Carregar exemplo de apoio\" para validar o nucleo compartilhado no Android."
                + "\nEste modulo nao substitui o fluxo editorial principal do desktop.";
    }

    static String format(Project project) {
        int chapterCount = project.getChapters().size();
        int sceneCount = project.getChapters().stream().mapToInt(chapter -> chapter.getScenes().size()).sum();
        int characterCount = project.getCharacters().size();

        return "Titulo: " + nonBlank(project.getTitle(), "Sem titulo") + "\n"
                + "Autor: " + nonBlank(project.getAuthor(), "Sem autor") + "\n"
                + "Capitulos: " + chapterCount + "\n"
                + "Cenas: " + sceneCount + "\n"
                + "Personagens: " + characterCount + "\n\n"
                + "Escopo Android atual: consulta leve do manuscrito e validacao do modulo core.\n"
                + "Nao inclui fluxo editorial completo nem substitui a experiencia principal do desktop.";
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
