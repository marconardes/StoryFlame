import 'dart:convert';
import 'dart:io';

import 'package:domain/domain.dart';
import 'package:flutter/services.dart' show AssetBundle, rootBundle;
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

class ExtensionService {
  ExtensionService({
    List<String>? assetPaths,
    AssetBundle? bundle,
    Directory? overrideDirectory,
  }) : _assetPaths =
           assetPaths ??
           const <String>['assets/extensions/storyflame_keywords.json'],
       _bundle = bundle ?? rootBundle,
       _overrideDirectory = overrideDirectory;

  final List<String> _assetPaths;
  final AssetBundle _bundle;
  final Directory? _overrideDirectory;
  List<ExtensionManifest>? _cache;

  Future<List<ExtensionManifest>> loadExtensions() async {
    if (_cache != null) return _cache!;
    final manifests = <ExtensionManifest>[];
    for (final asset in _assetPaths) {
      try {
        final contents = await _bundle.loadString(asset);
        manifests.add(
          ExtensionManifest.fromJson(
            jsonDecode(contents) as Map<String, dynamic>,
          ),
        );
      } catch (_) {
        // Ignora assets inválidos para não travar o app.
      }
    }
    final directory = await _ensureDirectory();
    final files =
        directory.existsSync()
            ? directory.listSync().whereType<File>().where(
              (file) => file.path.endsWith('.json'),
            )
            : const <File>[];
    for (final file in files) {
      try {
        final contents = await file.readAsString();
        manifests.add(
          ExtensionManifest.fromJson(
            jsonDecode(contents) as Map<String, dynamic>,
          ),
        );
      } catch (_) {
        // Arquivo de extensão inválido, segue para o próximo.
      }
    }
    // Remove duplicados mantendo o último registro.
    final unique = <String, ExtensionManifest>{};
    for (final manifest in manifests) {
      if (manifest.id.isEmpty) continue;
      unique[manifest.id] = manifest;
    }
    _cache = unique.values.toList(growable: false);
    return _cache!;
  }

  Future<List<ExtensionFinding>> runAnalyzers(Project project) async {
    final manifests = await loadExtensions();
    final findings = <ExtensionFinding>[];
    for (final manifest in manifests.where(
      (manifest) => manifest.type == ExtensionType.analyzer,
    )) {
      for (final rule in manifest.rules) {
        switch (rule.kind) {
          case ExtensionRuleKind.keyword:
            findings.addAll(_runKeywordRule(manifest, rule, project));
            break;
        }
      }
    }
    return findings;
  }

  List<ExtensionFinding> _runKeywordRule(
    ExtensionManifest manifest,
    ExtensionRule rule,
    Project project,
  ) {
    final regex = RegExp(rule.pattern, caseSensitive: false, multiLine: true);
    final matches = <ExtensionFinding>[];
    switch (rule.scope) {
      case ExtensionScope.content:
        for (final chapter in project.chapters) {
          final occurrences = regex.allMatches(chapter.content).length;
          if (occurrences >= rule.threshold) {
            matches.add(
              ExtensionFinding(
                extensionId: manifest.id,
                extensionName: manifest.name,
                ruleId: rule.id,
                message: rule.message,
                severity: rule.severity,
                occurrences: occurrences,
                chapterId: chapter.id,
                chapterTitle: chapter.title,
              ),
            );
          }
        }
        break;
      case ExtensionScope.summary:
        for (final chapter in project.chapters) {
          final occurrences = regex.allMatches(chapter.summary).length;
          if (occurrences >= rule.threshold) {
            matches.add(
              ExtensionFinding(
                extensionId: manifest.id,
                extensionName: manifest.name,
                ruleId: rule.id,
                message: rule.message,
                severity: rule.severity,
                occurrences: occurrences,
                chapterId: chapter.id,
                chapterTitle: chapter.title,
              ),
            );
          }
        }
        break;
      case ExtensionScope.description:
        final occurrences = regex.allMatches(project.description).length;
        if (occurrences >= rule.threshold) {
          matches.add(
            ExtensionFinding(
              extensionId: manifest.id,
              extensionName: manifest.name,
              ruleId: rule.id,
              message: rule.message,
              severity: rule.severity,
              occurrences: occurrences,
              chapterId: null,
              chapterTitle: null,
            ),
          );
        }
        break;
    }
    return matches;
  }

  Future<Directory> _ensureDirectory() async {
    if (_overrideDirectory != null) {
      return _overrideDirectory!;
    }
    final base = await getApplicationDocumentsDirectory();
    final directory = Directory(p.join(base.path, 'storyflame_extensions'));
    if (!await directory.exists()) {
      await directory.create(recursive: true);
    }
    return directory;
  }
}
