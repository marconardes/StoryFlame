package io.storyflame.core.publication;

final class PublicationStyleTemplate {
    private static final PublicationStyleTemplate EDITORIAL_DEFAULT = new PublicationStyleTemplate(
            72f,
            19f,
            12f,
            15f,
            11f,
            11f,
            28f,
            18f,
            22f,
            18f,
            17f,
            10f,
            12f,
            4f,
            6f,
            18f,
            "pt-BR",
            "Sumario"
    );

    private final float pdfMargin;
    private final float pdfTitleFontSize;
    private final float pdfAuthorFontSize;
    private final float pdfChapterFontSize;
    private final float pdfSceneTitleFontSize;
    private final float pdfBodyFontSize;
    private final float pdfTitleLineHeight;
    private final float pdfAuthorLineHeight;
    private final float pdfChapterLineHeight;
    private final float pdfSceneLineHeight;
    private final float pdfParagraphLineHeight;
    private final float pdfTitleSpacing;
    private final float pdfChapterSpacing;
    private final float pdfSceneSpacing;
    private final float pdfParagraphSpacing;
    private final float pdfParagraphIndent;
    private final String epubLanguage;
    private final String epubNavigationTitle;

    private PublicationStyleTemplate(
            float pdfMargin,
            float pdfTitleFontSize,
            float pdfAuthorFontSize,
            float pdfChapterFontSize,
            float pdfSceneTitleFontSize,
            float pdfBodyFontSize,
            float pdfTitleLineHeight,
            float pdfAuthorLineHeight,
            float pdfChapterLineHeight,
            float pdfSceneLineHeight,
            float pdfParagraphLineHeight,
            float pdfTitleSpacing,
            float pdfChapterSpacing,
            float pdfSceneSpacing,
            float pdfParagraphSpacing,
            float pdfParagraphIndent,
            String epubLanguage,
            String epubNavigationTitle
    ) {
        this.pdfMargin = pdfMargin;
        this.pdfTitleFontSize = pdfTitleFontSize;
        this.pdfAuthorFontSize = pdfAuthorFontSize;
        this.pdfChapterFontSize = pdfChapterFontSize;
        this.pdfSceneTitleFontSize = pdfSceneTitleFontSize;
        this.pdfBodyFontSize = pdfBodyFontSize;
        this.pdfTitleLineHeight = pdfTitleLineHeight;
        this.pdfAuthorLineHeight = pdfAuthorLineHeight;
        this.pdfChapterLineHeight = pdfChapterLineHeight;
        this.pdfSceneLineHeight = pdfSceneLineHeight;
        this.pdfParagraphLineHeight = pdfParagraphLineHeight;
        this.pdfTitleSpacing = pdfTitleSpacing;
        this.pdfChapterSpacing = pdfChapterSpacing;
        this.pdfSceneSpacing = pdfSceneSpacing;
        this.pdfParagraphSpacing = pdfParagraphSpacing;
        this.pdfParagraphIndent = pdfParagraphIndent;
        this.epubLanguage = epubLanguage;
        this.epubNavigationTitle = epubNavigationTitle;
    }

    static PublicationStyleTemplate editorialDefault() {
        return EDITORIAL_DEFAULT;
    }

    float pdfMargin() {
        return pdfMargin;
    }

    float pdfTitleFontSize() {
        return pdfTitleFontSize;
    }

    float pdfAuthorFontSize() {
        return pdfAuthorFontSize;
    }

    float pdfChapterFontSize() {
        return pdfChapterFontSize;
    }

    float pdfSceneTitleFontSize() {
        return pdfSceneTitleFontSize;
    }

    float pdfBodyFontSize() {
        return pdfBodyFontSize;
    }

    float pdfTitleLineHeight() {
        return pdfTitleLineHeight;
    }

    float pdfAuthorLineHeight() {
        return pdfAuthorLineHeight;
    }

    float pdfChapterLineHeight() {
        return pdfChapterLineHeight;
    }

    float pdfSceneLineHeight() {
        return pdfSceneLineHeight;
    }

    float pdfParagraphLineHeight() {
        return pdfParagraphLineHeight;
    }

    float pdfTitleSpacing() {
        return pdfTitleSpacing;
    }

    float pdfChapterSpacing() {
        return pdfChapterSpacing;
    }

    float pdfSceneSpacing() {
        return pdfSceneSpacing;
    }

    float pdfParagraphSpacing() {
        return pdfParagraphSpacing;
    }

    float pdfParagraphIndent() {
        return pdfParagraphIndent;
    }

    String epubLanguage() {
        return epubLanguage;
    }

    String epubNavigationTitle() {
        return epubNavigationTitle;
    }
}
