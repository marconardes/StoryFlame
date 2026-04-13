package io.storyflame.app.project;

import io.storyflame.core.validation.ProjectValidationResult;
import java.util.List;

public record ProjectValidationDto(String operation, List<ProjectIssueDto> issues) {
    public ProjectValidationDto {
        issues = List.copyOf(issues == null ? List.of() : issues);
    }

    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    public boolean hasBlockingIssues() {
        return issues.stream().anyMatch(issue -> "BLOCKING".equals(issue.severity()));
    }

    public static ProjectValidationDto from(ProjectValidationResult result) {
        return new ProjectValidationDto(
                result.operation().name(),
                result.issues().stream()
                        .map(issue -> new ProjectIssueDto(issue.code(), issue.message(), issue.severity().name()))
                        .toList()
        );
    }
}
