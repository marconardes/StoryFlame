import 'package:meta/meta.dart';

@immutable
class TimelineEvent {
  const TimelineEvent({
    required this.id,
    required this.title,
    required this.description,
    required this.date,
    required this.chapterId,
    required this.order,
    required this.tags,
  });

  final String id;
  final String title;
  final String description;
  final DateTime? date;
  final String? chapterId;
  final int order;
  final List<String> tags;

  factory TimelineEvent.fromJson(Map<String, dynamic> json) => TimelineEvent(
    id: json['id'] as String? ?? '',
    title: json['title'] as String? ?? 'Evento',
    description: json['description'] as String? ?? '',
    date:
        json['date'] == null ? null : DateTime.tryParse(json['date'] as String),
    chapterId: json['chapterId'] as String?,
    order: json['order'] as int? ?? 0,
    tags: (json['tags'] as List<dynamic>? ?? <dynamic>[])
        .map((e) => e as String)
        .toList(growable: false),
  );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'title': title,
    'description': description,
    'date': date?.toIso8601String(),
    'chapterId': chapterId,
    'order': order,
    'tags': tags,
  };

  TimelineEvent copyWith({
    String? id,
    String? title,
    String? description,
    DateTime? date,
    String? chapterId,
    int? order,
    List<String>? tags,
  }) {
    return TimelineEvent(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      date: date ?? this.date,
      chapterId: chapterId ?? this.chapterId,
      order: order ?? this.order,
      tags: tags ?? this.tags,
    );
  }
}
