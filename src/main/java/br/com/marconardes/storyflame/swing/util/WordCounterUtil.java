package br.com.marconardes.storyflame.swing.util;

// No specific imports like StringTokenizer or Pattern are strictly needed for this simple version,
// but they might be for more advanced word counting. java.util.regex.Pattern could be used
// for a more robust split if needed, but text.split("\s+") is generally okay.

public class WordCounterUtil {

    /**
     * Counts words in a given text.
     * A word is generally a sequence of non-whitespace characters.
     * This implementation uses a simple approach by splitting by whitespace,
     * which is generally effective for basic word counting.
     *
     * @param text The text to count words in.
     * @return The number of words.
     */
    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        // Replace multiple whitespace characters (including newlines, tabs) with a single space and trim
        String trimmedText = text.replaceAll("\\s+", " ").trim();
        if (trimmedText.isEmpty()) { // Check again after replacing multiple spaces, could result in empty
            return 0;
        }
        // Split by single space
        return trimmedText.split(" ").length;
    }
}
