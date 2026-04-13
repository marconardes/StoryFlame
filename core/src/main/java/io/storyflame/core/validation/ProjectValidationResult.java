package io.storyflame.core.validation;

import java.util.List;
import java.util.Objects;

public record ProjectValidationResult(
        ProjectValidationOperation operation,
        List<ProjectValidationIssue> issues
) {
    public ProjectValidationResult {
        Objects.requireNonNull(operation);
        issues = List.copyOf(Objects.requireNonNullElse(issues, List.of()));
    }

    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    public boolean hasBlockingIssues() {
        return issues.stream().anyMatch(issue -> issue.severity() == ProjectValidationSeverity.BLOCKING);
    }

    public List<ProjectValidationIssue> blockingIssues() {
        return issues.stream()
                .filter(issue -> issue.severity() == ProjectValidationSeverity.BLOCKING)
                .toList();
    }

    public List<ProjectValidationIssue> warningIssues() {
        return issues.stream()
                .filter(issue -> issue.severity() == ProjectValidationSeverity.WARNING)
                .toList();
    }
}
