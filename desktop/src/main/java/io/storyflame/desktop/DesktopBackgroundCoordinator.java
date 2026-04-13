package io.storyflame.desktop;

import io.storyflame.core.validation.ProjectValidationResult;
import java.awt.Cursor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

final class DesktopBackgroundCoordinator {
    @FunctionalInterface
    interface Operation<T> {
        T run() throws Exception;
    }

    private final JFrame frame;
    private final JLabel statusLabel;
    private final Map<AbstractButton, Boolean> busyManagedButtons;
    private final List<Consumer<Boolean>> busyStateHandlers;
    private boolean busy;

    DesktopBackgroundCoordinator(JFrame frame, JLabel statusLabel) {
        this.frame = frame;
        this.statusLabel = statusLabel;
        this.busyManagedButtons = new LinkedHashMap<>();
        this.busyStateHandlers = new ArrayList<>();
        this.busy = false;
    }

    void registerBusyManagedButton(AbstractButton button) {
        if (button != null) {
            busyManagedButtons.put(button, button.isEnabled());
        }
    }

    void registerBusyStateHandler(Consumer<Boolean> handler) {
        if (handler != null) {
            busyStateHandlers.add(handler);
            handler.accept(busy);
        }
    }

    boolean isBusy() {
        return busy;
    }

    <T> void run(
            String busyMessage,
            Operation<T> operation,
            Consumer<T> onSuccess,
            String failureMessage
    ) {
        run(busyMessage, operation, onSuccess, failureMessage, () -> {
        });
    }

    <T> void run(
            String busyMessage,
            Operation<T> operation,
            Consumer<T> onSuccess,
            String failureMessage,
            Runnable onFailure
    ) {
        setBusyState(true, busyMessage);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operation.run();
            }

            @Override
            protected void done() {
                setBusyState(false, null);
                try {
                    onSuccess.accept(get());
                } catch (Exception exception) {
                    onFailure.run();
                    showOperationFailure(failureMessage, exception);
                }
            }
        }.execute();
    }

    boolean confirmArchiveValidation(ProjectValidationResult validation, String dialogTitle) {
        if (validation == null || !validation.hasIssues()) {
            return true;
        }
        int confirmation = JOptionPane.showConfirmDialog(
                frame,
                DesktopProjectValidationFormatter.archiveWarningDialog(validation),
                dialogTitle,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return confirmation == JOptionPane.YES_OPTION;
    }

    void handleAutosaveFailure(Path autosavePath, Exception exception) {
        Throwable rootCause = exception.getCause() != null ? exception.getCause() : exception;
        String detail = rootCause.getMessage() == null || rootCause.getMessage().isBlank()
                ? rootCause.getClass().getSimpleName()
                : rootCause.getMessage();
        statusLabel.setText(DesktopOperationStatusFormatter.failure("Falha ao salvar a versao automatica em " + autosavePath + "."));
        JOptionPane.showMessageDialog(
                frame,
                "Nao foi possivel salvar a versao automatica.\n"
                        + "Suas alteracoes continuam no editor.\n\n"
                        + detail,
                "Autosave",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void setBusyState(boolean busy, String message) {
        this.busy = busy;
        frame.setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        frame.getGlassPane().setVisible(busy);
        busyManagedButtons.forEach((button, wasEnabled) -> button.setEnabled(busy ? false : wasEnabled));
        busyStateHandlers.forEach(handler -> handler.accept(busy));
        if (message != null && !message.isBlank()) {
            statusLabel.setText(message);
        }
    }

    private void showOperationFailure(String failureMessage, Exception exception) {
        Throwable rootCause = exception.getCause() != null ? exception.getCause() : exception;
        String detail = rootCause.getMessage() == null || rootCause.getMessage().isBlank()
                ? rootCause.getClass().getSimpleName()
                : rootCause.getMessage();
        statusLabel.setText(DesktopOperationStatusFormatter.failure(failureMessage));
        JOptionPane.showMessageDialog(
                frame,
                failureMessage + "\n" + detail,
                "Operacao falhou",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
