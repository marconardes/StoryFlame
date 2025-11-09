import 'package:meta/meta.dart';

@immutable
class NarrativeTemplate {
  const NarrativeTemplate({
    required this.id,
    required this.name,
    required this.description,
    required this.steps,
    required this.completedSteps,
    required this.updatedAt,
  });

  final String id;
  final String name;
  final String description;
  final List<String> steps;
  final List<String> completedSteps;
  final DateTime updatedAt;

  factory NarrativeTemplate.fromJson(
    Map<String, dynamic> json,
  ) => NarrativeTemplate(
    id: json['id'] as String? ?? '',
    name: json['name'] as String? ?? 'Template',
    description: json['description'] as String? ?? '',
    steps: (json['steps'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    completedSteps: (json['completedSteps'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    updatedAt:
        DateTime.tryParse(json['updatedAt'] as String? ?? '') ?? DateTime.now(),
  );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'name': name,
    'description': description,
    'steps': steps,
    'completedSteps': completedSteps,
    'updatedAt': updatedAt.toIso8601String(),
  };

  NarrativeTemplate copyWith({
    String? id,
    String? name,
    String? description,
    List<String>? steps,
    List<String>? completedSteps,
    DateTime? updatedAt,
  }) {
    return NarrativeTemplate(
      id: id ?? this.id,
      name: name ?? this.name,
      description: description ?? this.description,
      steps: steps ?? this.steps,
      completedSteps: completedSteps ?? this.completedSteps,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
