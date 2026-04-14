package io.storyflame.app.project;

import io.storyflame.core.model.Project;
import io.storyflame.core.publication.ProjectPublicationRequest;
import io.storyflame.core.publication.PublicationExportService;
import io.storyflame.core.publication.PublicationFormat;
import java.nio.file.Path;
import java.util.Objects;

public final class ProjectPublicationApplicationService {
    private final PublicationExportService exportService;

    public ProjectPublicationApplicationService(PublicationExportService exportService) {
        this.exportService = Objects.requireNonNull(exportService);
    }

    public ProjectValidationDto validate(Project project, PublicationFormat format) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(format);
        Path probePath = Path.of("publication" + format.extension());
        return ProjectValidationDto.from(exportService.validate(new ProjectPublicationRequest(project, format, probePath)));
    }

    public PublicationOperationResult export(Project project, PublicationFormat format, Path targetPath) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(format);
        Objects.requireNonNull(targetPath);
        ProjectValidationDto validation = validate(project, format);
        if (validation.hasBlockingIssues()) {
            String message = validation.issues().stream()
                    .filter(issue -> "BLOCKING".equals(issue.severity()))
                    .map(ProjectIssueDto::message)
                    .findFirst()
                    .orElse("Publicacao bloqueada.");
            throw new IllegalStateException(message);
        }
        Path exportedPath = exportService.export(new ProjectPublicationRequest(project, format, targetPath));
        return new PublicationOperationResult(
                exportedPath,
                validation,
                "Manuscrito publicado em " + exportedPath
        );
    }
}
