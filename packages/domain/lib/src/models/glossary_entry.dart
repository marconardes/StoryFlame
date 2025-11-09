import 'package:meta/meta.dart';

@immutable
class GlossaryEntry {
  const GlossaryEntry({
    required this.id,
    required this.term,
    required this.definition,
    required this.category,
    required this.notes,
    required this.chapterIds,
    required this.updatedAt,
  });

  final String id;
  final String term;
  final String definition;
  final String category;
  final String notes;
  final List<String> chapterIds;
  final DateTime updatedAt;

  factory GlossaryEntry.fromJson(Map<String, dynamic> json) => GlossaryEntry(
    id: json['id'] as String? ?? '',
    term: json['term'] as String? ?? 'Termo',
    definition: json['definition'] as String? ?? '',
    category: json['category'] as String? ?? '',
    notes: json['notes'] as String? ?? '',
    chapterIds: (json['chapterIds'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    updatedAt:
        DateTime.tryParse(json['updatedAt'] as String? ?? '') ?? DateTime.now(),
  );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'term': term,
    'definition': definition,
    'category': category,
    'notes': notes,
    'chapterIds': chapterIds,
    'updatedAt': updatedAt.toIso8601String(),
  };

  GlossaryEntry copyWith({
    String? id,
    String? term,
    String? definition,
    String? category,
    String? notes,
    List<String>? chapterIds,
    DateTime? updatedAt,
  }) {
    return GlossaryEntry(
      id: id ?? this.id,
      term: term ?? this.term,
      definition: definition ?? this.definition,
      category: category ?? this.category,
      notes: notes ?? this.notes,
      chapterIds: chapterIds ?? this.chapterIds,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
