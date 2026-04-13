package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

final class DesktopBackgroundCoordinatorTest {
    @Test
    void disablesManagedButtonsWhileOperationIsRunning() throws Exception {
        AtomicBoolean busyState = new AtomicBoolean(false);
        CountDownLatch operationStarted = new CountDownLatch(1);
        CountDownLatch releaseOperation = new CountDownLatch(1);
        CountDownLatch operationFinished = new CountDownLatch(1);
        AtomicReference<JFrame> frameRef = new AtomicReference<>();
        AtomicReference<JButton> saveButtonRef = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            JLabel statusLabel = new JLabel();
            JButton saveButton = new JButton("Salvar");
            DesktopBackgroundCoordinator coordinator = new DesktopBackgroundCoordinator(frame, statusLabel);
            coordinator.registerBusyManagedButton(saveButton);
            coordinator.registerBusyStateHandler(busyState::set);

            frameRef.set(frame);
            saveButtonRef.set(saveButton);

            coordinator.run(
                    "Salvando...",
                    () -> {
                        operationStarted.countDown();
                        releaseOperation.await(2, TimeUnit.SECONDS);
                        return "ok";
                    },
                    ignored -> operationFinished.countDown(),
                    "Falha ao salvar"
            );
        });

        assertTrue(operationStarted.await(2, TimeUnit.SECONDS));
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(busyState.get());
            assertFalse(saveButtonRef.get().isEnabled());
        });

        releaseOperation.countDown();
        assertTrue(operationFinished.await(2, TimeUnit.SECONDS));
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(busyState.get());
            assertTrue(saveButtonRef.get().isEnabled());
        });

        SwingUtilities.invokeAndWait(() -> frameRef.get().dispose());
    }
}
