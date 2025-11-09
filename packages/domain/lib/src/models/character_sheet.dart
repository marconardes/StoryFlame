import 'package:meta/meta.dart';

@immutable
class CharacterSheet {
  const CharacterSheet({
    required this.id,
    required this.name,
    required this.alias,
    required this.description,
    required this.history,
    required this.traits,
    required this.relationships,
    required this.notes,
    required this.chapterIds,
    required this.updatedAt,
  });

  final String id;
  final String name;
  final String alias;
  final String description;
  final String history;
  final List<String> traits;
  final List<String> relationships;
  final List<String> notes;
  final List<String> chapterIds;
  final DateTime updatedAt;

  factory CharacterSheet.fromJson(Map<String, dynamic> json) => CharacterSheet(
    id: json['id'] as String? ?? '',
    name: json['name'] as String? ?? 'Personagem sem nome',
    alias: json['alias'] as String? ?? '',
    description: json['description'] as String? ?? '',
    history: json['history'] as String? ?? '',
    traits: (json['traits'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    relationships: (json['relationships'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    notes: (json['notes'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    chapterIds: (json['chapterIds'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    updatedAt:
        DateTime.tryParse(json['updatedAt'] as String? ?? '') ?? DateTime.now(),
  );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'name': name,
    'alias': alias,
    'description': description,
    'history': history,
    'traits': traits,
    'relationships': relationships,
    'notes': notes,
    'chapterIds': chapterIds,
    'updatedAt': updatedAt.toIso8601String(),
  };

  CharacterSheet copyWith({
    String? id,
    String? name,
    String? alias,
    String? description,
    String? history,
    List<String>? traits,
    List<String>? relationships,
    List<String>? notes,
    List<String>? chapterIds,
    DateTime? updatedAt,
  }) {
    return CharacterSheet(
      id: id ?? this.id,
      name: name ?? this.name,
      alias: alias ?? this.alias,
      description: description ?? this.description,
      history: history ?? this.history,
      traits: traits ?? this.traits,
      relationships: relationships ?? this.relationships,
      notes: notes ?? this.notes,
      chapterIds: chapterIds ?? this.chapterIds,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
