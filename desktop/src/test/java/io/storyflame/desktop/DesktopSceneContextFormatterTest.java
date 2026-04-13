package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopSceneContextFormatterTest {
    @Test
    void formatsSceneContextBlocks() {
        assertTrue(DesktopSceneContextFormatter.synopsisText("Cena de abertura com tensao.").contains("Cena de abertura"));
        assertTrue(DesktopSceneContextFormatter.charactersText(List.of("Lia", "Noa")).contains("Lia, Noa"));
        assertTrue(DesktopSceneContextFormatter.tagsText("validas: tag-1").contains("tag-1"));
        assertTrue(DesktopSceneContextFormatter.integrityText("0 referencias quebradas no POV").contains("0 referencias"));
    }
}
