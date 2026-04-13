package io.storyflame.desktop;

import io.storyflame.core.validation.ProjectValidationIssue;
import io.storyflame.core.validation.ProjectValidationResult;
import java.util.List;

final class DesktopProjectValidationFormatter {
    private DesktopProjectValidationFormatter() {
    }

    static String archiveWarningDialog(ProjectValidationResult validation) {
        return """
                O projeto tem inconsistencias que nao impedem salvar o arquivo agora.

                %s

                Deseja continuar mesmo assim?
                """.formatted(issueLines(validation.warningIssues()));
    }

    static String publicationBlockingDialog(ProjectValidationResult validation) {
        return """
                A publicacao foi bloqueada porque o projeto tem inconsistencias que precisam ser corrigidas:

                %s
                """.formatted(issueLines(validation.blockingIssues()));
    }

    static String continuedWithWarningsStatus(ProjectValidationResult validation) {
        if (validation == null || validation.warningIssues().isEmpty()) {
            return DesktopOperationStatusFormatter.success("Operacao validada sem avisos.");
        }
        return DesktopOperationStatusFormatter.warning(
                "Operacao continuou com " + validation.warningIssues().size() + " avisos de validacao."
        );
    }

    private static String issueLines(List<ProjectValidationIssue> issues) {
        return issues.stream()
                .map(issue -> "- " + issue.message())
                .limit(8)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("- Sem detalhes disponiveis");
    }
}
