import 'package:meta/meta.dart';

@immutable
class Chapter {
  const Chapter({
    required this.id,
    required this.title,
    required this.summary,
    required this.content,
    required this.wordCount,
    required this.updatedAt,
    this.status = ChapterStatus.draft,
    this.coverImage = '',
  });

  final String id;
  final String title;
  final String summary;
  final String content;
  final int wordCount;
  final DateTime updatedAt;
  final ChapterStatus status;
  final String coverImage;

  factory Chapter.fromJson(Map<String, dynamic> json) => Chapter(
    id: json['id'] as String? ?? '',
    title: json['title'] as String? ?? 'Capítulo sem título',
    summary: json['summary'] as String? ?? '',
    content: json['content'] as String? ?? '',
    wordCount:
        json['wordCount'] as int? ??
        countWords(json['content'] as String? ?? ''),
    updatedAt:
        DateTime.tryParse(json['updatedAt'] as String? ?? '') ?? DateTime.now(),
    status: ChapterStatusX.parse(json['status'] as String? ?? 'draft'),
    coverImage: json['coverImage'] as String? ?? '',
  );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'title': title,
    'summary': summary,
    'content': content,
    'wordCount': wordCount,
    'updatedAt': updatedAt.toIso8601String(),
    'status': status.name,
    'coverImage': coverImage,
  };

  Chapter copyWith({
    String? id,
    String? title,
    String? summary,
    String? content,
    int? wordCount,
    DateTime? updatedAt,
    ChapterStatus? status,
    String? coverImage,
  }) {
    return Chapter(
      id: id ?? this.id,
      title: title ?? this.title,
      summary: summary ?? this.summary,
      content: content ?? this.content,
      wordCount: wordCount ?? this.wordCount,
      updatedAt: updatedAt ?? this.updatedAt,
      status: status ?? this.status,
      coverImage: coverImage ?? this.coverImage,
    );
  }

  static int countWords(String text) {
    final words = text.trim().split(RegExp(r'\s+'));
    return words.where((word) => word.isNotEmpty).length;
  }
}

enum ChapterStatus { draft, revision, finalDraft }

extension ChapterStatusX on ChapterStatus {
  static ChapterStatus parse(String value) {
    return ChapterStatus.values.firstWhere(
      (candidate) => candidate.name == value,
      orElse: () => ChapterStatus.draft,
    );
  }

  String get label {
    switch (this) {
      case ChapterStatus.draft:
        return 'Rascunho';
      case ChapterStatus.revision:
        return 'Revisão';
      case ChapterStatus.finalDraft:
        return 'Pronto';
    }
  }
}
