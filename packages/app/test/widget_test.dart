import 'package:domain/domain.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:storyflame_app/src/project_store.dart';
import 'package:storyflame_app/src/theme_controller.dart';
import 'package:storyflame_app/src/ui/project_details_page.dart';
import 'package:storyflame_app/src/ui/project_list_page.dart';

class _FakeProjectRepository implements ProjectRepository {
  List<Project> _projects = [
    Project(
      id: 'p1',
      title: 'Demo Project',
      description: 'Descrição',
      updatedAt: DateTime(2024, 5, 1),
      chapters: [
        Chapter(
          id: 'c1',
          title: 'Primeiro Capítulo',
          summary: 'Resumo',
          content: 'Conteúdo de teste',
          wordCount: Chapter.countWords('Conteúdo de teste'),
          updatedAt: DateTime(2024, 5, 1),
        ),
      ],
    ),
  ];

  @override
  Future<Project> addChapter(String projectId, Chapter chapter) async {
    final project = _projects.firstWhere((p) => p.id == projectId);
    final updated = project.copyWith(chapters: [...project.chapters, chapter]);
    return updateProject(updated);
  }

  @override
  Future<Project> createProject(Project project) async {
    _projects = [..._projects, project];
    return project;
  }

  @override
  Future<void> deleteProject(String projectId) async {
    _projects = _projects.where((p) => p.id != projectId).toList();
  }

  @override
  Future<List<Project>> fetchProjects() async => _projects;

  @override
  Future<Project> reorderChapters(
    String projectId,
    List<Chapter> chapters,
  ) async {
    final project = _projects.firstWhere((p) => p.id == projectId);
    final updated = project.copyWith(chapters: chapters);
    return updateProject(updated);
  }

  @override
  Future<Project> updateChapter(String projectId, Chapter chapter) async {
    final project = _projects.firstWhere((p) => p.id == projectId);
    final chapters =
        project.chapters.map((c) => c.id == chapter.id ? chapter : c).toList();
    final updated = project.copyWith(chapters: chapters);
    return updateProject(updated);
  }

  @override
  Future<Project> updateProject(Project project) async {
    _projects = _projects.map((p) => p.id == project.id ? project : p).toList();
    return project;
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('lista projetos e navega para capítulos', (tester) async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    final theme = ThemeController(prefs, false);
    final store = ProjectStore(_FakeProjectRepository());
    await store.load();

    await tester.pumpWidget(
      MultiProvider(
        providers: [
          ChangeNotifierProvider<ProjectStore>.value(value: store),
          ChangeNotifierProvider<ThemeController>.value(value: theme),
        ],
        child: MaterialApp(
          home: const ProjectListPage(),
          onGenerateRoute: (settings) {
            if (settings.name == ProjectDetailsPage.routeName) {
              final projectId = settings.arguments as String;
              return MaterialPageRoute(
                builder: (_) => ProjectDetailsPage(projectId: projectId),
              );
            }
            return null;
          },
        ),
      ),
    );

    await tester.pumpAndSettle();

    expect(find.text('Demo Project'), findsOneWidget);

    await tester.tap(find.text('Demo Project'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text('Primeiro Capítulo'), findsOneWidget);
  });
}
