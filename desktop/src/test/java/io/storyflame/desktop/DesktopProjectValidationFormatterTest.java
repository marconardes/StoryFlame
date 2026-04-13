package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.validation.ProjectValidationIssue;
import io.storyflame.core.validation.ProjectValidationOperation;
import io.storyflame.core.validation.ProjectValidationResult;
import io.storyflame.core.validation.ProjectValidationSeverity;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopProjectValidationFormatterTest {
    @Test
    void formatsArchiveWarningDialog() {
        ProjectValidationResult validation = new ProjectValidationResult(
                ProjectValidationOperation.SAVE_ARCHIVE,
                List.of(new ProjectValidationIssue("broken-point-of-view", "Cena invalida", ProjectValidationSeverity.WARNING))
        );

        String dialog = DesktopProjectValidationFormatter.archiveWarningDialog(validation);

        assertTrue(dialog.contains("nao impedem salvar"));
        assertTrue(dialog.contains("Cena invalida"));
    }

    @Test
    void formatsPublicationBlockingDialog() {
        ProjectValidationResult validation = new ProjectValidationResult(
                ProjectValidationOperation.EXPORT_PUBLICATION,
                List.of(new ProjectValidationIssue("duplicate-tag-id", "Tag duplicada", ProjectValidationSeverity.BLOCKING))
        );

        String dialog = DesktopProjectValidationFormatter.publicationBlockingDialog(validation);

        assertTrue(dialog.contains("publicacao foi bloqueada"));
        assertTrue(dialog.contains("Tag duplicada"));
    }

    @Test
    void formatsContinueWithWarningsStatus() {
        ProjectValidationResult validation = new ProjectValidationResult(
                ProjectValidationOperation.SAVE_ARCHIVE,
                List.of(new ProjectValidationIssue("broken-point-of-view", "Cena invalida", ProjectValidationSeverity.WARNING))
        );

        assertEquals(
                "Concluido com aviso: Operacao continuou com 1 avisos de validacao.",
                DesktopProjectValidationFormatter.continuedWithWarningsStatus(validation)
        );
    }
}
