import 'package:meta/meta.dart';

enum WorldElementType { location, item }

@immutable
class WorldElement {
  const WorldElement({
    required this.id,
    required this.name,
    required this.description,
    required this.lore,
    required this.chapterIds,
    required this.type,
    required this.updatedAt,
  });

  final String id;
  final String name;
  final String description;
  final String lore;
  final List<String> chapterIds;
  final WorldElementType type;
  final DateTime updatedAt;

  factory WorldElement.fromJson(Map<String, dynamic> json) => WorldElement(
    id: json['id'] as String? ?? '',
    name: json['name'] as String? ?? 'Elemento',
    description: json['description'] as String? ?? '',
    lore: json['lore'] as String? ?? '',
    chapterIds: (json['chapterIds'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
    type: _typeFromString(json['type'] as String? ?? 'location'),
    updatedAt:
        DateTime.tryParse(json['updatedAt'] as String? ?? '') ?? DateTime.now(),
  );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'name': name,
    'description': description,
    'lore': lore,
    'chapterIds': chapterIds,
    'type': type.name,
    'updatedAt': updatedAt.toIso8601String(),
  };

  WorldElement copyWith({
    String? id,
    String? name,
    String? description,
    String? lore,
    List<String>? chapterIds,
    WorldElementType? type,
    DateTime? updatedAt,
  }) {
    return WorldElement(
      id: id ?? this.id,
      name: name ?? this.name,
      description: description ?? this.description,
      lore: lore ?? this.lore,
      chapterIds: chapterIds ?? this.chapterIds,
      type: type ?? this.type,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  static WorldElementType _typeFromString(String value) {
    return WorldElementType.values.firstWhere(
      (element) => element.name == value,
      orElse: () => WorldElementType.location,
    );
  }
}
