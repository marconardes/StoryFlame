package io.storyflame.desktop;

import io.storyflame.app.project.ProjectApplicationService;
import io.storyflame.core.model.Project;
import io.storyflame.core.publication.ProjectPublicationRequest;
import io.storyflame.core.publication.PublicationExportService;
import io.storyflame.core.publication.PublicationFormat;
import io.storyflame.core.publication.PublicationPreset;
import io.storyflame.core.storage.ProjectArchiveInspection;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectStoragePaths;
import io.storyflame.core.validation.ProjectValidationResult;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

final class DesktopProjectWorkflow {
    record LoadedProjectState(Project project, Path path, String statusPrefix) {
    }

    private record ImportedProjectState(ProjectArchiveInspection inspection, LoadedProjectState loadedProject) {
    }

    private final ProjectArchiveStore store;
    private final ProjectApplicationService projectApplicationService;
    private final PublicationExportService publicationExportService;
    private final DesktopBackgroundCoordinator backgroundCoordinator;
    private final JLabel statusLabel;

    DesktopProjectWorkflow(
            ProjectApplicationService projectApplicationService,
            ProjectArchiveStore store,
            PublicationExportService publicationExportService,
            DesktopBackgroundCoordinator backgroundCoordinator,
            JLabel statusLabel
    ) {
        this.projectApplicationService = projectApplicationService;
        this.store = store;
        this.publicationExportService = publicationExportService;
        this.backgroundCoordinator = backgroundCoordinator;
        this.statusLabel = statusLabel;
    }

    void createProject(Consumer<LoadedProjectState> onLoaded, Runnable onFailure) {
        backgroundCoordinator.run(
                DesktopOperationStatusFormatter.creatingProject(),
                () -> {
                    ProjectApplicationService.LoadedProject loadedProject =
                            projectApplicationService.createProject("Novo Projeto", "Autor");
                    return new LoadedProjectState(
                            loadedProject.project(),
                            loadedProject.path(),
                            DesktopOperationStatusFormatter.success("Projeto criado em ")
                    );
                },
                onLoaded,
                "Nao foi possivel criar o projeto.",
                onFailure
        );
    }

    void openProject(JFrame owner, Consumer<LoadedProjectState> onLoaded) {
        JFileChooser chooser = new JFileChooser(store.getBaseDirectory().toFile());
        int result = chooser.showOpenDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        openProject(chooser.getSelectedFile().toPath(), onLoaded);
    }

    void openProject(Path selectedPath, Consumer<LoadedProjectState> onLoaded) {
        backgroundCoordinator.run(
                DesktopOperationStatusFormatter.openingProject(selectedPath),
                () -> {
                    ProjectApplicationService.LoadedProject loadedProject = projectApplicationService.openProject(selectedPath);
                    return new LoadedProjectState(
                            loadedProject.project(),
                            loadedProject.path(),
                            DesktopOperationStatusFormatter.success("Projeto aberto de ")
                    );
                },
                onLoaded,
                "Nao foi possivel abrir o projeto."
        );
    }

    void saveProject(Project currentProject, Path currentPath, Consumer<Path> onSaved) {
        ProjectValidationResult validation = projectApplicationService.validateForSave(currentProject);
        boolean confirmed = backgroundCoordinator.confirmArchiveValidation(validation, "Salvar projeto");
        if (!confirmed) {
            statusLabel.setText(DesktopOperationStatusFormatter.warning("Salvamento cancelado para revisar inconsistencias."));
            return;
        }
        if (!validation.warningIssues().isEmpty()) {
            statusLabel.setText(DesktopProjectValidationFormatter.continuedWithWarningsStatus(validation));
        }
        Path previousPath = currentPath;
        backgroundCoordinator.run(
                DesktopOperationStatusFormatter.savingProject(previousPath),
                () -> projectApplicationService.saveProject(currentProject, previousPath),
                saveResult -> {
                    if (saveResult.backupFailed()) {
                        statusLabel.setText(DesktopOperationStatusFormatter.partialBackupFailure());
                    }
                    if (saveResult.retainedPreviousArchive()) {
                        statusLabel.setText(DesktopOperationStatusFormatter.retainedPreviousArchive(saveResult.path()));
                    }
                    onSaved.accept(saveResult.path());
                },
                "Nao foi possivel salvar o projeto."
        );
    }

    void exportProjectArchive(JFrame owner, Project currentProject, Path currentPath) {
        ProjectValidationResult validation = store.validateForArchiveExport(currentProject);
        boolean confirmed = backgroundCoordinator.confirmArchiveValidation(validation, "Exportar projeto");
        if (!confirmed) {
            statusLabel.setText(DesktopOperationStatusFormatter.warning("Exportacao cancelada para revisar inconsistencias."));
            return;
        }
        if (!validation.warningIssues().isEmpty()) {
            statusLabel.setText(DesktopProjectValidationFormatter.continuedWithWarningsStatus(validation));
        }
        JFileChooser chooser = new JFileChooser(store.getBaseDirectory().toFile());
        chooser.setSelectedFile((currentPath == null ? ProjectStoragePaths.suggestedArchivePath(store.getBaseDirectory(), currentProject) : currentPath).toFile());
        int result = chooser.showSaveDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path targetPath = chooser.getSelectedFile().toPath();
        Path finalTargetPath = targetPath;
        backgroundCoordinator.run(
                DesktopOperationStatusFormatter.exportingProject(finalTargetPath),
                () -> projectApplicationService.exportProjectArchive(currentProject, finalTargetPath),
                exportResult -> statusLabel.setText(DesktopOperationStatusFormatter.success("Projeto exportado para " + exportResult.path())),
                "Nao foi possivel exportar o projeto."
        );
    }

    void importProjectArchive(JFrame owner, Consumer<LoadedProjectState> onLoaded) {
        JFileChooser chooser = new JFileChooser(store.getBaseDirectory().toFile());
        int result = chooser.showOpenDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) {
            statusLabel.setText(DesktopOperationStatusFormatter.warning("Importacao cancelada."));
            return;
        }
        importProjectArchive(chooser.getSelectedFile().toPath(), owner, onLoaded);
    }

    void importProjectArchive(Path sourcePath, JFrame owner, Consumer<LoadedProjectState> onLoaded) {
        backgroundCoordinator.run(
                DesktopOperationStatusFormatter.importingProject(sourcePath),
                () -> {
                    ProjectApplicationService.ImportArchiveResult importResult =
                            projectApplicationService.importProjectArchive(sourcePath);
                    if (!importResult.inspection().valid()) {
                        return new ImportedProjectState(importResult.inspection(), null);
                    }
                    return new ImportedProjectState(
                            importResult.inspection(),
                            toLoadedProjectState(
                                    importResult.loadedProject(),
                                    importResult.inspection().requiresMigration()
                                            ? DesktopOperationStatusFormatter.warning("Projeto importado e migrado de ")
                                            : DesktopOperationStatusFormatter.success("Projeto importado de ")
                            )
                    );
                },
                importedState -> {
                    if (!importedState.inspection().valid()) {
                        JOptionPane.showMessageDialog(
                                owner,
                                DesktopArchiveInspectionFormatter.importFailureDialog(importedState.inspection()),
                                "Importar projeto",
                                JOptionPane.ERROR_MESSAGE
                        );
                        statusLabel.setText(DesktopArchiveInspectionFormatter.importFailureStatus(importedState.inspection()));
                        return;
                    }
                    onLoaded.accept(importedState.loadedProject());
                },
                "Nao foi possivel importar o projeto."
        );
    }

    void inspectProjectArchive(JFrame owner) {
        JFileChooser chooser = new JFileChooser(store.getBaseDirectory().toFile());
        int result = chooser.showOpenDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) {
            statusLabel.setText(DesktopArchiveInspectionFormatter.inspectionCancelledStatus());
            return;
        }
        Path sourcePath = chooser.getSelectedFile().toPath();
        backgroundCoordinator.run(
                DesktopOperationStatusFormatter.inspectingArchive(sourcePath),
                () -> projectApplicationService.inspectProjectArchive(sourcePath),
                inspectResult -> {
                    ProjectArchiveInspection inspection = inspectResult.inspection();
                    JOptionPane.showMessageDialog(
                            owner,
                            DesktopArchiveInspectionFormatter.inspectionDialog(inspection),
                            "Verificar arquivo",
                            inspection.valid() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                    );
                    statusLabel.setText(DesktopArchiveInspectionFormatter.inspectionStatus(inspection));
                },
                "Nao foi possivel verificar o arquivo."
        );
    }

    void showPublicationFormatDialog(JFrame owner, Project currentProject, Path currentPath) {
        Object[] options = DesktopPublicationExportFormatter.presetOptions();
        Object selectedOption = JOptionPane.showInputDialog(
                owner,
                DesktopPublicationExportFormatter.presetDialogMessage(),
                "Publicar manuscrito",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                DesktopPublicationExportFormatter.presetLabel(PublicationPreset.EDITORIAL_PROOF)
        );
        PublicationPreset selectedPreset = DesktopPublicationExportFormatter.presetFromOption(selectedOption);
        if (selectedPreset == null) {
            statusLabel.setText(DesktopPublicationExportFormatter.cancelledStatus());
            return;
        }
        statusLabel.setText(DesktopPublicationExportFormatter.presetChosenStatus(selectedPreset));
        exportPublishable(owner, currentProject, currentPath, selectedPreset);
    }

    void exportPublishable(JFrame owner, Project currentProject, Path currentPath, PublicationPreset preset) {
        exportPublishable(owner, currentProject, currentPath, preset.format(), preset);
    }

    void exportPublishable(JFrame owner, Project currentProject, Path currentPath, PublicationFormat format) {
        exportPublishable(owner, currentProject, currentPath, format, null);
    }

    private void exportPublishable(
            JFrame owner,
            Project currentProject,
            Path currentPath,
            PublicationFormat format,
            PublicationPreset preset
    ) {
        ProjectValidationResult validation = publicationExportService.validate(
                new ProjectPublicationRequest(currentProject, format, suggestPublicationPath(currentProject, currentPath, format))
        );
        if (validation.hasBlockingIssues()) {
            JOptionPane.showMessageDialog(
                    owner,
                    DesktopProjectValidationFormatter.publicationBlockingDialog(validation),
                    "Publicacao do manuscrito bloqueada",
                    JOptionPane.ERROR_MESSAGE
            );
            statusLabel.setText(DesktopOperationStatusFormatter.failure("Publicacao do manuscrito bloqueada por inconsistencias do projeto."));
            return;
        }
        JFileChooser chooser = new JFileChooser(store.getBaseDirectory().toFile());
        chooser.setDialogTitle(preset == null
                ? DesktopPublicationExportFormatter.chooserTitle(format)
                : DesktopPublicationExportFormatter.chooserTitle(preset));
        FileNameExtensionFilter filter = DesktopPublicationExportFormatter.chooserFilter(format);
        chooser.setFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setSelectedFile(suggestPublicationPath(currentProject, currentPath, format).toFile());
        int result = chooser.showSaveDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) {
            statusLabel.setText(DesktopPublicationExportFormatter.cancelledStatus());
            return;
        }
        Path targetPath = chooser.getSelectedFile().toPath();
        if (!targetPath.getFileName().toString().toLowerCase().endsWith(format.extension())) {
            targetPath = targetPath.resolveSibling(targetPath.getFileName() + format.extension());
        }
        exportPublishable(currentProject, currentPath, format, targetPath, true);
    }

    void exportPublishable(Project currentProject, Path currentPath, PublicationFormat format, Path targetPath, boolean openAfterExport) {
        Path finalTargetPath = targetPath;
        if (!finalTargetPath.getFileName().toString().toLowerCase().endsWith(format.extension())) {
            finalTargetPath = finalTargetPath.resolveSibling(finalTargetPath.getFileName() + format.extension());
        }
        Path exportPath = finalTargetPath;
        backgroundCoordinator.run(
                DesktopOperationStatusFormatter.exportingPublication(format, exportPath),
                () -> {
                    publicationExportService.export(new ProjectPublicationRequest(currentProject, format, exportPath));
                    return exportPath;
                },
                exportedPath -> {
                    if (openAfterExport) {
                        openExportedFile(format, exportedPath);
                        return;
                    }
                    statusLabel.setText(DesktopPublicationExportFormatter.exportedMessage(format, exportedPath));
                },
                DesktopPublicationExportFormatter.failureMessage(format)
        );
    }

    private void openExportedFile(PublicationFormat format, Path targetPath) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            statusLabel.setText(DesktopPublicationExportFormatter.exportedMessage(format, targetPath));
            return;
        }
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (!desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    return false;
                }
                desktop.open(targetPath.toFile());
                return true;
            }

            @Override
            protected void done() {
                try {
                    boolean opened = get();
                    statusLabel.setText(opened
                            ? DesktopPublicationExportFormatter.exportedAndOpenedMessage(format, targetPath)
                            : DesktopPublicationExportFormatter.exportedMessage(format, targetPath));
                } catch (Exception exception) {
                    statusLabel.setText(DesktopPublicationExportFormatter.exportedOpenFailedMessage(format, targetPath));
                }
            }
        }.execute();
    }

    private LoadedProjectState toLoadedProjectState(ProjectApplicationService.LoadedProject loadedProject, String statusPrefix) {
        return new LoadedProjectState(loadedProject.project(), loadedProject.path(), statusPrefix);
    }

    private Path suggestPublicationPath(Project currentProject, Path currentPath, PublicationFormat format) {
        String baseName = currentProject == null || currentProject.getTitle().isBlank()
                ? "storyflame-manuscrito"
                : ProjectStoragePaths.sanitize(currentProject.getTitle());
        if (baseName.isBlank()) {
            baseName = "storyflame-manuscrito";
        }
        Path baseDirectory = currentPath == null
                ? store.getBaseDirectory()
                : currentPath.toAbsolutePath().normalize().getParent();
        return baseDirectory.resolve(baseName + format.extension());
    }

}
