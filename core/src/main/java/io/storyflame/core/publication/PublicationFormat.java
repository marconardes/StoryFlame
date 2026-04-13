package io.storyflame.core.publication;

public enum PublicationFormat {
    TXT(".txt"),
    MARKDOWN(".md"),
    PDF(".pdf"),
    EPUB(".epub");

    private final String extension;

    PublicationFormat(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }
}
