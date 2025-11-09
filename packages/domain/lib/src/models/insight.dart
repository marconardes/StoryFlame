import 'package:meta/meta.dart';

@immutable
class Insight {
  const Insight({
    required this.id,
    required this.category,
    required this.severity,
    required this.title,
    required this.description,
    required this.generatedAt,
    this.contextId,
  });

  final String id;
  final InsightCategory category;
  final InsightSeverity severity;
  final String title;
  final String description;
  final DateTime generatedAt;
  final String? contextId;

  factory Insight.fromJson(Map<String, dynamic> json) => Insight(
    id: json['id'] as String? ?? '',
    category: InsightCategoryX.parse(json['category'] as String?),
    severity: InsightSeverityX.parse(json['severity'] as String?),
    title: json['title'] as String? ?? '',
    description: json['description'] as String? ?? '',
    contextId: json['contextId'] as String?,
    generatedAt:
        DateTime.tryParse(json['generatedAt'] as String? ?? '') ??
        DateTime.now(),
  );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'category': category.name,
    'severity': severity.name,
    'title': title,
    'description': description,
    'contextId': contextId,
    'generatedAt': generatedAt.toIso8601String(),
  };

  Insight copyWith({
    String? id,
    InsightCategory? category,
    InsightSeverity? severity,
    String? title,
    String? description,
    String? contextId,
    DateTime? generatedAt,
  }) {
    return Insight(
      id: id ?? this.id,
      category: category ?? this.category,
      severity: severity ?? this.severity,
      title: title ?? this.title,
      description: description ?? this.description,
      contextId: contextId ?? this.contextId,
      generatedAt: generatedAt ?? this.generatedAt,
    );
  }
}

enum InsightCategory {
  goals,
  pacing,
  character,
  terminology,
  emotion,
  structure,
  prompts,
  health,
}

extension InsightCategoryX on InsightCategory {
  static InsightCategory parse(String? value) {
    return InsightCategory.values.firstWhere(
      (candidate) => candidate.name == value,
      orElse: () => InsightCategory.goals,
    );
  }

  String get label {
    switch (this) {
      case InsightCategory.goals:
        return 'Metas';
      case InsightCategory.pacing:
        return 'Ritmo';
      case InsightCategory.character:
        return 'Personagens';
      case InsightCategory.terminology:
        return 'Terminologia';
      case InsightCategory.emotion:
        return 'Emoções';
      case InsightCategory.structure:
        return 'Estrutura';
      case InsightCategory.prompts:
        return 'Prompts';
      case InsightCategory.health:
        return 'Saúde do projeto';
    }
  }
}

enum InsightSeverity { info, warning, critical }

extension InsightSeverityX on InsightSeverity {
  static InsightSeverity parse(String? value) {
    return InsightSeverity.values.firstWhere(
      (candidate) => candidate.name == value,
      orElse: () => InsightSeverity.info,
    );
  }

  String get label {
    switch (this) {
      case InsightSeverity.info:
        return 'Sugestão';
      case InsightSeverity.warning:
        return 'Atenção';
      case InsightSeverity.critical:
        return 'Crítico';
    }
  }
}
