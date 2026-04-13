package io.storyflame.core.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectAutosaveServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void savesProjectAndInvokesSuccessCallback() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        CountDownLatch savedLatch = new CountDownLatch(1);

        try (ProjectAutosaveService autosaveService = new ProjectAutosaveService(store, Duration.ofMillis(25))) {
            Path archivePath = tempDir.resolve("autosave.storyflame");
            autosaveService.schedule(Project.blank("Autosave", "Ana"), archivePath, savedLatch::countDown, null);

            assertTrue(savedLatch.await(2, TimeUnit.SECONDS));
            assertTrue(Files.exists(archivePath));
        }
    }

    @Test
    void invokesErrorCallbackWhenSaveFails() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        Path blockedParent = tempDir.resolve("blocked");
        Files.writeString(blockedParent, "not-a-directory");
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        try (ProjectAutosaveService autosaveService = new ProjectAutosaveService(store, Duration.ofMillis(25))) {
            autosaveService.schedule(
                    Project.blank("Broken Autosave", "Ana"),
                    blockedParent.resolve("autosave.storyflame"),
                    () -> {
                    },
                    exception -> {
                        errorRef.set(exception);
                        errorLatch.countDown();
                    }
            );

            assertTrue(errorLatch.await(2, TimeUnit.SECONDS));
            assertFalse(Files.exists(blockedParent.resolve("autosave.storyflame")));
            assertNotNull(errorRef.get());
            assertEquals(UncheckedIOException.class, errorRef.get().getClass());
        }
    }

    @Test
    void savesSnapshotCapturedAtScheduleTime() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        CountDownLatch savedLatch = new CountDownLatch(1);
        Project project = Project.blank("Autosave", "Ana");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo",
                List.of(new Scene("scene-1", "Cena", "versao inicial", null))
        ));

        try (ProjectAutosaveService autosaveService = new ProjectAutosaveService(store, Duration.ofMillis(100))) {
            Path archivePath = tempDir.resolve("autosave-snapshot.storyflame");
            autosaveService.schedule(project, archivePath, savedLatch::countDown, null);

            project.setTitle("Titulo alterado apos agendamento");
            project.getChapters().get(0).getScenes().get(0).setContent("versao alterada apos agendamento");

            assertTrue(savedLatch.await(2, TimeUnit.SECONDS));

            Project loaded = store.open(archivePath);
            assertEquals("Autosave", loaded.getTitle());
            assertEquals("versao inicial", loaded.getChapters().get(0).getScenes().get(0).getContent());
        }
    }

    @Test
    void savesOnlyLatestScheduledSnapshotWhenUsingDeterministicScheduler() throws Exception {
        ProjectArchiveStore store = new ProjectArchiveStore(tempDir);
        ManualScheduler scheduler = new ManualScheduler();
        Project project = Project.blank("Versao 1", "Ana");
        project.getChapters().add(new Chapter(
                "chapter-1",
                "Capitulo",
                new ArrayList<>(List.of(new Scene("scene-1", "Cena", "conteudo 1", null)))
        ));

        try (ProjectAutosaveService autosaveService = new ProjectAutosaveService(store, Duration.ofSeconds(1), scheduler)) {
            Path archivePath = tempDir.resolve("autosave-deterministic.storyflame");

            autosaveService.schedule(project, archivePath, null, null);
            project.setTitle("Versao 2");
            project.getChapters().get(0).getScenes().get(0).setContent("conteudo 2");
            autosaveService.schedule(project, archivePath, null, null);

            scheduler.runAll();

            Project loaded = store.open(archivePath);
            assertEquals("Versao 2", loaded.getTitle());
            assertEquals("conteudo 2", loaded.getChapters().get(0).getScenes().get(0).getContent());
            assertEquals(2, scheduler.scheduledCount());
            assertEquals(1, scheduler.executedCount());
        }
    }

    private static final class ManualScheduler implements ProjectAutosaveService.Scheduler {
        private final List<ManualScheduledFuture> scheduled = new ArrayList<>();
        private int executedCount;

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Duration delay) {
            ManualScheduledFuture future = new ManualScheduledFuture(task);
            scheduled.add(future);
            return future;
        }

        @Override
        public void shutdown() {
        }

        void runAll() {
            for (ManualScheduledFuture future : scheduled) {
                if (!future.isCancelled()) {
                    future.run();
                    executedCount++;
                }
            }
        }

        int scheduledCount() {
            return scheduled.size();
        }

        int executedCount() {
            return executedCount;
        }
    }

    private static final class ManualScheduledFuture implements ScheduledFuture<Object> {
        private final Runnable task;
        private boolean cancelled;
        private boolean done;

        private ManualScheduledFuture(Runnable task) {
            this.task = task;
        }

        void run() {
            if (cancelled || done) {
                return;
            }
            task.run();
            done = true;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (done) {
                return false;
            }
            cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return done || cancelled;
        }

        @Override
        public Object get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    }
}
