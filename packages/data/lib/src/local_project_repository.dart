import 'dart:convert';
import 'dart:io';

import 'package:domain/domain.dart';
import 'package:path_provider/path_provider.dart';

class LocalProjectRepository implements ProjectRepository {
  LocalProjectRepository({Directory? overrideDirectory})
    : _overrideDirectory = overrideDirectory;

  final Directory? _overrideDirectory;
  List<Project> _cache = <Project>[];
  bool _loaded = false;

  Future<File> _databaseFile() async {
    final override = _overrideDirectory;
    if (override != null) {
      return File('${override.path}/storyflame_projects.json');
    }
    final directory = await getApplicationDocumentsDirectory();
    return File('${directory.path}/storyflame_projects.json');
  }

  Future<void> _ensureLoaded() async {
    if (_loaded) return;
    final file = await _databaseFile();
    if (!await file.exists()) {
      await file.create(recursive: true);
      await file.writeAsString(jsonEncode(<dynamic>[]));
    }
    final contents = await file.readAsString();
    if (contents.trim().isEmpty) {
      _cache = <Project>[];
    } else {
      final decoded = jsonDecode(contents) as List<dynamic>;
      _cache = decoded
          .map(
            (json) => Project.fromJson(
              json as Map<String, dynamic>? ?? <String, dynamic>{},
            ),
          )
          .toList(growable: false);
    }
    _loaded = true;
  }

  Future<void> _persist() async {
    final file = await _databaseFile();
    final payload = jsonEncode(_cache.map((e) => e.toJson()).toList());
    await file.writeAsString(payload);
  }

  @override
  Future<List<Project>> fetchProjects() async {
    await _ensureLoaded();
    return List<Project>.unmodifiable(_cache);
  }

  @override
  Future<Project> createProject(Project project) async {
    await _ensureLoaded();
    _cache = <Project>[..._cache, project];
    await _persist();
    return project;
  }

  @override
  Future<void> deleteProject(String projectId) async {
    await _ensureLoaded();
    _cache = _cache.where((p) => p.id != projectId).toList(growable: false);
    await _persist();
  }

  @override
  Future<Project> updateProject(Project project) async {
    await _ensureLoaded();
    final index = _cache.indexWhere((p) => p.id == project.id);
    if (index == -1) {
      throw StateError('Projeto não encontrado: ${project.id}');
    }
    _cache = <Project>[..._cache]..[index] = project;
    await _persist();
    return project;
  }

  @override
  Future<Project> addChapter(String projectId, Chapter chapter) async {
    await _ensureLoaded();
    final project = _getProject(projectId);
    final updatedProject = project.copyWith(
      chapters: <Chapter>[...project.chapters, chapter],
      updatedAt: DateTime.now(),
    );
    return updateProject(updatedProject);
  }

  @override
  Future<Project> updateChapter(String projectId, Chapter chapter) async {
    await _ensureLoaded();
    final project = _getProject(projectId);
    final chapters = project.chapters
        .map((c) {
          if (c.id == chapter.id) {
            return chapter;
          }
          return c;
        })
        .toList(growable: false);
    final updatedProject = project.copyWith(
      chapters: chapters,
      updatedAt: DateTime.now(),
    );
    return updateProject(updatedProject);
  }

  @override
  Future<Project> reorderChapters(
    String projectId,
    List<Chapter> chapters,
  ) async {
    await _ensureLoaded();
    final project = _getProject(projectId);
    final updatedProject = project.copyWith(
      chapters: List<Chapter>.from(chapters),
      updatedAt: DateTime.now(),
    );
    return updateProject(updatedProject);
  }

  Project _getProject(String id) {
    final project = _cache.firstWhere(
      (p) => p.id == id,
      orElse: () => throw StateError('Projeto não encontrado: $id'),
    );
    return project;
  }
}
