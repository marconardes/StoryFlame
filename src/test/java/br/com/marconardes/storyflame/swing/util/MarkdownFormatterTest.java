package br.com.marconardes.storyflame.swing.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarkdownFormatterTest {

    @Test
    void testApplyBold_withSelection() {
        String text = "Hello world";
        // Select "world"
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(text, 6, 11, "**", false);
        assertEquals("Hello **world**", result.newText);
        assertEquals(6, result.newSelectionStart);
        assertEquals(6 + 2 + 5 + 2, result.newSelectionEnd); // "**world**"
    }

    @Test
    void testApplyItalic_withSelection() {
        String text = "Some text here";
        // Select "text"
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(text, 5, 9, "*", false);
        assertEquals("Some *text* here", result.newText);
        assertEquals(5, result.newSelectionStart);
        assertEquals(5 + 1 + 4 + 1, result.newSelectionEnd); // "*text*"
    }

    @Test
    void testApplyBold_noSelection_insertsSyntaxAtCaret() {
        String text = "Hello ";
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(text, 6, 6, "**", false);
        assertEquals("Hello ****" , result.newText); // Should be "Hello ****"
        assertEquals(6 + 2, result.newSelectionStart); // Cursor in the middle: "Hello **|**"
        assertEquals(6 + 2, result.newSelectionEnd);
    }

    @Test
    void testApplyItalic_noSelection_insertsSyntaxAtCaret() {
        String text = "Line ";
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(text, 5, 5, "*", false);
        assertEquals("Line **" , result.newText); // Should be "Line **"
        assertEquals(5 + 1, result.newSelectionStart); // Cursor in the middle: "Line *|*"
        assertEquals(5 + 1, result.newSelectionEnd);
    }

    @Test
    void testApplyH1_withSelection() {
        String text = "My Title";
        // Select "My Title"
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(text, 0, 8, "# ", true);
        assertEquals("# My Title", result.newText);
        assertEquals(0, result.newSelectionStart);
        assertEquals(2 + 8, result.newSelectionEnd); // "# My Title"
    }

    @Test
    void testApplyH2_noSelection_insertsPrefixAtCaret() {
        String text = "Subtitle";
        // Select nothing, caret at start of "Subtitle"
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(text, 0, 0, "## ", true);
        assertEquals("## Subtitle", result.newText);
        assertEquals(0, result.newSelectionStart); // Selection starts at beginning of prefix
        assertEquals(3 + 8, result.newSelectionEnd); // Selection covers "## Subtitle"
    }

    @Test
    void testApplyH3_toEmptyLine_insertsPrefix() {
        String text = "";
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(text, 0, 0, "### ", true);
        assertEquals("### ", result.newText);
        assertEquals(0, result.newSelectionStart);
        assertEquals(4, result.newSelectionEnd);
    }

    @Test
    void testApplyBold_emptyText_noSelection() {
        String text = "";
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(text, 0, 0, "**", false);
        assertEquals("****", result.newText);
        assertEquals(2, result.newSelectionStart);
        assertEquals(2, result.newSelectionEnd);
    }

    @Test
    void testApplyFormat_prefix_middleOfText_withSelection() {
        String originalText = "line1\nline2\nline3";
        // Select "line2"
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(originalText, 6, 11, "# ", true);
        assertEquals("line1\n# line2\nline3", result.newText);
        assertEquals(6, result.newSelectionStart); // Start of "# line2"
        assertEquals(6 + "# ".length() + "line2".length(), result.newSelectionEnd); // End of "# line2"
    }

    @Test
    void testApplyFormat_wrap_multipleLinesSelection() {
        String originalText = "This is\na multi-line\nselection";
        // Select "a multi-line\nselection"
        MarkdownFormatter.FormatResult result = MarkdownFormatter.applyFormat(originalText, 8, 30, "_", false);
        assertEquals("This is\n_a multi-line\nselection_", result.newText);
        assertEquals(8, result.newSelectionStart);
        assertEquals(8 + "_".length() + "a multi-line\nselection".length() + "_".length(), result.newSelectionEnd);
    }
}
