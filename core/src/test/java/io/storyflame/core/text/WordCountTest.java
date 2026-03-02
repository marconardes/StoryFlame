package io.storyflame.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WordCountTest {
    @Test
    void countsWordsAcrossMultipleLines() {
        assertEquals(7, WordCount.count("Uma cena\ncom varias linhas e espacos"));
    }

    @Test
    void returnsZeroForBlankText() {
        assertEquals(0, WordCount.count("   \n\t  "));
    }

    @Test
    void handlesLongTextWithoutLosingCount() {
        String text = ("historia longa ").repeat(20_000).trim();

        assertEquals(40_000, WordCount.count(text));
    }
}
