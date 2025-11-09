import 'package:meta/meta.dart';

enum CreativeIdeaStatus { idea, draft, done }

@immutable
class CreativeIdea {
  const CreativeIdea({
    required this.id,
    required this.title,
    required this.description,
    required this.tags,
    required this.status,
    required this.updatedAt,
  });

  final String id;
  final String title;
  final String description;
  final List<String> tags;
  final CreativeIdeaStatus status;
  final DateTime updatedAt;

  factory CreativeIdea.fromJson(Map<String, dynamic> json) => CreativeIdea(
    id: json['id'] as String? ?? '',
    title: json['title'] as String? ?? 'Ideia',
    description: json['description'] as String? ?? '',
    tags: (json['tags'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    status: CreativeIdeaStatus.values.firstWhere(
      (value) => value.name == (json['status'] as String? ?? 'idea'),
      orElse: () => CreativeIdeaStatus.idea,
    ),
    updatedAt:
        DateTime.tryParse(json['updatedAt'] as String? ?? '') ?? DateTime.now(),
  );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'title': title,
    'description': description,
    'tags': tags,
    'status': status.name,
    'updatedAt': updatedAt.toIso8601String(),
  };

  CreativeIdea copyWith({
    String? id,
    String? title,
    String? description,
    List<String>? tags,
    CreativeIdeaStatus? status,
    DateTime? updatedAt,
  }) {
    return CreativeIdea(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      tags: tags ?? this.tags,
      status: status ?? this.status,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
