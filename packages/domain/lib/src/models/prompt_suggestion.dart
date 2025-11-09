import 'package:meta/meta.dart';

@immutable
class PromptSuggestion {
  const PromptSuggestion({
    required this.id,
    required this.prompt,
    required this.context,
    required this.tags,
    required this.score,
    required this.createdAt,
    this.relatedChapterId,
  });

  final String id;
  final String prompt;
  final String context;
  final List<String> tags;
  final double score;
  final DateTime createdAt;
  final String? relatedChapterId;

  factory PromptSuggestion.fromJson(Map<String, dynamic> json) {
    return PromptSuggestion(
      id: json['id'] as String? ?? '',
      prompt: json['prompt'] as String? ?? '',
      context: json['context'] as String? ?? '',
      tags: (json['tags'] as List<dynamic>? ?? const <dynamic>[])
          .map((tag) => tag as String? ?? '')
          .where((tag) => tag.isNotEmpty)
          .toList(growable: false),
      score: (json['score'] as num?)?.toDouble() ?? 0,
      relatedChapterId: json['relatedChapterId'] as String?,
      createdAt:
          DateTime.tryParse(json['createdAt'] as String? ?? '') ??
          DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'prompt': prompt,
    'context': context,
    'tags': tags,
    'score': score,
    'relatedChapterId': relatedChapterId,
    'createdAt': createdAt.toIso8601String(),
  };

  PromptSuggestion copyWith({
    String? id,
    String? prompt,
    String? context,
    List<String>? tags,
    double? score,
    DateTime? createdAt,
    String? relatedChapterId,
  }) {
    return PromptSuggestion(
      id: id ?? this.id,
      prompt: prompt ?? this.prompt,
      context: context ?? this.context,
      tags: tags ?? this.tags,
      score: score ?? this.score,
      createdAt: createdAt ?? this.createdAt,
      relatedChapterId: relatedChapterId ?? this.relatedChapterId,
    );
  }
}
