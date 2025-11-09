import 'dart:async';
import 'dart:convert';

import 'package:crypto/crypto.dart';
import 'package:domain/domain.dart';
import 'package:flutter/foundation.dart';
import 'package:uuid/uuid.dart';

import 'services/extension_service.dart';
import 'services/insights_service.dart';

class ProjectStore extends ChangeNotifier {
  ProjectStore(
    this.repository, {
    InsightsService? insightsService,
    ExtensionService? extensionService,
  }) : _insightsService =
           insightsService ??
           InsightsService(
             extensionService: extensionService ?? ExtensionService(),
           );

  final ProjectRepository repository;
  final _uuid = const Uuid();
  final InsightsService _insightsService;

  List<Project> _projects = <Project>[];
  bool _loading = true;
  String _query = '';
  final Map<String, int> _attempts = <String, int>{};
  final Map<String, DateTime> _lockedUntil = <String, DateTime>{};
  final Map<String, InsightsSnapshot> _insightsCache =
      <String, InsightsSnapshot>{};

  bool get isLoading => _loading;

  List<Project> get projects {
    if (_query.isEmpty) return _projects;
    return _projects
        .where(
          (p) =>
              p.title.toLowerCase().contains(_query.toLowerCase()) ||
              p.description.toLowerCase().contains(_query.toLowerCase()),
        )
        .toList(growable: false);
  }

  Future<void> load() async {
    _loading = true;
    notifyListeners();
    _projects = await repository.fetchProjects();
    final ids = _projects.map((project) => project.id).toSet();
    _insightsCache.removeWhere((id, _) => !ids.contains(id));
    _loading = false;
    notifyListeners();
  }

  void search(String query) {
    _query = query;
    notifyListeners();
  }

  InsightsSnapshot? insightsFor(String projectId) => _insightsCache[projectId];

  Future<InsightsSnapshot> refreshInsights(String projectId) async {
    final project = _getProject(projectId);
    final snapshot = await _insightsService.analyze(project);
    _insightsCache[projectId] = snapshot;
    notifyListeners();
    return snapshot;
  }

  Future<Project> createProject({
    required String title,
    required String description,
  }) async {
    final project = Project(
      id: _uuid.v4(),
      title: title,
      description: description,
      updatedAt: DateTime.now(),
      chapters: const <Chapter>[],
      characters: const <CharacterSheet>[],
      glossary: const <GlossaryEntry>[],
      timeline: const <TimelineEvent>[],
      worldElements: const <WorldElement>[],
    );
    final created = await repository.createProject(project);
    _projects = <Project>[..._projects, created];
    notifyListeners();
    return created;
  }

  Future<Project> importProject(Project project) async {
    final exists = _projects.any((p) => p.id == project.id);
    final projectToSave = exists ? project.copyWith(id: _uuid.v4()) : project;
    final created = await repository.createProject(projectToSave);
    _projects = <Project>[..._projects, created];
    notifyListeners();
    return created;
  }

  Future<void> deleteProject(String projectId) async {
    await repository.deleteProject(projectId);
    _projects = _projects.where((p) => p.id != projectId).toList();
    notifyListeners();
  }

  Future<Project> addChapter(String projectId, String title) async {
    final chapter = Chapter(
      id: _uuid.v4(),
      title: title,
      summary: '',
      content: '',
      wordCount: 0,
      updatedAt: DateTime.now(),
      status: ChapterStatus.draft,
      coverImage: '',
    );
    final updated = await repository.addChapter(projectId, chapter);
    _replaceProject(updated);
    return updated;
  }

  Future<Project> updateChapter(String projectId, Chapter chapter) async {
    final updated = await repository.updateChapter(projectId, chapter);
    _replaceProject(updated);
    return updated;
  }

  Future<Project> reorderChapters(
    String projectId,
    List<Chapter> chapters,
  ) async {
    final updated = await repository.reorderChapters(projectId, chapters);
    _replaceProject(updated);
    return updated;
  }

  Future<Project> updateProject(Project project) async {
    final updated = await repository.updateProject(project);
    _replaceProject(updated);
    return updated;
  }

  Future<Project> deleteChapter(String projectId, String chapterId) async {
    final project = _getProject(projectId);
    final now = DateTime.now();
    final chapters =
        project.chapters.where((chapter) => chapter.id != chapterId).toList();
    final characters =
        project.characters
            .map(
              (character) =>
                  character.chapterIds.contains(chapterId)
                      ? character.copyWith(
                        chapterIds:
                            character.chapterIds
                                .where((id) => id != chapterId)
                                .toList(),
                        updatedAt: now,
                      )
                      : character,
            )
            .toList();
    final glossary =
        project.glossary
            .map(
              (entry) =>
                  entry.chapterIds.contains(chapterId)
                      ? entry.copyWith(
                        chapterIds:
                            entry.chapterIds
                                .where((id) => id != chapterId)
                                .toList(),
                        updatedAt: now,
                      )
                      : entry,
            )
            .toList();
    final worldElements =
        project.worldElements
            .map(
              (element) =>
                  element.chapterIds.contains(chapterId)
                      ? element.copyWith(
                        chapterIds:
                            element.chapterIds
                                .where((id) => id != chapterId)
                                .toList(),
                        updatedAt: now,
                      )
                      : element,
            )
            .toList();
    final timeline =
        project.timeline
            .map(
              (event) =>
                  event.chapterId == chapterId
                      ? event.copyWith(chapterId: null)
                      : event,
            )
            .toList();
    final updatedProject = project.copyWith(
      chapters: chapters,
      characters: characters,
      glossary: glossary,
      worldElements: worldElements,
      timeline: timeline,
      updatedAt: now,
    );
    return updateProject(updatedProject);
  }

  Future<Project> addReviewComment({
    required String projectId,
    required String message,
    String? chapterId,
    String? context,
    String author = 'Revisor local',
  }) async {
    final project = _getProject(projectId);
    final now = DateTime.now();
    final comment = ReviewComment(
      id: _uuid.v4(),
      message: message,
      context: context,
      targetChapterId: chapterId,
      author: author,
      createdAt: now,
      updatedAt: now,
      resolved: false,
    );
    final updated = project.copyWith(
      reviewComments: <ReviewComment>[...project.reviewComments, comment],
      updatedAt: now,
    );
    return updateProject(updated);
  }

  Future<Project> toggleReviewComment({
    required String projectId,
    required String commentId,
    required bool resolved,
  }) async {
    final project = _getProject(projectId);
    final now = DateTime.now();
    final comments = project.reviewComments
        .map(
          (comment) =>
              comment.id == commentId
                  ? comment.copyWith(resolved: resolved, updatedAt: now)
                  : comment,
        )
        .toList(growable: false);
    final updated = project.copyWith(reviewComments: comments, updatedAt: now);
    return updateProject(updated);
  }

  Future<Project> deleteReviewComment(
    String projectId,
    String commentId,
  ) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      reviewComments: project.reviewComments
          .where((comment) => comment.id != commentId)
          .toList(growable: false),
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  void _replaceProject(Project project) {
    _projects = _projects.map((p) => p.id == project.id ? project : p).toList();
    _insightsCache.remove(project.id);
    notifyListeners();
  }

  bool requiresPassword(Project project) =>
      (project.passwordHash != null && project.passwordHash!.isNotEmpty);

  bool isLocked(Project project) {
    final unlockAt = _lockedUntil[project.id];
    if (unlockAt == null) return false;
    if (DateTime.now().isAfter(unlockAt)) {
      _lockedUntil.remove(project.id);
      _attempts[project.id] = 0;
      return false;
    }
    return true;
  }

  bool verifyPassword(Project project, String password) {
    if (!requiresPassword(project)) return true;
    if (isLocked(project)) return false;
    final hash = sha256.convert(utf8.encode(password)).toString();
    if (hash == project.passwordHash) {
      _attempts[project.id] = 0;
      return true;
    }
    final attempt = (_attempts[project.id] ?? 0) + 1;
    _attempts[project.id] = attempt;
    if (attempt >= 5) {
      _lockedUntil[project.id] = DateTime.now().add(const Duration(minutes: 1));
    }
    return false;
  }

  Future<void> setPassword(Project project, String? password) async {
    final hash =
        password == null || password.isEmpty
            ? null
            : sha256.convert(utf8.encode(password)).toString();
    final updated = project.copyWith(
      passwordHash: hash,
      updatedAt: DateTime.now(),
    );
    await updateProject(updated);
  }

  Future<void> updateGoals({
    required Project project,
    required int dailyGoal,
    required int totalGoal,
  }) async {
    final updated = project.copyWith(
      dailyGoal: dailyGoal,
      totalGoal: totalGoal,
      updatedAt: DateTime.now(),
    );
    await updateProject(updated);
  }

  int projectWordCount(Project project) =>
      project.chapters.fold<int>(0, (sum, c) => sum + c.wordCount);

  int wordsWrittenToday(Project project) {
    final now = DateTime.now();
    return project.chapters
        .where((chapter) {
          final updated = chapter.updatedAt;
          return updated.year == now.year &&
              updated.month == now.month &&
              updated.day == now.day;
        })
        .fold<int>(0, (sum, chapter) => sum + chapter.wordCount);
  }

  Project _getProject(String projectId) {
    return _projects.firstWhere((p) => p.id == projectId);
  }

  Future<Project> toggleCharacterChapter(
    String projectId,
    String characterId,
    String chapterId,
  ) async {
    final project = _getProject(projectId);
    final characters = project.characters
        .map((character) {
          if (character.id != characterId) return character;
          final chapters =
              character.chapterIds.contains(chapterId)
                  ? character.chapterIds.where((id) => id != chapterId).toList()
                  : <String>[...character.chapterIds, chapterId];
          return character.copyWith(
            chapterIds: chapters,
            updatedAt: DateTime.now(),
          );
        })
        .toList(growable: false);
    final updated = project.copyWith(
      characters: characters,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> toggleWorldElementChapter(
    String projectId,
    String elementId,
    String chapterId,
  ) async {
    final project = _getProject(projectId);
    final elements = project.worldElements
        .map((element) {
          if (element.id != elementId) return element;
          final chapters =
              element.chapterIds.contains(chapterId)
                  ? element.chapterIds.where((id) => id != chapterId).toList()
                  : <String>[...element.chapterIds, chapterId];
          return element.copyWith(
            chapterIds: chapters,
            updatedAt: DateTime.now(),
          );
        })
        .toList(growable: false);
    final updated = project.copyWith(
      worldElements: elements,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> saveCharacter(String projectId, CharacterSheet sheet) async {
    final project = _getProject(projectId);
    final exists = project.characters.any((c) => c.id == sheet.id);
    final characters =
        exists
            ? project.characters
                .map((c) => c.id == sheet.id ? sheet : c)
                .toList(growable: false)
            : <CharacterSheet>[...project.characters, sheet];
    final updated = project.copyWith(
      characters: characters,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> deleteCharacter(String projectId, String characterId) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      characters: project.characters
          .where((c) => c.id != characterId)
          .toList(growable: false),
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> saveGlossaryEntry(
    String projectId,
    GlossaryEntry entry,
  ) async {
    final project = _getProject(projectId);
    final exists = project.glossary.any((g) => g.id == entry.id);
    final glossary =
        exists
            ? project.glossary
                .map((g) => g.id == entry.id ? entry : g)
                .toList(growable: false)
            : <GlossaryEntry>[...project.glossary, entry];
    final updated = project.copyWith(
      glossary: glossary,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> deleteGlossaryEntry(String projectId, String entryId) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      glossary: project.glossary
          .where((entry) => entry.id != entryId)
          .toList(growable: false),
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> toggleGlossaryChapter(
    String projectId,
    String entryId,
    String chapterId,
  ) async {
    final project = _getProject(projectId);
    final glossary = project.glossary
        .map((entry) {
          if (entry.id != entryId) return entry;
          final chapters =
              entry.chapterIds.contains(chapterId)
                  ? entry.chapterIds.where((id) => id != chapterId).toList()
                  : <String>[...entry.chapterIds, chapterId];
          return entry.copyWith(
            chapterIds: chapters,
            updatedAt: DateTime.now(),
          );
        })
        .toList(growable: false);
    final updated = project.copyWith(
      glossary: glossary,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> saveTimelineEvent(
    String projectId,
    TimelineEvent event,
  ) async {
    final project = _getProject(projectId);
    final exists = project.timeline.any((t) => t.id == event.id);
    final timeline =
        exists
            ? project.timeline
                .map((t) => t.id == event.id ? event : t)
                .toList(growable: false)
            : <TimelineEvent>[...project.timeline, event];
    final ordered = <TimelineEvent>[...timeline]
      ..sort((a, b) => a.order.compareTo(b.order));
    final updated = project.copyWith(
      timeline: ordered,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> deleteTimelineEvent(String projectId, String eventId) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      timeline: project.timeline
          .where((event) => event.id != eventId)
          .toList(growable: false),
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> reorderTimeline(
    String projectId,
    List<TimelineEvent> events,
  ) async {
    final ordered = <TimelineEvent>[];
    for (var i = 0; i < events.length; i++) {
      ordered.add(events[i].copyWith(order: i));
    }
    final project = _getProject(projectId);
    final updated = project.copyWith(
      timeline: ordered,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> saveWorldElement(
    String projectId,
    WorldElement element,
  ) async {
    final project = _getProject(projectId);
    final exists = project.worldElements.any((el) => el.id == element.id);
    final elements =
        exists
            ? project.worldElements
                .map((el) => el.id == element.id ? element : el)
                .toList(growable: false)
            : <WorldElement>[...project.worldElements, element];
    final updated = project.copyWith(
      worldElements: elements,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> deleteWorldElement(String projectId, String elementId) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      worldElements: project.worldElements
          .where((element) => element.id != elementId)
          .toList(growable: false),
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> updatePublicationChecklist(
    String projectId,
    PublicationChecklist checklist,
  ) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      publicationChecklist: checklist,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> addPublicationRecord(
    String projectId,
    PublicationRecord record,
  ) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      publicationHistory: <PublicationRecord>[
        ...project.publicationHistory,
        record,
      ],
      publicationChecklist: project.publicationChecklist.copyWith(
        ebookExported:
            record.channel == PublicationChannel.epub ||
            record.channel == PublicationChannel.mobi,
        kdpPackageReady: record.channel == PublicationChannel.kdp,
      ),
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> saveCreativeIdea(String projectId, CreativeIdea idea) async {
    final project = _getProject(projectId);
    final exists = project.creativeIdeas.any(
      (candidate) => candidate.id == idea.id,
    );
    final ideas =
        exists
            ? project.creativeIdeas
                .map((candidate) => candidate.id == idea.id ? idea : candidate)
                .toList(growable: false)
            : <CreativeIdea>[...project.creativeIdeas, idea];
    final updated = project.copyWith(
      creativeIdeas: ideas,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> deleteCreativeIdea(String projectId, String ideaId) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      creativeIdeas: project.creativeIdeas
          .where((idea) => idea.id != ideaId)
          .toList(growable: false),
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> saveTemplate(
    String projectId,
    NarrativeTemplate template,
  ) async {
    final project = _getProject(projectId);
    final exists = project.templates.any(
      (candidate) => candidate.id == template.id,
    );
    final templates =
        exists
            ? project.templates
                .map(
                  (candidate) =>
                      candidate.id == template.id ? template : candidate,
                )
                .toList(growable: false)
            : <NarrativeTemplate>[...project.templates, template];
    final updated = project.copyWith(
      templates: templates,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> deleteTemplate(String projectId, String templateId) async {
    final project = _getProject(projectId);
    final updated = project.copyWith(
      templates: project.templates
          .where((template) => template.id != templateId)
          .toList(growable: false),
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }

  Future<Project> toggleTemplateStep(
    String projectId,
    String templateId,
    String step,
  ) async {
    final project = _getProject(projectId);
    final templates = project.templates
        .map((template) {
          if (template.id != templateId) return template;
          final completed =
              template.completedSteps.contains(step)
                  ? template.completedSteps
                      .where((value) => value != step)
                      .toList()
                  : <String>[...template.completedSteps, step];
          return template.copyWith(
            completedSteps: completed,
            updatedAt: DateTime.now(),
          );
        })
        .toList(growable: false);
    final updated = project.copyWith(
      templates: templates,
      updatedAt: DateTime.now(),
    );
    return updateProject(updated);
  }
}
