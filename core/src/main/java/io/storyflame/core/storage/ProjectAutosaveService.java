package io.storyflame.core.storage;

import io.storyflame.core.model.Project;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ProjectAutosaveService implements AutoCloseable {
    private final ScheduledExecutorService executorService;
    private final ProjectArchiveStore store;
    private final Duration delay;
    private ScheduledFuture<?> pendingSave;

    public ProjectAutosaveService(ProjectArchiveStore store, Duration delay) {
        this.store = Objects.requireNonNull(store);
        this.delay = Objects.requireNonNull(delay);
        this.executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "storyflame-autosave");
            thread.setDaemon(true);
            return thread;
        });
    }

    public synchronized void schedule(Project project, Path path, Runnable onSaved) {
        if (pendingSave != null) {
            pendingSave.cancel(false);
        }
        pendingSave = executorService.schedule(() -> {
            store.save(project, path);
            if (onSaved != null) {
                onSaved.run();
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void close() {
        if (pendingSave != null) {
            pendingSave.cancel(false);
        }
        executorService.shutdownNow();
    }
}

