package io.storyflame.core.publication;

public enum PublicationPreset {
    QUICK_REVIEW(
            "Revisao rapida",
            "Gera Markdown para leitura e revisao editorial leve.",
            PublicationFormat.MARKDOWN
    ),
    DIGITAL_READING(
            "Leitura digital",
            "Gera EPUB para leitura continua em apps e e-readers.",
            PublicationFormat.EPUB
    ),
    EDITORIAL_PROOF(
            "Prova editorial",
            "Gera PDF com formatacao editorial basica para conferencia visual.",
            PublicationFormat.PDF
    ),
    PLAIN_MANUSCRIPT(
            "Manuscrito simples",
            "Gera TXT limpo para compartilhamento bruto ou processamento externo.",
            PublicationFormat.TXT
    );

    private final String label;
    private final String description;
    private final PublicationFormat format;

    PublicationPreset(String label, String description, PublicationFormat format) {
        this.label = label;
        this.description = description;
        this.format = format;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public PublicationFormat format() {
        return format;
    }
}
