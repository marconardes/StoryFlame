import '../models/chapter.dart';
import '../models/project.dart';

abstract class ProjectRepository {
  Future<List<Project>> fetchProjects();

  Future<Project> createProject(Project project);
  Future<Project> updateProject(Project project);
  Future<void> deleteProject(String projectId);

  Future<Project> addChapter(String projectId, Chapter chapter);
  Future<Project> updateChapter(String projectId, Chapter chapter);
  Future<Project> reorderChapters(String projectId, List<Chapter> chapters);
}
