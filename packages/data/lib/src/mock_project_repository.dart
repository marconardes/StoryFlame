import 'dart:convert';

import 'package:domain/domain.dart';
import 'package:flutter/services.dart';

class MockProjectRepository implements ProjectRepository {
  MockProjectRepository({AssetBundle? bundle}) : _bundle = bundle ?? rootBundle;

  final AssetBundle _bundle;
  static const _assetPath = 'packages/data/assets/mock/projects.json';

  @override
  Future<List<Project>> fetchProjects() async {
    final raw = await _bundle.loadString(_assetPath);
    final decoded = jsonDecode(raw) as List<dynamic>;
    return decoded
        .map(
          (project) => Project.fromJson(
            project as Map<String, dynamic>? ?? <String, dynamic>{},
          ),
        )
        .toList(growable: false);
  }

  @override
  Future<Project> createProject(Project project) {
    throw UnsupportedError('MockProjectRepository é somente leitura.');
  }

  @override
  Future<void> deleteProject(String projectId) {
    throw UnsupportedError('MockProjectRepository é somente leitura.');
  }

  @override
  Future<Project> updateProject(Project project) {
    throw UnsupportedError('MockProjectRepository é somente leitura.');
  }

  @override
  Future<Project> addChapter(String projectId, Chapter chapter) {
    throw UnsupportedError('MockProjectRepository é somente leitura.');
  }

  @override
  Future<Project> reorderChapters(String projectId, List<Chapter> chapters) {
    throw UnsupportedError('MockProjectRepository é somente leitura.');
  }

  @override
  Future<Project> updateChapter(String projectId, Chapter chapter) {
    throw UnsupportedError('MockProjectRepository é somente leitura.');
  }
}
