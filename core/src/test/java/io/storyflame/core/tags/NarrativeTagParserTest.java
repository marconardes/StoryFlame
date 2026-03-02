package io.storyflame.core.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class NarrativeTagParserTest {
    @Test
    void validatesTagExistenceAgainstCatalog() {
        NarrativeTagCatalog catalog = new NarrativeTagCatalog(List.of(
                new NarrativeTag("lfp1", "LFP", "Descricao"),
                new NarrativeTag("emo1", "Emocao", "Descricao")
        ));

        List<ParsedNarrativeTag> parsedTags = NarrativeTagParser.parse("A {lfp1} B {missing1} C {emo1}", catalog);

        assertEquals(3, parsedTags.size());
        assertTrue(parsedTags.get(0).valid());
        assertFalse(parsedTags.get(1).valid());
        assertTrue(parsedTags.get(2).valid());
    }

    @Test
    void usesDefaultCatalogForKnownExampleTag() {
        List<ParsedNarrativeTag> parsedTags = NarrativeTagParser.parse("{lfp1}", NarrativeTagCatalog.defaultCatalog());

        assertEquals(1, parsedTags.size());
        assertTrue(parsedTags.get(0).valid());
    }
}
