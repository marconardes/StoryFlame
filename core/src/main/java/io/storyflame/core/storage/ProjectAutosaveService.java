package io.storyflame.core.storage;

import io.storyflame.core.analysis.EmotionAnalysisReport;
import io.storyflame.core.analysis.EmotionCache;
import io.storyflame.core.analysis.EmotionCacheEntry;
import io.storyflame.core.analysis.EmotionChunkAnalysis;
import io.storyflame.core.analysis.EmotionLabel;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.NarrativeTag;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ProjectAutosaveService implements AutoCloseable {
    private final ProjectArchiveStore store;
    private final Duration delay;
    private final Scheduler scheduler;
    private ScheduledFuture<?> pendingSave;

    public ProjectAutosaveService(ProjectArchiveStore store, Duration delay) {
        this(store, delay, Scheduler.threadBacked());
    }

    ProjectAutosaveService(ProjectArchiveStore store, Duration delay, Scheduler scheduler) {
        this.store = Objects.requireNonNull(store);
        this.delay = Objects.requireNonNull(delay);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    public synchronized void schedule(Project project, Path path, Runnable onSaved) {
        schedule(project, path, onSaved, null);
    }

    public synchronized void schedule(Project project, Path path, Runnable onSaved, Consumer<Exception> onError) {
        if (pendingSave != null) {
            pendingSave.cancel(false);
        }
        Project snapshot = snapshot(project);
        pendingSave = scheduler.schedule(() -> {
            try {
                store.save(snapshot, path);
                if (onSaved != null) {
                    onSaved.run();
                }
            } catch (Exception exception) {
                if (onError != null) {
                    onError.accept(exception);
                }
            }
        }, delay);
    }

    @Override
    public synchronized void close() {
        if (pendingSave != null) {
            pendingSave.cancel(false);
        }
        scheduler.shutdown();
    }

    interface Scheduler {
        ScheduledFuture<?> schedule(Runnable task, Duration delay);

        void shutdown();

        static Scheduler threadBacked() {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "storyflame-autosave");
                thread.setDaemon(true);
                return thread;
            });
            return new Scheduler() {
                @Override
                public ScheduledFuture<?> schedule(Runnable task, Duration delay) {
                    return executorService.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
                }

                @Override
                public void shutdown() {
                    executorService.shutdownNow();
                }
            };
        }
    }

    private static Project snapshot(Project project) {
        Objects.requireNonNull(project);
        return new Project(
                project.getId(),
                project.getTitle(),
                project.getAuthor(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                copyChapters(project.getChapters()),
                copyCharacters(project.getCharacters()),
                copyNarrativeTags(project.getNarrativeTags()),
                copyCharacterTagProfiles(project.getCharacterTagProfiles()),
                copyEmotionAnalysis(project.getEmotionAnalysis()),
                copyEmotionCache(project.getEmotionCache())
        );
    }

    private static List<Chapter> copyChapters(List<Chapter> chapters) {
        List<Chapter> copies = new ArrayList<>();
        for (Chapter chapter : chapters) {
            copies.add(new Chapter(
                    chapter.getId(),
                    chapter.getTitle(),
                    copyScenes(chapter.getScenes())
            ));
        }
        return copies;
    }

    private static List<Scene> copyScenes(List<Scene> scenes) {
        List<Scene> copies = new ArrayList<>();
        for (Scene scene : scenes) {
            copies.add(new Scene(
                    scene.getId(),
                    scene.getTitle(),
                    scene.getSynopsis(),
                    scene.getContent(),
                    scene.getPointOfViewCharacterId()
            ));
        }
        return copies;
    }

    private static List<Character> copyCharacters(List<Character> characters) {
        List<Character> copies = new ArrayList<>();
        for (Character character : characters) {
            copies.add(new Character(
                    character.getId(),
                    character.getName(),
                    character.getDescription()
            ));
        }
        return copies;
    }

    private static List<NarrativeTag> copyNarrativeTags(List<NarrativeTag> tags) {
        return tags == null ? List.of() : new ArrayList<>(tags);
    }

    private static List<CharacterTagProfile> copyCharacterTagProfiles(List<CharacterTagProfile> profiles) {
        List<CharacterTagProfile> copies = new ArrayList<>();
        for (CharacterTagProfile profile : profiles) {
            copies.add(new CharacterTagProfile(
                    profile.getCharacterId(),
                    profile.getPrefix(),
                    List.copyOf(profile.getPreferredTagIds())
            ));
        }
        return copies;
    }

    private static EmotionAnalysisReport copyEmotionAnalysis(EmotionAnalysisReport report) {
        if (report == null) {
            return null;
        }
        return new EmotionAnalysisReport(
                report.generatedAt(),
                report.chunkCount(),
                report.overallSentiment(),
                report.dominantEmotion(),
                copyEmotionScores(report.averageEmotionScores()),
                copyChunkAnalyses(report.chunks())
        );
    }

    private static List<EmotionChunkAnalysis> copyChunkAnalyses(List<EmotionChunkAnalysis> chunks) {
        if (chunks == null) {
            return List.of();
        }
        List<EmotionChunkAnalysis> copies = new ArrayList<>();
        for (EmotionChunkAnalysis chunk : chunks) {
            copies.add(new EmotionChunkAnalysis(
                    chunk.chapterId(),
                    chunk.chapterTitle(),
                    chunk.sceneId(),
                    chunk.sceneTitle(),
                    chunk.excerpt(),
                    chunk.wordCount(),
                    chunk.chunkIndex(),
                    chunk.sentiment(),
                    chunk.dominantEmotion(),
                    copyEmotionScores(chunk.emotionScores())
            ));
        }
        return copies;
    }

    private static Map<EmotionLabel, Double> copyEmotionScores(Map<EmotionLabel, Double> scores) {
        return scores == null ? Map.of() : new LinkedHashMap<>(scores);
    }

    private static EmotionCache copyEmotionCache(EmotionCache cache) {
        if (cache == null) {
            return new EmotionCache();
        }
        List<EmotionCacheEntry> copiedEntries = new ArrayList<>();
        for (EmotionCacheEntry entry : cache.getEntries()) {
            copiedEntries.add(new EmotionCacheEntry(
                    entry.sceneId(),
                    entry.contentHash(),
                    copyEmotionChunkAnalysis(entry.analysis())
            ));
        }
        return new EmotionCache(copiedEntries);
    }

    private static EmotionChunkAnalysis copyEmotionChunkAnalysis(EmotionChunkAnalysis chunk) {
        if (chunk == null) {
            return null;
        }
        return new EmotionChunkAnalysis(
                chunk.chapterId(),
                chunk.chapterTitle(),
                chunk.sceneId(),
                chunk.sceneTitle(),
                chunk.excerpt(),
                chunk.wordCount(),
                chunk.chunkIndex(),
                chunk.sentiment(),
                chunk.dominantEmotion(),
                copyEmotionScores(chunk.emotionScores())
        );
    }
}
