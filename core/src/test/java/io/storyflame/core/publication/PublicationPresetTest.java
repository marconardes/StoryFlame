package io.storyflame.core.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PublicationPresetTest {
    @Test
    void exposesStablePresetMappings() {
        assertEquals(PublicationFormat.MARKDOWN, PublicationPreset.QUICK_REVIEW.format());
        assertEquals(PublicationFormat.EPUB, PublicationPreset.DIGITAL_READING.format());
        assertEquals(PublicationFormat.PDF, PublicationPreset.EDITORIAL_PROOF.format());
        assertEquals(PublicationFormat.TXT, PublicationPreset.PLAIN_MANUSCRIPT.format());
    }
}
