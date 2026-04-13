package io.storyflame.desktop;

final class DesktopDraftStateFormatter {
    private DesktopDraftStateFormatter() {
    }

    static String characterSearchTitle() {
        return "Buscar personagem existente";
    }

    static String tagSearchTitle() {
        return "Buscar tag existente";
    }

    static String characterNameTitle() {
        return "Nome do personagem";
    }

    static String tagLabelTitle() {
        return "Rotulo da tag";
    }

    static String tagIdTitle() {
        return "Id da tag ({falc1})";
    }

    static String tagTemplateTitle() {
        return "Texto renderizado";
    }

    static String characterDraftHint(boolean hasSelection, String name, String description) {
        if (!hasSelection) {
            return "Preencha nome e descricao. Depois clique em Salvar personagem.";
        }
        if (name == null || name.isBlank()) {
            return "Preencha o nome do personagem. Depois clique em Salvar personagem.";
        }
        if (description == null || description.isBlank()) {
            return "Preencha a descricao. Depois clique em Salvar personagem.";
        }
        return "Pronto para salvar. Clique em Salvar personagem.";
    }

    static String tagDraftHint(boolean hasSelection, String tagId, String label, String template) {
        if (!hasSelection) {
            return "Preencha id, rotulo e texto renderizado. Depois clique em Salvar tag.";
        }
        if (tagId == null || tagId.isBlank()) {
            return "Preencha o id da tag no formato 4 letras + numero.";
        }
        if (!tagId.matches("[a-z]{4}\\d+")) {
            return "Use id no formato 4 letras + numero, como falc1.";
        }
        if (label == null || label.isBlank()) {
            return "Preencha o rotulo da tag. Depois clique em Salvar tag.";
        }
        if (template == null || template.isBlank()) {
            return "Preencha o texto renderizado. Depois clique em Salvar tag.";
        }
        return "Pronto para salvar. Clique em Salvar tag.";
    }

    static String tagStatus(boolean hasSelection, String label, String template, boolean characterOwned) {
        if (!hasSelection) {
            return "Rascunho de tag";
        }
        if (label == null || label.isBlank()) {
            return "Falta rotulo";
        }
        if (characterOwned) {
            return "Tag de personagem";
        }
        if (template == null || template.isBlank()) {
            return "Falta texto renderizado";
        }
        return "Texto renderizado pronto";
    }
}
