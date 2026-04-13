package io.storyflame.core.validation;

import io.storyflame.core.model.Project;
import io.storyflame.core.tags.TagLibraryIssue;
import io.storyflame.core.tags.TagLibraryValidator;
import java.util.ArrayList;
import java.util.List;

public final class ProjectValidationService {
    private ProjectValidationService() {
    }

    public static ProjectValidationResult validate(Project project, ProjectValidationOperation operation) {
        List<ProjectValidationIssue> issues = new ArrayList<>();

        for (NarrativeIntegrityIssue issue : NarrativeIntegrityValidator.findBrokenPointOfViewReferences(project)) {
            issues.add(new ProjectValidationIssue(
                    "broken-point-of-view",
                    issue.message(),
                    severityForNarrativeIssue(operation)
            ));
        }
        for (TagLibraryIssue issue : TagLibraryValidator.validate(project)) {
            issues.add(new ProjectValidationIssue(
                    issue.code(),
                    issue.message(),
                    severityForTagIssue(operation, issue.code())
            ));
        }

        return new ProjectValidationResult(operation, issues);
    }

    private static ProjectValidationSeverity severityForNarrativeIssue(ProjectValidationOperation operation) {
        return ProjectValidationSeverity.WARNING;
    }

    private static ProjectValidationSeverity severityForTagIssue(ProjectValidationOperation operation, String code) {
        if (operation != ProjectValidationOperation.EXPORT_PUBLICATION) {
            return ProjectValidationSeverity.WARNING;
        }
        if ("duplicate-tag-id".equals(code)) {
            return ProjectValidationSeverity.BLOCKING;
        }
        return ProjectValidationSeverity.WARNING;
    }
}
