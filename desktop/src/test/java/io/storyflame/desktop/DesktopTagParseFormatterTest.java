package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.tags.ParsedNarrativeTag;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopTagParseFormatterTest {
    @Test
    void formatsEmptyAndValidStates() {
        assertEquals("0 tags", DesktopTagParseFormatter.labelText(List.of()));
        assertEquals(
                "1 tag valida na cena",
                DesktopTagParseFormatter.labelText(List.of(tag("lfp1", true)))
        );
        assertEquals(
                "2 tags validas na cena",
                DesktopTagParseFormatter.labelText(List.of(tag("lfp1", true), tag("emo1", true)))
        );
    }

    @Test
    void formatsMixedValidityState() {
        assertEquals(
                "3 tags | 1 invalida na cena",
                DesktopTagParseFormatter.labelText(List.of(tag("lfp1", true), tag("emo1", true), tag("missing1", false)))
        );
    }

    @Test
    void omitsTooltipWhenNoTagsExist() {
        assertNull(DesktopTagParseFormatter.tooltipText(List.of()));
    }

    @Test
    void formatsTooltipWithValidAndInvalidTags() {
        String tooltip = DesktopTagParseFormatter.tooltipText(List.of(
                tag("lfp1", true),
                tag("emo1", true),
                tag("missing1", false)
        ));

        assertTrue(tooltip.contains("Tags validas: {lfp1}, {emo1}"));
        assertTrue(tooltip.contains("Tags invalidas: {missing1}"));
    }

    private ParsedNarrativeTag tag(String tagId, boolean valid) {
        return new ParsedNarrativeTag("{" + tagId + "}", tagId, 0, tagId.length() + 2, valid);
    }
}
