package io.storyflame.android;

import static org.junit.Assert.assertTrue;

import io.storyflame.core.model.Project;
import org.junit.Test;

public class AndroidProjectOverviewFormatterTest {
    @Test
    public void formatsOverviewFromCoreProject() {
        Project project = AndroidProjectPreviewFactory.sampleProject();

        String formatted = AndroidProjectOverviewFormatter.format(project);

        assertTrue(formatted.contains("StoryFlame Android"));
        assertTrue(formatted.contains("Capitulos: 1"));
        assertTrue(formatted.contains("Cenas: 2"));
        assertTrue(formatted.contains("Escopo Android atual: consulta leve"));
        assertTrue(formatted.contains("Nao inclui fluxo editorial completo"));
    }
}
