package br.com.marconardes.storyflame.swing.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WordCounterUtilTest {

    @Test
    void testCountWords_emptyString() {
        assertEquals(0, WordCounterUtil.countWords(""));
    }

    @Test
    void testCountWords_nullString() {
        assertEquals(0, WordCounterUtil.countWords(null));
    }

    @Test
    void testCountWords_stringWithOnlySpaces() {
        assertEquals(0, WordCounterUtil.countWords("   "));
    }

    @Test
    void testCountWords_stringWithOnlyTabs() {
        assertEquals(0, WordCounterUtil.countWords("\t\t\t"));
    }

    @Test
    void testCountWords_stringWithOnlyNewlines() {
        assertEquals(0, WordCounterUtil.countWords("\n\n\n"));
    }

    @Test
    void testCountWords_stringWithMixedWhitespaceOnly() {
        assertEquals(0, WordCounterUtil.countWords(" \n \t \n "));
    }

    @Test
    void testCountWords_singleWord() {
        assertEquals(1, WordCounterUtil.countWords("Hello"));
    }

    @Test
    void testCountWords_multipleWords() {
        assertEquals(3, WordCounterUtil.countWords("Hello world example"));
    }

    @Test
    void testCountWords_wordsWithExtraSpacesBetween() {
        assertEquals(4, WordCounterUtil.countWords("This   has  extra   spaces."));
    }

    @Test
    void testCountWords_wordsWithLeadingAndTrailingSpaces() {
        assertEquals(4, WordCounterUtil.countWords("  Leading and trailing spaces  ")); // Corrected expected value
    }

    @Test
    void testCountWords_wordsWithPunctuation() {
        // Basic space splitting will count "word." as one word.
        assertEquals(3, WordCounterUtil.countWords("Hello, world. Test!"));
    }

    @Test
    void testCountWords_textWithNewlines() {
        // "First line.\nSecond line.\nThird line." should be 6 words.
        // The replaceAll("\\s+", " ") should handle newlines correctly.
        assertEquals(6, WordCounterUtil.countWords("First line.\nSecond line.\nThird line."));
    }

    @Test
    void testCountWords_textWithTabsAsSeparators() {
        assertEquals(3, WordCounterUtil.countWords("Word1\tWord2\tWord3"));
    }

    @Test
    void testCountWords_textWithMixedWhitespace() {
        assertEquals(5, WordCounterUtil.countWords("Word1\tWord2\nWord3 Word4  Word5"));
    }

    @Test
    void testCountWords_leadingAndTrailingWhitespaceComplex() {
        assertEquals(3, WordCounterUtil.countWords("\n\t  word1 word2 word3 \t\n  "));
    }
}
