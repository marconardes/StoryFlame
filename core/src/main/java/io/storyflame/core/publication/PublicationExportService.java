package io.storyflame.core.publication;

import io.storyflame.core.validation.ProjectValidationOperation;
import io.storyflame.core.validation.ProjectValidationResult;
import io.storyflame.core.validation.ProjectValidationService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public final class PublicationExportService {
    public ProjectValidationResult validate(ProjectPublicationRequest request) {
        return ProjectValidationService.validate(request.project(), ProjectValidationOperation.EXPORT_PUBLICATION);
    }

    public Path export(ProjectPublicationRequest request) {
        ProjectValidationResult validation = validate(request);
        if (validation.hasBlockingIssues()) {
            throw new IllegalStateException(
                    "Publication export blocked: "
                            + validation.blockingIssues().stream().map(issue -> issue.message()).findFirst().orElse("validation failed")
            );
        }
        PublicationManuscript manuscript = PublicationManuscriptBuilder.build(request.project());
        PublicationStyleTemplate styleTemplate = PublicationStyleTemplate.editorialDefault();
        try {
            Path parent = request.targetPath().toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return switch (request.format()) {
                case TXT -> writeText(request.targetPath(), renderPlainText(manuscript));
                case MARKDOWN -> writeText(request.targetPath(), renderMarkdown(manuscript));
                case PDF -> writePdf(request.targetPath(), manuscript, styleTemplate);
                case EPUB -> writeEpub(request.targetPath(), manuscript, styleTemplate);
            };
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to export manuscript to " + request.targetPath(), exception);
        }
    }

    private Path writeText(Path targetPath, String content) throws IOException {
        Files.writeString(targetPath, content, StandardCharsets.UTF_8);
        return targetPath;
    }

    private Path writePdf(Path targetPath, PublicationManuscript manuscript, PublicationStyleTemplate styleTemplate) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDFont titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont headingFont = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
            PDFont bodyFont = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
            float margin = styleTemplate.pdfMargin();
            float maxWidth = PDRectangle.A4.getWidth() - (margin * 2);
            PdfCursor cursor = new PdfCursor(document, margin);

            cursor.writeCentered(
                    nonBlank(manuscript.title(), "Sem titulo"),
                    titleFont,
                    styleTemplate.pdfTitleFontSize(),
                    styleTemplate.pdfTitleLineHeight()
            );
            cursor.blankLine(styleTemplate.pdfTitleSpacing());
            if (!manuscript.author().isBlank()) {
                cursor.writeCentered(
                        "Por " + manuscript.author(),
                        bodyFont,
                        styleTemplate.pdfAuthorFontSize(),
                        styleTemplate.pdfAuthorLineHeight()
                );
            }
            if (!manuscript.chapters().isEmpty()) {
                cursor.newPage();
            }

            for (int chapterIndex = 0; chapterIndex < manuscript.chapters().size(); chapterIndex++) {
                PublicationChapter chapter = manuscript.chapters().get(chapterIndex);
                cursor.writeWrapped(
                        nonBlank(chapter.title(), "Capitulo"),
                        headingFont,
                        styleTemplate.pdfChapterFontSize(),
                        maxWidth,
                        styleTemplate.pdfChapterLineHeight()
                );
                cursor.blankLine(styleTemplate.pdfChapterSpacing());
                for (PublicationScene scene : chapter.scenes()) {
                    if (!scene.title().isBlank()) {
                        cursor.writeWrapped(
                                scene.title(),
                                headingFont,
                                styleTemplate.pdfSceneTitleFontSize(),
                                maxWidth,
                                styleTemplate.pdfSceneLineHeight()
                        );
                        cursor.blankLine(styleTemplate.pdfSceneSpacing());
                    }
                    for (String paragraph : scene.content().split("\\R\\R+")) {
                        if (paragraph.isBlank()) {
                            continue;
                        }
                        cursor.writeWrappedWithIndent(
                                paragraph.strip().replaceAll("\\s+", " "),
                                bodyFont,
                                styleTemplate.pdfBodyFontSize(),
                                maxWidth,
                                styleTemplate.pdfParagraphLineHeight(),
                                styleTemplate.pdfParagraphIndent()
                        );
                        cursor.blankLine(styleTemplate.pdfParagraphSpacing());
                    }
                }
                if (chapterIndex < manuscript.chapters().size() - 1) {
                    cursor.newPage();
                }
            }

            cursor.close();
            document.save(targetPath.toFile());
        }
        return targetPath;
    }

    private Path writeEpub(Path targetPath, PublicationManuscript manuscript, PublicationStyleTemplate styleTemplate) throws IOException {
        try (OutputStream output = Files.newOutputStream(targetPath);
             ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            byte[] mimetypeBytes = "application/epub+zip".getBytes(StandardCharsets.UTF_8);
            ZipEntry mimetypeEntry = new ZipEntry("mimetype");
            mimetypeEntry.setMethod(ZipEntry.STORED);
            mimetypeEntry.setSize(mimetypeBytes.length);
            mimetypeEntry.setCompressedSize(mimetypeBytes.length);
            CRC32 crc32 = new CRC32();
            crc32.update(mimetypeBytes);
            mimetypeEntry.setCrc(crc32.getValue());
            zip.putNextEntry(mimetypeEntry);
            zip.write(mimetypeBytes);
            zip.closeEntry();

            writeZipText(zip, "META-INF/container.xml", """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                      <rootfiles>
                        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                      </rootfiles>
                    </container>
                    """);
            writeZipText(zip, "OEBPS/book.xhtml", renderEpubBook(manuscript));
            writeZipText(zip, "OEBPS/nav.xhtml", renderEpubNav(manuscript, styleTemplate));
            writeZipText(zip, "OEBPS/content.opf", renderEpubPackage(manuscript, styleTemplate));
        }
        return targetPath;
    }

    private void writeZipText(ZipOutputStream zip, String path, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String renderPlainText(PublicationManuscript manuscript) {
        StringBuilder builder = new StringBuilder();
        builder.append(nonBlank(manuscript.title(), "Sem titulo")).append('\n');
        if (!manuscript.author().isBlank()) {
            builder.append("Por ").append(manuscript.author()).append('\n');
        }
        builder.append('\n');
        for (PublicationChapter chapter : manuscript.chapters()) {
            builder.append(nonBlank(chapter.title(), "Capitulo")).append('\n').append('\n');
            for (PublicationScene scene : chapter.scenes()) {
                if (!scene.title().isBlank()) {
                    builder.append(scene.title()).append('\n').append('\n');
                }
                builder.append(scene.content().strip()).append('\n').append('\n');
            }
        }
        return builder.toString().strip() + '\n';
    }

    private String renderMarkdown(PublicationManuscript manuscript) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(nonBlank(manuscript.title(), "Sem titulo")).append("\n\n");
        if (!manuscript.author().isBlank()) {
            builder.append("_Por ").append(manuscript.author()).append("_\n\n");
        }
        for (PublicationChapter chapter : manuscript.chapters()) {
            builder.append("## ").append(nonBlank(chapter.title(), "Capitulo")).append("\n\n");
            for (PublicationScene scene : chapter.scenes()) {
                if (!scene.title().isBlank()) {
                    builder.append("### ").append(scene.title()).append("\n\n");
                }
                builder.append(scene.content().strip()).append("\n\n");
            }
        }
        return builder.toString().strip() + '\n';
    }

    private String renderEpubBook(PublicationManuscript manuscript) {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                <?xml version="1.0" encoding="UTF-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                  <title>""").append(escapeXml(nonBlank(manuscript.title(), "Sem titulo"))).append("""
                </title>
                </head>
                <body>
                """);
        builder.append("<h1>").append(escapeXml(nonBlank(manuscript.title(), "Sem titulo"))).append("</h1>");
        if (!manuscript.author().isBlank()) {
            builder.append("<p>").append(escapeXml(manuscript.author())).append("</p>");
        }
        for (int chapterIndex = 0; chapterIndex < manuscript.chapters().size(); chapterIndex++) {
            PublicationChapter chapter = manuscript.chapters().get(chapterIndex);
            builder.append("<h2 id=\"chapter-")
                    .append(chapterIndex + 1)
                    .append("\">")
                    .append(escapeXml(nonBlank(chapter.title(), "Capitulo")))
                    .append("</h2>");
            for (PublicationScene scene : chapter.scenes()) {
                if (!scene.title().isBlank()) {
                    builder.append("<h3>").append(escapeXml(scene.title())).append("</h3>");
                }
                for (String paragraph : scene.content().split("\\R\\R+")) {
                    if (!paragraph.isBlank()) {
                        builder.append("<p>").append(escapeXml(paragraph.strip())).append("</p>");
                    }
                }
            }
        }
        builder.append("</body></html>");
        return builder.toString();
    }

    private String renderEpubNav(PublicationManuscript manuscript, PublicationStyleTemplate styleTemplate) {
        StringBuilder items = new StringBuilder();
        for (int chapterIndex = 0; chapterIndex < manuscript.chapters().size(); chapterIndex++) {
            PublicationChapter chapter = manuscript.chapters().get(chapterIndex);
            items.append("      <li><a href=\"book.xhtml#chapter-")
                    .append(chapterIndex + 1)
                    .append("\">")
                    .append(escapeXml(nonBlank(chapter.title(), "Capitulo")))
                    .append("</a></li>\n");
        }
        if (items.length() == 0) {
            items.append("      <li><a href=\"book.xhtml\">")
                    .append(escapeXml(nonBlank(manuscript.title(), "Sem titulo")))
                    .append("</a></li>\n");
        }
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">
                <head><title>%s</title></head>
                <body>
                  <nav epub:type="toc" id="toc">
                    <h1>%s</h1>
                    <ol>
                %s    </ol>
                  </nav>
                </body>
                </html>
                """.formatted(
                escapeXml(styleTemplate.epubNavigationTitle()),
                escapeXml(styleTemplate.epubNavigationTitle()),
                items
        );
    }

    private String renderEpubPackage(PublicationManuscript manuscript, PublicationStyleTemplate styleTemplate) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="bookid">
                  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:identifier id="bookid">%s</dc:identifier>
                    <dc:title>%s</dc:title>
                    <dc:creator>%s</dc:creator>
                    <dc:language>%s</dc:language>
                  </metadata>
                  <manifest>
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="book" href="book.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="book"/>
                  </spine>
                </package>
                """.formatted(
                escapeXml(nonBlank(manuscript.title(), "storyflame-book")),
                escapeXml(nonBlank(manuscript.title(), "Sem titulo")),
                escapeXml(nonBlank(manuscript.author(), "Autor")),
                escapeXml(styleTemplate.epubLanguage())
        );
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static List<String> wrapPdfText(String value, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String normalized = value.replace('\t', ' ').replace('\r', ' ').trim();
        if (normalized.isEmpty()) {
            lines.add("");
            return lines;
        }
        StringBuilder currentLine = new StringBuilder();
        for (String word : normalized.split("\\s+")) {
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            float candidateWidth = font.getStringWidth(candidate) / 1000f * fontSize;
            if (candidateWidth <= maxWidth || currentLine.length() == 0) {
                currentLine.setLength(0);
                currentLine.append(candidate);
                continue;
            }
            lines.add(currentLine.toString());
            currentLine.setLength(0);
            currentLine.append(word);
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private static final class PdfCursor {
        private final PDDocument document;
        private final float margin;
        private PDPage page;
        private PDPageContentStream contentStream;
        private float y;

        private PdfCursor(PDDocument document, float margin) throws IOException {
            this.document = document;
            this.margin = margin;
            newPageInternal();
        }

        private void writeWrapped(String text, PDFont font, float fontSize, float maxWidth, float lineHeight) throws IOException {
            for (String line : wrapPdfText(text, font, fontSize, maxWidth)) {
                ensureSpace(lineHeight);
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(line);
                contentStream.endText();
                y -= lineHeight;
            }
        }

        private void writeWrappedWithIndent(String text, PDFont font, float fontSize, float maxWidth, float lineHeight, float indent) throws IOException {
            List<String> lines = wrapPdfText(text, font, fontSize, maxWidth - indent);
            for (int index = 0; index < lines.size(); index++) {
                ensureSpace(lineHeight);
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(margin + (index == 0 ? indent : 0), y);
                contentStream.showText(lines.get(index));
                contentStream.endText();
                y -= lineHeight;
            }
        }

        private void writeCentered(String text, PDFont font, float fontSize, float lineHeight) throws IOException {
            ensureSpace(lineHeight);
            float pageWidth = page.getMediaBox().getWidth();
            float textWidth = font.getStringWidth(text) / 1000f * fontSize;
            float startX = Math.max(margin, (pageWidth - textWidth) / 2f);
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(startX, y);
            contentStream.showText(text);
            contentStream.endText();
            y -= lineHeight;
        }

        private void blankLine(float amount) throws IOException {
            ensureSpace(amount);
            y -= amount;
        }

        private void newPage() throws IOException {
            if (contentStream != null && y < page.getMediaBox().getHeight() - margin) {
                newPageInternal();
            }
        }

        private void ensureSpace(float requiredHeight) throws IOException {
            if (y - requiredHeight <= margin) {
                newPageInternal();
            }
        }

        private void newPageInternal() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - margin;
        }

        private void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
        }
    }
}
