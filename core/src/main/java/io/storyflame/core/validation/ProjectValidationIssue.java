package io.storyflame.core.validation;

import java.util.Objects;

public record ProjectValidationIssue(
        String code,
        String message,
        ProjectValidationSeverity severity
) {
    public ProjectValidationIssue {
        code = Objects.requireNonNullElse(code, "unknown");
        message = Objects.requireNonNullElse(message, "");
        severity = severity == null ? ProjectValidationSeverity.WARNING : severity;
    }
}
