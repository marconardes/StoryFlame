package io.storyflame.app.electron;

import io.storyflame.app.project.CreateProjectRequest;
import io.storyflame.app.project.OpenProjectRequest;
import io.storyflame.app.project.ProjectApplicationService;
import io.storyflame.app.project.ProjectCharacterApplicationService;
import io.storyflame.app.project.ProjectEditorApplicationService;
import io.storyflame.app.project.ProjectStructureApplicationService;
import io.storyflame.app.project.ProjectTagApplicationService;
import io.storyflame.app.project.ProjectValidationDto;
import io.storyflame.app.project.SaveProjectRequest;
import io.storyflame.core.analysis.EmotionAnalysisReport;
import io.storyflame.core.analysis.EmotionAnalysisService;
import io.storyflame.core.analysis.EmotionChunkAnalysis;
import io.storyflame.core.model.Character;
import io.storyflame.core.model.Chapter;
import io.storyflame.core.model.Project;
import io.storyflame.core.model.Scene;
import io.storyflame.core.search.ProjectSearch;
import io.storyflame.core.search.SearchMatch;
import io.storyflame.core.tags.CharacterTagProfile;
import io.storyflame.core.tags.CharacterTagProfileSynchronizer;
import io.storyflame.core.tags.NarrativeTag;
import io.storyflame.core.tags.NarrativeTagIdPolicy;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

final class ElectronBridgeSessionService {
    private final ProjectApplicationService projectApplicationService;
    private final ProjectCharacterApplicationService projectCharacterApplicationService;
    private final ProjectEditorApplicationService projectEditorApplicationService;
    private final ProjectStructureApplicationService projectStructureApplicationService;
    private final ProjectTagApplicationService projectTagApplicationService;
    private final EmotionAnalysisService emotionAnalysisService;
    private Project currentProject;
    private Path currentPath;
    private String selectedChapterId;
    private String selectedSceneId;
    private String selectedCharacterId;
    private String selectedTagId;
    private String selectedProfileCharacterId;

    ElectronBridgeSessionService(
            ProjectApplicationService projectApplicationService,
            ProjectCharacterApplicationService projectCharacterApplicationService,
            ProjectEditorApplicationService projectEditorApplicationService,
            ProjectStructureApplicationService projectStructureApplicationService,
            ProjectTagApplicationService projectTagApplicationService,
            EmotionAnalysisService emotionAnalysisService
    ) {
        this.projectApplicationService = Objects.requireNonNull(projectApplicationService);
        this.projectCharacterApplicationService = Objects.requireNonNull(projectCharacterApplicationService);
        this.projectEditorApplicationService = Objects.requireNonNull(projectEditorApplicationService);
        this.projectStructureApplicationService = Objects.requireNonNull(projectStructureApplicationService);
        this.projectTagApplicationService = Objects.requireNonNull(projectTagApplicationService);
        this.emotionAnalysisService = Objects.requireNonNull(emotionAnalysisService);
    }

    synchronized BridgeResult currentSession() {
        if (currentProject == null || currentPath == null) {
            return new BridgeResult("Nenhum projeto carregado.", null);
        }
        return new BridgeResult("Projeto atual carregado.", snapshot("Projeto atual carregado."));
    }

    synchronized BridgeResult createProject(String title, String author) {
        var result = projectApplicationService.createProject(new CreateProjectRequest(title, author));
        currentProject = result.project();
        currentPath = result.path();
        ensureSelection();
        ensureCharacterSelection();
        ensureTagSelection();
        ensureProfileSelection();
        return new BridgeResult(result.message(), snapshot(result.message()));
    }

    synchronized BridgeResult openProject(String path) {
        var result = projectApplicationService.openProject(new OpenProjectRequest(Path.of(path)));
        currentProject = result.project();
        currentPath = result.path();
        ensureSelection();
        ensureCharacterSelection();
        ensureTagSelection();
        ensureProfileSelection();
        return new BridgeResult(result.message(), snapshot(result.message()));
    }

    synchronized BridgeResult updateMetadata(String title, String author) {
        ensureSession();
        projectEditorApplicationService.updateProjectMetadata(currentProject, title, author);
        return new BridgeResult("Metadados atualizados.", snapshot("Metadados atualizados."));
    }

    synchronized BridgeResult saveProject() {
        ensureSession();
        var result = projectApplicationService.saveProject(new SaveProjectRequest(currentProject, currentPath));
        currentProject = result.project();
        currentPath = result.path();
        ensureSelection();
        ensureCharacterSelection();
        ensureTagSelection();
        ensureProfileSelection();
        return new BridgeResult(result.message(), snapshot(result.message()));
    }

    synchronized BridgeResult selectScene(String chapterId, String sceneId) {
        ensureSession();
        Chapter chapter = findChapter(chapterId);
        Scene scene = findScene(chapter, sceneId);
        selectedChapterId = chapter.getId();
        selectedSceneId = scene.getId();
        return new BridgeResult("Cena selecionada.", snapshot("Cena selecionada."));
    }

    synchronized BridgeResult updateSceneDraft(String title, String synopsis, String content) {
        ensureSession();
        Scene selectedScene = selectedScene();
        projectEditorApplicationService.updateSceneDraft(currentProject, selectedScene, title, synopsis, content);
        return new BridgeResult("Cena atualizada.", snapshot("Cena atualizada."));
    }

    synchronized BridgeResult addChapter() {
        ensureSession();
        var selection = projectStructureApplicationService.addChapter(currentProject);
        selectedChapterId = selection.chapter().getId();
        selectedSceneId = selection.scene().getId();
        return new BridgeResult("Capitulo criado.", snapshot("Capitulo criado."));
    }

    synchronized BridgeResult removeChapter() {
        ensureSession();
        var selection = projectStructureApplicationService.removeChapter(currentProject, selectedChapter());
        selectedChapterId = selection.chapter().getId();
        selectedSceneId = selection.scene().getId();
        return new BridgeResult("Capitulo removido.", snapshot("Capitulo removido."));
    }

    synchronized BridgeResult moveChapter(int offset) {
        ensureSession();
        var selection = projectStructureApplicationService.moveChapter(currentProject, selectedChapter(), offset);
        selectedChapterId = selection.chapter().getId();
        selectedSceneId = selection.scene().getId();
        return new BridgeResult("Capitulo movido.", snapshot("Capitulo movido."));
    }

    synchronized BridgeResult addScene() {
        ensureSession();
        var selection = projectStructureApplicationService.addScene(currentProject, selectedChapter());
        selectedChapterId = selection.chapter().getId();
        selectedSceneId = selection.scene().getId();
        return new BridgeResult("Cena criada.", snapshot("Cena criada."));
    }

    synchronized BridgeResult removeScene() {
        ensureSession();
        var selection = projectStructureApplicationService.removeScene(currentProject, selectedChapter(), selectedScene());
        selectedChapterId = selection.chapter().getId();
        selectedSceneId = selection.scene().getId();
        return new BridgeResult("Cena removida.", snapshot("Cena removida."));
    }

    synchronized BridgeResult moveScene(int offset) {
        ensureSession();
        var selection = projectStructureApplicationService.moveScene(currentProject, selectedChapter(), selectedScene(), offset);
        selectedChapterId = selection.chapter().getId();
        selectedSceneId = selection.scene().getId();
        return new BridgeResult("Cena movida.", snapshot("Cena movida."));
    }

    synchronized BridgeResult selectCharacter(String characterId) {
        ensureSession();
        Character character = findCharacter(characterId);
        selectedCharacterId = character.getId();
        return new BridgeResult("Personagem selecionado.", snapshot("Personagem selecionado."));
    }

    synchronized BridgeResult createCharacter(String name, String description) {
        ensureSession();
        Character character = projectCharacterApplicationService.createCharacter(currentProject, name, description);
        selectedCharacterId = character.getId();
        return new BridgeResult("Personagem criado.", snapshot("Personagem criado."));
    }

    synchronized BridgeResult updateCharacter(String name, String description) {
        ensureSession();
        Character character = selectedCharacter();
        if (character == null) {
            throw new IllegalStateException("Nenhum personagem selecionado.");
        }
        projectCharacterApplicationService.updateCharacter(currentProject, character, name, description);
        return new BridgeResult("Personagem atualizado.", snapshot("Personagem atualizado."));
    }

    synchronized BridgeResult deleteCharacter() {
        ensureSession();
        Character character = selectedCharacter();
        if (character == null) {
            throw new IllegalStateException("Nenhum personagem selecionado.");
        }
        Character remaining = projectCharacterApplicationService.deleteCharacter(currentProject, character);
        selectedCharacterId = remaining == null ? null : remaining.getId();
        ensureCharacterSelection();
        return new BridgeResult("Personagem removido.", snapshot("Personagem removido."));
    }

    synchronized BridgeResult selectTag(String tagId) {
        ensureSession();
        NarrativeTag tag = findTag(tagId);
        selectedTagId = tag.id();
        return new BridgeResult("Tag selecionada.", snapshot("Tag selecionada."));
    }

    synchronized BridgeResult createTag(String id, String label, String template) {
        ensureSession();
        String normalizedId = normalizeNewTagId(id, label);
        NarrativeTag tag = projectTagApplicationService.createTag(currentProject, normalizedId, label, template);
        selectedTagId = tag.id();
        return new BridgeResult("Tag criada.", snapshot("Tag criada."));
    }

    synchronized BridgeResult updateTag(String id, String label, String template) {
        ensureSession();
        NarrativeTag selectedTag = selectedTag();
        if (selectedTag == null) {
            throw new IllegalStateException("Nenhuma tag selecionada.");
        }
        String normalizedId = normalizeExistingTagId(id, selectedTag.id());
        NarrativeTag updatedTag = projectTagApplicationService.updateTag(
                currentProject,
                selectedTag,
                normalizedId,
                label,
                template
        );
        selectedTagId = updatedTag.id();
        return new BridgeResult("Tag atualizada.", snapshot("Tag atualizada."));
    }

    synchronized BridgeResult deleteTag() {
        ensureSession();
        NarrativeTag selectedTag = selectedTag();
        if (selectedTag == null) {
            throw new IllegalStateException("Nenhuma tag selecionada.");
        }
        NarrativeTag remaining = projectTagApplicationService.deleteTag(currentProject, selectedTag);
        selectedTagId = remaining == null ? null : remaining.id();
        ensureTagSelection();
        ensureProfileSelection();
        return new BridgeResult("Tag removida.", snapshot("Tag removida."));
    }

    synchronized BridgeResult selectProfile(String characterId) {
        ensureSession();
        CharacterTagProfile profile = findProfile(characterId);
        selectedProfileCharacterId = profile.getCharacterId();
        return new BridgeResult("Perfil selecionado.", snapshot("Perfil selecionado."));
    }

    synchronized BridgeResult updateProfilePrefix(String prefix) {
        ensureSession();
        CharacterTagProfile profile = selectedProfile();
        if (profile == null) {
            throw new IllegalStateException("Nenhum perfil selecionado.");
        }
        profile.setPrefix(prefix == null ? "" : prefix);
        currentProject.touch();
        return new BridgeResult("Prefixo atualizado.", snapshot("Prefixo atualizado."));
    }

    synchronized BridgeResult addTagToProfile(String tagId) {
        ensureSession();
        CharacterTagProfile profile = selectedProfile();
        if (profile == null) {
            throw new IllegalStateException("Nenhum perfil selecionado.");
        }
        NarrativeTag tag = findTag(tagId);
        projectTagApplicationService.addTagToProfile(currentProject, profile, tag);
        return new BridgeResult("Tag adicionada ao perfil.", snapshot("Tag adicionada ao perfil."));
    }

    synchronized BridgeResult removeTagFromProfile(String tagId) {
        ensureSession();
        CharacterTagProfile profile = selectedProfile();
        if (profile == null) {
            throw new IllegalStateException("Nenhum perfil selecionado.");
        }
        NarrativeTag tag = findTag(tagId);
        projectTagApplicationService.removeTagFromProfile(currentProject, profile, tag);
        return new BridgeResult("Tag removida do perfil.", snapshot("Tag removida do perfil."));
    }

    synchronized List<SearchResultDto> search(String query) {
        ensureSession();
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return ProjectSearch.search(currentProject, query).stream()
                .map(this::toSearchResultDto)
                .toList();
    }

    synchronized EmotionReportDto runEmotionAnalysis() {
        ensureSession();
        EmotionAnalysisReport report = emotionAnalysisService.analyze(currentProject);
        return report == null ? null : toEmotionReportDto(report);
    }

    synchronized EmotionReportDto currentEmotionAnalysis() {
        ensureSession();
        EmotionAnalysisReport report = currentProject.getEmotionAnalysis();
        return report == null ? null : toEmotionReportDto(report);
    }

    synchronized String baseDirectory() {
        return projectApplicationService.baseDirectory().toString();
    }

    synchronized List<LocalProjectDto> localProjects() {
        return projectApplicationService.listProjects().stream()
                .map(path -> new LocalProjectDto(path.toString(), path.getFileName().toString()))
                .toList();
    }

    synchronized BridgeResult deleteLocalProject(String path) {
        Objects.requireNonNull(path);
        boolean deleted = projectApplicationService.deleteProject(Path.of(path));
        if (currentPath != null && currentPath.toAbsolutePath().normalize().equals(Path.of(path).toAbsolutePath().normalize())) {
            clearSession();
        }
        return new BridgeResult(
                deleted ? "Projeto removido." : "Projeto nao encontrado.",
                currentProject == null ? null : snapshot("Projeto atual carregado.")
        );
    }

    private BridgeSessionDto snapshot(String message) {
        ensureSelection();
        ensureCharacterSelection();
        ensureTagSelection();
        ensureProfileSelection();
        Scene selectedScene = selectedScene();
        Character selectedCharacter = selectedCharacter();
        NarrativeTag selectedTag = selectedTag();
        CharacterTagProfile selectedProfile = selectedProfile();
        return new BridgeSessionDto(
                currentPath.toString(),
                ProjectValidationDto.from(projectApplicationService.validateForSave(currentProject)),
                message,
                new SelectionDto(selectedChapterId, selectedSceneId),
                selectedCharacter == null ? null : new CharacterSelectionDto(selectedCharacter.getId()),
                selectedTag == null ? null : new TagSelectionDto(selectedTag.id()),
                selectedProfile == null ? null : new ProfileSelectionDto(selectedProfile.getCharacterId()),
                new SceneDraftDto(
                        selectedScene.getId(),
                        selectedScene.getTitle(),
                        selectedScene.getSynopsis(),
                        selectedScene.getContent()
                ),
                new ProjectSummaryDto(
                        currentProject.getTitle(),
                        currentProject.getAuthor(),
                        currentProject.getChapters().size(),
                        currentProject.getChapters().stream().mapToInt(chapter -> chapter.getScenes().size()).sum(),
                        currentProject.getChapters().stream()
                                .map(this::toChapterDto)
                                .toList(),
                        currentProject.getCharacters().stream()
                                .map(this::toCharacterDto)
                                .toList(),
                        currentProject.getNarrativeTags().stream()
                                .map(this::toTagDto)
                                .toList(),
                        currentProject.getCharacterTagProfiles().stream()
                                .map(this::toProfileDto)
                                .toList()
                )
        );
    }

    private ChapterDto toChapterDto(Chapter chapter) {
        return new ChapterDto(
                chapter.getId(),
                chapter.getTitle(),
                chapter.getScenes().stream()
                        .map(this::toSceneDto)
                        .toList()
        );
    }

    private SceneDto toSceneDto(Scene scene) {
        return new SceneDto(scene.getId(), scene.getTitle(), scene.getSynopsis());
    }

    private CharacterDto toCharacterDto(Character character) {
        int linkedSceneCount = (int) currentProject.getChapters().stream()
                .flatMap(chapter -> chapter.getScenes().stream())
                .filter(scene -> character.getId().equals(scene.getPointOfViewCharacterId()))
                .count();
        boolean selectedPointOfView = selectedScene() != null
                && character.getId().equals(selectedScene().getPointOfViewCharacterId());
        return new CharacterDto(
                character.getId(),
                character.getName(),
                character.getDescription(),
                linkedSceneCount,
                selectedPointOfView
        );
    }

    private TagDto toTagDto(NarrativeTag tag) {
        return new TagDto(
                tag.id(),
                tag.label(),
                tag.template(),
                countTagUsage(tag.id())
        );
    }

    private ProfileDto toProfileDto(CharacterTagProfile profile) {
        String characterName = currentProject.getCharacters().stream()
                .filter(character -> character.getId().equals(profile.getCharacterId()))
                .map(Character::getName)
                .findFirst()
                .orElse("Personagem");
        return new ProfileDto(
                profile.getCharacterId(),
                characterName,
                profile.getPrefix(),
                List.copyOf(profile.getPreferredTagIds())
        );
    }

    private SearchResultDto toSearchResultDto(SearchMatch match) {
        return new SearchResultDto(
                match.target().name(),
                match.chapterIndex(),
                match.sceneIndex(),
                match.title(),
                match.excerpt()
        );
    }

    private EmotionReportDto toEmotionReportDto(EmotionAnalysisReport report) {
        return new EmotionReportDto(
                report.generatedAt() == null ? "" : report.generatedAt().toString(),
                report.chunkCount(),
                report.overallSentiment() == null ? "" : report.overallSentiment().name(),
                report.dominantEmotion() == null ? "" : report.dominantEmotion().name(),
                report.averageEmotionScores().entrySet().stream()
                        .map(entry -> new ScoreDto(entry.getKey().name(), entry.getValue()))
                        .toList(),
                report.chunks().stream()
                        .map(this::toChunkDto)
                        .toList()
        );
    }

    private ChunkDto toChunkDto(EmotionChunkAnalysis chunk) {
        return new ChunkDto(
                chunk.chapterId(),
                chunk.chapterTitle(),
                chunk.sceneId(),
                chunk.sceneTitle(),
                chunk.excerpt(),
                chunk.wordCount(),
                chunk.chunkIndex(),
                chunk.sentiment() == null ? "" : chunk.sentiment().name(),
                chunk.dominantEmotion() == null ? "" : chunk.dominantEmotion().name(),
                chunk.emotionScores().entrySet().stream()
                        .map(entry -> new ScoreDto(entry.getKey().name(), entry.getValue()))
                        .toList()
        );
    }

    private int countTagUsage(String tagId) {
        String token = "{" + tagId + "}";
        return (int) currentProject.getChapters().stream()
                .flatMap(chapter -> chapter.getScenes().stream())
                .map(Scene::getContent)
                .filter(Objects::nonNull)
                .mapToLong(content -> content.split(java.util.regex.Pattern.quote(token), -1).length - 1L)
                .sum();
    }

    private void ensureSession() {
        if (currentProject == null || currentPath == null) {
            throw new IllegalStateException("Nenhum projeto carregado.");
        }
    }

    private void clearSession() {
        currentProject = null;
        currentPath = null;
        selectedChapterId = null;
        selectedSceneId = null;
        selectedCharacterId = null;
        selectedTagId = null;
        selectedProfileCharacterId = null;
    }

    private void ensureSelection() {
        ensureSession();
        if (selectedScene() != null) {
            return;
        }
        Chapter firstChapter = currentProject.getChapters().get(0);
        Scene firstScene = firstChapter.getScenes().get(0);
        selectedChapterId = firstChapter.getId();
        selectedSceneId = firstScene.getId();
    }

    private void ensureCharacterSelection() {
        ensureSession();
        if (selectedCharacter() != null) {
            return;
        }
        selectedCharacterId = currentProject.getCharacters().isEmpty()
                ? null
                : currentProject.getCharacters().get(0).getId();
    }

    private void ensureTagSelection() {
        ensureSession();
        if (selectedTag() != null) {
            return;
        }
        selectedTagId = currentProject.getNarrativeTags().isEmpty()
                ? null
                : currentProject.getNarrativeTags().get(0).id();
    }

    private void ensureProfileSelection() {
        ensureSession();
        CharacterTagProfileSynchronizer.synchronize(currentProject);
        if (selectedProfile() != null) {
            return;
        }
        selectedProfileCharacterId = currentProject.getCharacterTagProfiles().isEmpty()
                ? null
                : currentProject.getCharacterTagProfiles().get(0).getCharacterId();
    }

    private Chapter findChapter(String chapterId) {
        return currentProject.getChapters().stream()
                .filter(chapter -> chapter.getId().equals(chapterId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Capitulo nao encontrado."));
    }

    private Chapter selectedChapter() {
        if (currentProject == null || selectedChapterId == null) {
            return null;
        }
        return currentProject.getChapters().stream()
                .filter(chapter -> chapter.getId().equals(selectedChapterId))
                .findFirst()
                .orElse(null);
    }

    private Scene findScene(Chapter chapter, String sceneId) {
        return chapter.getScenes().stream()
                .filter(scene -> scene.getId().equals(sceneId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cena nao encontrada."));
    }

    private Scene selectedScene() {
        if (currentProject == null || selectedChapterId == null || selectedSceneId == null) {
            return null;
        }
        return currentProject.getChapters().stream()
                .filter(chapter -> chapter.getId().equals(selectedChapterId))
                .findFirst()
                .flatMap(chapter -> chapter.getScenes().stream()
                        .filter(scene -> scene.getId().equals(selectedSceneId))
                        .findFirst())
                .orElse(null);
    }

    private Character findCharacter(String characterId) {
        return currentProject.getCharacters().stream()
                .filter(character -> character.getId().equals(characterId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Personagem nao encontrado."));
    }

    private Character selectedCharacter() {
        if (currentProject == null || selectedCharacterId == null) {
            return null;
        }
        return currentProject.getCharacters().stream()
                .filter(character -> character.getId().equals(selectedCharacterId))
                .findFirst()
                .orElse(null);
    }

    private NarrativeTag findTag(String tagId) {
        return currentProject.getNarrativeTags().stream()
                .filter(tag -> tag.id().equals(tagId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tag nao encontrada."));
    }

    private NarrativeTag selectedTag() {
        if (currentProject == null || selectedTagId == null) {
            return null;
        }
        return currentProject.getNarrativeTags().stream()
                .filter(tag -> tag.id().equals(selectedTagId))
                .findFirst()
                .orElse(null);
    }

    private CharacterTagProfile findProfile(String characterId) {
        CharacterTagProfileSynchronizer.synchronize(currentProject);
        return currentProject.getCharacterTagProfiles().stream()
                .filter(profile -> profile.getCharacterId().equals(characterId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Perfil nao encontrado."));
    }

    private CharacterTagProfile selectedProfile() {
        if (currentProject == null || selectedProfileCharacterId == null) {
            return null;
        }
        CharacterTagProfileSynchronizer.synchronize(currentProject);
        return currentProject.getCharacterTagProfiles().stream()
                .filter(profile -> profile.getCharacterId().equals(selectedProfileCharacterId))
                .findFirst()
                .orElse(null);
    }

    private String normalizeNewTagId(String explicitId, String label) {
        String normalized = NarrativeTagIdPolicy.normalizeExplicitId(explicitId);
        if (normalized.isBlank()) {
            normalized = NarrativeTagIdPolicy.suggestFromText(label);
        }
        validateTagId(normalized, null);
        return normalized;
    }

    private String normalizeExistingTagId(String explicitId, String currentId) {
        String normalized = NarrativeTagIdPolicy.normalizeExplicitId(explicitId);
        if (normalized.isBlank()) {
            normalized = currentId;
        }
        validateTagId(normalized, currentId);
        return normalized;
    }

    private void validateTagId(String tagId, String currentId) {
        if (tagId == null || tagId.isBlank()) {
            throw new IllegalArgumentException("Preencha o id da tag no formato 4 letras + numero.");
        }
        if (!NarrativeTagIdPolicy.isValid(tagId)) {
            throw new IllegalArgumentException("Use id no formato 4 letras + numero, como falc1.");
        }
        boolean exists = currentProject.getNarrativeTags().stream()
                .map(NarrativeTag::id)
                .anyMatch(existingId -> existingId.equals(tagId) && !existingId.equals(currentId));
        if (exists) {
            throw new IllegalArgumentException("Ja existe uma tag com esse id.");
        }
    }

    record BridgeResult(String message, BridgeSessionDto session) {
    }

    record BridgeSessionDto(
            String path,
            ProjectValidationDto validation,
            String message,
            SelectionDto selection,
            CharacterSelectionDto characterSelection,
            TagSelectionDto tagSelection,
            ProfileSelectionDto profileSelection,
            SceneDraftDto scene,
            ProjectSummaryDto project
    ) {
    }

    record SelectionDto(String chapterId, String sceneId) {
    }

    record CharacterSelectionDto(String characterId) {
    }

    record TagSelectionDto(String tagId) {
    }

    record ProfileSelectionDto(String characterId) {
    }

    record SceneDraftDto(String id, String title, String synopsis, String content) {
    }

    record LocalProjectDto(String path, String label) {
    }

    record ProjectSummaryDto(
            String title,
            String author,
            int chapterCount,
            int sceneCount,
            List<ChapterDto> chapters,
            List<CharacterDto> characters,
            List<TagDto> tags,
            List<ProfileDto> profiles
    ) {
    }

    record ChapterDto(String id, String title, List<SceneDto> scenes) {
    }

    record SceneDto(String id, String title, String synopsis) {
    }

    record CharacterDto(String id, String name, String description, int linkedSceneCount, boolean selectedPointOfView) {
    }

    record TagDto(String id, String label, String template, int usageCount) {
    }

    record ProfileDto(String characterId, String characterName, String prefix, List<String> preferredTagIds) {
    }

    record SearchResultDto(String target, int chapterIndex, int sceneIndex, String title, String excerpt) {
    }

    record EmotionReportDto(
            String generatedAt,
            int chunkCount,
            String overallSentiment,
            String dominantEmotion,
            List<ScoreDto> averageScores,
            List<ChunkDto> chunks
    ) {
    }

    record ScoreDto(String label, Double score) {
    }

    record ChunkDto(
            String chapterId,
            String chapterTitle,
            String sceneId,
            String sceneTitle,
            String excerpt,
            int wordCount,
            int chunkIndex,
            String sentiment,
            String dominantEmotion,
            List<ScoreDto> scores
    ) {
    }
}
