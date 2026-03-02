package io.storyflame.core.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class NarrativeTagDetectorTest {
    @Test
    void detectsNarrativeTagsByRegex() {
        List<NarrativeTagMatch> matches = NarrativeTagDetector.detect("Inicio {lfp1} meio {emo1} fim");

        assertEquals(2, matches.size());
        assertEquals("{lfp1}", matches.get(0).rawText());
        assertEquals("lfp1", matches.get(0).tagId());
        assertEquals("{emo1}", matches.get(1).rawText());
        assertEquals("emo1", matches.get(1).tagId());
    }

    @Test
    void ignoresMalformedTags() {
        List<NarrativeTagMatch> matches = NarrativeTagDetector.detect("{1x} {} {tag invalida} ok {beat_1}");

        assertEquals(1, matches.size());
        assertEquals("beat_1", matches.get(0).tagId());
    }
}
