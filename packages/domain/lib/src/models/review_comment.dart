import 'package:meta/meta.dart';

@immutable
class ReviewComment {
  const ReviewComment({
    required this.id,
    required this.message,
    required this.targetChapterId,
    required this.author,
    required this.createdAt,
    required this.updatedAt,
    this.context,
    this.resolved = false,
  });

  final String id;
  final String message;
  final String? context;
  final String? targetChapterId;
  final String author;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool resolved;

  factory ReviewComment.fromJson(Map<String, dynamic>? json) {
    if (json == null) {
      return ReviewComment(
        id: '',
        message: '',
        targetChapterId: null,
        author: '',
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );
    }
    return ReviewComment(
      id: json['id'] as String? ?? '',
      message: json['message'] as String? ?? '',
      context: json['context'] as String?,
      targetChapterId: json['targetChapterId'] as String?,
      author: json['author'] as String? ?? '',
      resolved: json['resolved'] as bool? ?? false,
      createdAt:
          DateTime.tryParse(json['createdAt'] as String? ?? '') ??
          DateTime.now(),
      updatedAt:
          DateTime.tryParse(json['updatedAt'] as String? ?? '') ??
          DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'message': message,
    'context': context,
    'targetChapterId': targetChapterId,
    'author': author,
    'resolved': resolved,
    'createdAt': createdAt.toIso8601String(),
    'updatedAt': updatedAt.toIso8601String(),
  };

  ReviewComment copyWith({
    String? id,
    String? message,
    String? context,
    String? targetChapterId,
    String? author,
    DateTime? createdAt,
    DateTime? updatedAt,
    bool? resolved,
  }) {
    return ReviewComment(
      id: id ?? this.id,
      message: message ?? this.message,
      context: context ?? this.context,
      targetChapterId: targetChapterId ?? this.targetChapterId,
      author: author ?? this.author,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      resolved: resolved ?? this.resolved,
    );
  }
}
