import 'dart:convert';
import 'dart:io';

import 'package:domain/domain.dart';
import 'package:path_provider/path_provider.dart';

class CollaborationService {
  Future<File> exportProject(Project project) async {
    final directory = await getApplicationDocumentsDirectory();
    final sanitized = project.title.replaceAll(RegExp(r'[^a-zA-Z0-9_-]'), '_');
    final file = File('${directory.path}/$sanitized.storyflame');
    await file.writeAsString(jsonEncode(project.toJson()));
    return file;
  }

  Future<Project> importProject(String path) async {
    final file = File(path);
    if (!await file.exists()) {
      throw ArgumentError('Arquivo não encontrado: $path');
    }
    final content = await file.readAsString();
    final json = jsonDecode(content) as Map<String, dynamic>;
    return Project.fromJson(json);
  }
}
