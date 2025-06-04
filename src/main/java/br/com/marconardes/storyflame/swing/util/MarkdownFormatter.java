package br.com.marconardes.storyflame.swing.util;

public class MarkdownFormatter {

    public static class FormatResult {
        public final String newText;
        public final int newSelectionStart;
        public final int newSelectionEnd;

        public FormatResult(String newText, int newSelectionStart, int newSelectionEnd) {
            this.newText = newText;
            this.newSelectionStart = newSelectionStart;
            this.newSelectionEnd = newSelectionEnd;
        }
    }

    /**
     * Applies Markdown formatting to a text.
     *
     * @param originalText The original text from the JTextArea.
     * @param selectionStart The start index of the selected text.
     * @param selectionEnd The end index of the selected text.
     * @param syntax The Markdown syntax string (e.g., "**", "*", "# ").
     * @param isPrefix True if the syntax is a prefix (for headers), false to wrap.
     * @return FormatResult containing the new full text and new selection indices.
     */
    public static FormatResult applyFormat(String originalText, int selectionStart, int selectionEnd, String syntax, boolean isPrefix) {
        if (originalText == null) {
            originalText = "";
        }

        String selectedText = "";
        // Ensure selection indices are valid and within originalText bounds
        if (selectionStart >= 0 && selectionEnd >= selectionStart && selectionEnd <= originalText.length()) {
             selectedText = originalText.substring(selectionStart, selectionEnd);
        } else {
            // Invalid selection, treat as caret position at selectionStart or fallback to 0 if selectionStart is also invalid
            selectionStart = Math.max(0, Math.min(selectionStart, originalText.length()));
            selectionEnd = selectionStart; // No actual text selected
        }


        String newText;
        int newSelStart = selectionStart;
        int newSelEnd = selectionEnd;

        if (isPrefix) {
            if (selectedText.isEmpty()) { // Inserting prefix at a caret position
                newText = originalText.substring(0, selectionStart) + syntax + originalText.substring(selectionStart); // Apply to rest of string from caret
                newSelStart = selectionStart;
                // The selection should cover the inserted syntax and the part of the original text that followed the caret
                newSelEnd = selectionStart + syntax.length() + (originalText.length() - selectionStart);
            } else { // Prefixing an existing selection
                newText = originalText.substring(0, selectionStart) + syntax + selectedText + originalText.substring(selectionEnd);
                newSelStart = selectionStart;
                newSelEnd = selectionStart + syntax.length() + selectedText.length();
            }
        } else { // Wrap
            if (selectedText.isEmpty()) { // No selection, just insert syntax pair for user to fill
                newText = originalText.substring(0, selectionStart) + syntax + syntax + originalText.substring(selectionEnd);
                newSelStart = selectionStart + syntax.length(); // Place cursor in the middle
                newSelEnd = newSelStart;
            } else {
                newText = originalText.substring(0, selectionStart) + syntax + selectedText + syntax + originalText.substring(selectionEnd);
                newSelStart = selectionStart; // Selection starts at the beginning of the first syntax
                newSelEnd = selectionStart + syntax.length() * 2 + selectedText.length(); // Selection encompasses syntax and text
            }
        }
        return new FormatResult(newText, newSelStart, newSelEnd);
    }
}
