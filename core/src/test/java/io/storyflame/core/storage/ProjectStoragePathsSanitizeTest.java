package io.storyflame.core.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProjectStoragePathsSanitizeTest {
    @Test
    void removesAccentsWithoutDroppingLetters() {
        assertEquals("introducao", ProjectStoragePaths.sanitize("Introdução"));
    }

    @Test
    void normalizesMultipleAccentedWords() {
        assertEquals("capitulo-sao-joao", ProjectStoragePaths.sanitize("Capítulo São João"));
    }
}
