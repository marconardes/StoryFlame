import 'package:meta/meta.dart';

import 'insight.dart';

@immutable
class ExtensionManifest {
  const ExtensionManifest({
    required this.id,
    required this.name,
    required this.description,
    required this.version,
    required this.author,
    required this.type,
    required this.rules,
    this.repository,
    this.homepage,
  });

  final String id;
  final String name;
  final String description;
  final String version;
  final String author;
  final ExtensionType type;
  final List<ExtensionRule> rules;
  final String? repository;
  final String? homepage;

  factory ExtensionManifest.fromJson(Map<String, dynamic> json) {
    return ExtensionManifest(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? 'Extensão',
      description: json['description'] as String? ?? '',
      version: json['version'] as String? ?? '0.0.1',
      author: json['author'] as String? ?? 'Desconhecido',
      repository: json['repository'] as String?,
      homepage: json['homepage'] as String?,
      type: ExtensionTypeX.parse(json['type'] as String? ?? 'analyzer'),
      rules: (json['rules'] as List<dynamic>? ?? <dynamic>[])
          .map(
            (rule) => ExtensionRule.fromJson(
              rule as Map<String, dynamic>? ?? <String, dynamic>{},
            ),
          )
          .toList(growable: false),
    );
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'name': name,
    'description': description,
    'version': version,
    'author': author,
    'repository': repository,
    'homepage': homepage,
    'type': type.name,
    'rules': rules.map((rule) => rule.toJson()).toList(),
  };
}

enum ExtensionType { analyzer, connector }

extension ExtensionTypeX on ExtensionType {
  static ExtensionType parse(String value) {
    return ExtensionType.values.firstWhere(
      (candidate) => candidate.name == value,
      orElse: () => ExtensionType.analyzer,
    );
  }
}

@immutable
class ExtensionRule {
  const ExtensionRule({
    required this.id,
    required this.message,
    required this.kind,
    required this.scope,
    required this.pattern,
    required this.severity,
    this.threshold = 1,
  });

  final String id;
  final String message;
  final ExtensionRuleKind kind;
  final ExtensionScope scope;
  final String pattern;
  final int threshold;
  final InsightSeverity severity;

  factory ExtensionRule.fromJson(Map<String, dynamic> json) {
    return ExtensionRule(
      id: json['id'] as String? ?? '',
      message: json['message'] as String? ?? '',
      kind: ExtensionRuleKindX.parse(json['kind'] as String? ?? 'keyword'),
      scope: ExtensionScopeX.parse(json['scope'] as String? ?? 'content'),
      pattern: json['pattern'] as String? ?? '',
      threshold: json['threshold'] as int? ?? 1,
      severity: InsightSeverityX.parse(json['severity'] as String? ?? 'info'),
    );
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'message': message,
    'kind': kind.name,
    'scope': scope.name,
    'pattern': pattern,
    'threshold': threshold,
    'severity': severity.name,
  };
}

enum ExtensionRuleKind { keyword }

extension ExtensionRuleKindX on ExtensionRuleKind {
  static ExtensionRuleKind parse(String value) {
    return ExtensionRuleKind.values.firstWhere(
      (candidate) => candidate.name == value,
      orElse: () => ExtensionRuleKind.keyword,
    );
  }
}

enum ExtensionScope { content, summary, description }

extension ExtensionScopeX on ExtensionScope {
  static ExtensionScope parse(String value) {
    return ExtensionScope.values.firstWhere(
      (candidate) => candidate.name == value,
      orElse: () => ExtensionScope.content,
    );
  }
}

@immutable
class ExtensionFinding {
  const ExtensionFinding({
    required this.extensionId,
    required this.extensionName,
    required this.ruleId,
    required this.message,
    required this.severity,
    required this.occurrences,
    this.chapterId,
    this.chapterTitle,
  });

  final String extensionId;
  final String extensionName;
  final String ruleId;
  final String message;
  final InsightSeverity severity;
  final int occurrences;
  final String? chapterId;
  final String? chapterTitle;

  Map<String, dynamic> toJson() => <String, dynamic>{
    'extensionId': extensionId,
    'extensionName': extensionName,
    'ruleId': ruleId,
    'message': message,
    'severity': severity.name,
    'occurrences': occurrences,
    'chapterId': chapterId,
    'chapterTitle': chapterTitle,
  };
}
