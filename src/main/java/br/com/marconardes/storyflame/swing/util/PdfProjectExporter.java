package br.com.marconardes.storyflame.swing.util;

import br.com.marconardes.storyflame.swing.model.Chapter;
import br.com.marconardes.storyflame.swing.model.Project;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PdfProjectExporter {

    private static final float MARGIN = 50;
    private static final float LEADING_TITLE = 18f; // Line spacing for titles
    private static final float LEADING_TEXT = 14f;  // Line spacing for normal text
    private static final float FONT_SIZE_PROJECT_TITLE = 20f;
    private static final float FONT_SIZE_CHAPTER_TITLE = 16f;
    private static final float FONT_SIZE_SUMMARY_HEADER = 12f;
    private static final float FONT_SIZE_CONTENT_HEADER = 12f;
    private static final float FONT_SIZE_NORMAL = 10f;

    public void exportProject(Project project, Path filePath) throws IOException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null.");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = page.getMediaBox().getHeight() - MARGIN;

            // Project Title
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_PROJECT_TITLE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(project.getName() != null ? project.getName() : "Untitled Project");
            contentStream.endText();
            yPosition -= LEADING_TITLE * 2; // Extra space after project title

            List<Chapter> chapters = project.getChapters();
            if (chapters == null || chapters.isEmpty()) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("This project has no chapters.");
                contentStream.endText();
            } else {
                for (Chapter chapter : chapters) {
                    if (yPosition < MARGIN + LEADING_TITLE * 3) { // Check space for new chapter
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - MARGIN;
                    }

                    // Chapter Title
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_CHAPTER_TITLE);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText(chapter.getTitle() != null ? chapter.getTitle() : "Untitled Chapter");
                    contentStream.endText();
                    yPosition -= LEADING_TITLE * 1.5f;

                    // Summary
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_SUMMARY_HEADER);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("Summary:");
                    contentStream.endText();
                    yPosition -= LEADING_TEXT;

                    yPosition = writeMultiLineText(contentStream,
                                                   chapter.getSummary() != null && !chapter.getSummary().isEmpty() ? chapter.getSummary() : "No summary provided.",
                                                   MARGIN, yPosition, page.getMediaBox().getWidth() - 2*MARGIN,
                                                   new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL, LEADING_TEXT,
                                                   document);
                    yPosition -= LEADING_TEXT; // Extra space after summary

                    // Content
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_CONTENT_HEADER);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("Content:");
                    contentStream.endText();
                    yPosition -= LEADING_TEXT;

                    yPosition = writeMultiLineText(contentStream,
                                                   chapter.getContent() != null && !chapter.getContent().isEmpty() ? chapter.getContent() : "No content provided.",
                                                   MARGIN, yPosition, page.getMediaBox().getWidth() - 2*MARGIN,
                                                   new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL, LEADING_TEXT,
                                                   document);
                    yPosition -= LEADING_TITLE; // Space before next chapter
                }
            }
            contentStream.close();
            document.save(filePath.toFile());
        } catch (IOException e) {
            System.err.println("Error exporting project to PDF: " + e.getMessage());
            throw e;
        }
    }

    // Helper method to write multi-line text and handle page breaks
    private float writeMultiLineText(PDPageContentStream contentStream, String text,
                                     float x, float yStart, float maxWidth,
                                     PDType1Font font, float fontSize, float leading,
                                     PDDocument document) throws IOException {
        // Get the current page from the content stream
        // This is a bit of a workaround as PDPageContentStream doesn't directly expose its PDPage in PDFBox 3.x in a simple way for this context
        // However, we usually operate on one page at a time and create new ones as needed.
        // The `page` variable from the calling context should be the current one.
        // For page height to check for breaks, we'd ideally get it from the current page.
        // When a new page is created, its MediaBox should be used.
        // Let's assume the caller manages current page context for height for simplicity here
        // or we pass PDPage if this method creates new pages (which it does).

        List<String> lines = splitTextIntoLines(text, maxWidth, font, fontSize);
        float y = yStart;
        PDPage currentPage = document.getPage(document.getNumberOfPages() - 1); // Get current page

        for (String line : lines) {
            if (y < MARGIN) { // Check for page break
                contentStream.endText(); // End text on old page if any was active
                contentStream.close();
                currentPage = new PDPage(); // Create new page
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, fontSize); // Reset font on new page
                y = currentPage.getMediaBox().getHeight() - MARGIN;
            }
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(line);
            contentStream.endText();
            y -= leading;
        }
        return y;
    }

    private List<String> splitTextIntoLines(String text, float maxWidth, PDType1Font font, float fontSize) throws IOException {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add(""); // Add an empty line if text is null or empty to avoid issues
            return lines;
        }
        String[] paragraphs = text.split("\n"); // Split by manual newlines first

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) { // Handle empty paragraphs (multiple newlines)
                lines.add("");
                continue;
            }
            String remainingText = paragraph;
            while (remainingText.length() > 0) {
                int breakPoint = findBreakPoint(remainingText, maxWidth, font, fontSize);
                lines.add(remainingText.substring(0, breakPoint));
                remainingText = remainingText.substring(breakPoint).trim();
            }
        }
        return lines;
    }

    private int findBreakPoint(String text, float maxWidth, PDType1Font font, float fontSize) throws IOException {
        float width = 0;
        int lastSpace = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // PDFBox Font.getStringWidth is in thousands of text space units.
            // https://pdfbox.apache.org/docs/2.0.x/javadocs/org/apache/pdfbox/pdmodel/font/PDFont.html#getStringWidth-java.lang.String-
            float charWidth = font.getStringWidth(String.valueOf(c)) / 1000f * fontSize;

            if (Character.isWhitespace(c)) {
                lastSpace = i;
            }

            width += charWidth;
            if (width > maxWidth) {
                // If lastSpace is -1 (no space found yet in this segment) or 0 (first char is too wide),
                // then break at current char 'i'.
                // Otherwise, break at the last recorded whitespace.
                if (lastSpace > 0) { // Prefer breaking at last space
                    return lastSpace;
                } else { // No space found, or first word is too long
                    return i > 0 ? i : 1; // Break at current char, or 1 if first char itself is too long
                }
            }
        }
        return text.length(); // All fits
    }
}
