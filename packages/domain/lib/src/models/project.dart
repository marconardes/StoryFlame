import 'package:meta/meta.dart';

import 'chapter.dart';
import 'character_sheet.dart';
import 'glossary_entry.dart';
import 'creative_idea.dart';
import 'narrative_template.dart';
import 'timeline_event.dart';
import 'world_element.dart';
import 'publication_checklist.dart';
import 'publication_record.dart';
import 'review_comment.dart';

@immutable
class Project {
  const Project({
    required this.id,
    required this.title,
    required this.description,
    required this.updatedAt,
    required this.chapters,
    this.dailyGoal = 0,
    this.totalGoal = 0,
    this.passwordHash,
    this.characters = const <CharacterSheet>[],
    this.glossary = const <GlossaryEntry>[],
    this.timeline = const <TimelineEvent>[],
    this.worldElements = const <WorldElement>[],
    this.creativeIdeas = const <CreativeIdea>[],
    this.templates = const <NarrativeTemplate>[],
    this.publicationChecklist = const PublicationChecklist(
      betaRead: false,
      coverReady: false,
      isbnRegistered: false,
      ebookExported: false,
      kdpPackageReady: false,
    ),
    this.publicationHistory = const <PublicationRecord>[],
    this.reviewComments = const <ReviewComment>[],
  });

  final String id;
  final String title;
  final String description;
  final DateTime updatedAt;
  final List<Chapter> chapters;
  final int dailyGoal;
  final int totalGoal;
  final String? passwordHash;
  final List<CharacterSheet> characters;
  final List<GlossaryEntry> glossary;
  final List<TimelineEvent> timeline;
  final List<WorldElement> worldElements;
  final List<CreativeIdea> creativeIdeas;
  final List<NarrativeTemplate> templates;
  final PublicationChecklist publicationChecklist;
  final List<PublicationRecord> publicationHistory;
  final List<ReviewComment> reviewComments;

  factory Project.fromJson(Map<String, dynamic> json) {
    final chaptersJson = json['chapters'] as List<dynamic>? ?? <dynamic>[];
    return Project(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? 'Sem título',
      description: json['description'] as String? ?? '',
      updatedAt:
          DateTime.tryParse(json['updatedAt'] as String? ?? '') ??
          DateTime.now(),
      chapters: chaptersJson
          .map(
            (chapter) =>
                Chapter.fromJson(chapter as Map<String, dynamic>? ?? {}),
          )
          .toList(growable: false),
      dailyGoal: json['dailyGoal'] as int? ?? 0,
      totalGoal: json['totalGoal'] as int? ?? 0,
      passwordHash: json['passwordHash'] as String?,
      characters: (json['characters'] as List<dynamic>? ?? <dynamic>[])
          .map(
            (character) => CharacterSheet.fromJson(
              character as Map<String, dynamic>? ?? {},
            ),
          )
          .toList(growable: false),
      glossary: (json['glossary'] as List<dynamic>? ?? <dynamic>[])
          .map(
            (entry) =>
                GlossaryEntry.fromJson(entry as Map<String, dynamic>? ?? {}),
          )
          .toList(growable: false),
      timeline: (json['timeline'] as List<dynamic>? ?? <dynamic>[])
          .map(
            (event) =>
                TimelineEvent.fromJson(event as Map<String, dynamic>? ?? {}),
          )
          .toList(growable: false),
      worldElements: (json['worldElements'] as List<dynamic>? ?? <dynamic>[])
          .map(
            (element) =>
                WorldElement.fromJson(element as Map<String, dynamic>? ?? {}),
          )
          .toList(growable: false),
      creativeIdeas: (json['creativeIdeas'] as List<dynamic>? ?? <dynamic>[])
          .map(
            (idea) =>
                CreativeIdea.fromJson(idea as Map<String, dynamic>? ?? {}),
          )
          .toList(growable: false),
      templates: (json['templates'] as List<dynamic>? ?? <dynamic>[])
          .map(
            (template) => NarrativeTemplate.fromJson(
              template as Map<String, dynamic>? ?? {},
            ),
          )
          .toList(growable: false),
      publicationChecklist: PublicationChecklist.fromJson(
        json['publicationChecklist'] as Map<String, dynamic>?,
      ),
      publicationHistory: (json['publicationHistory'] as List<dynamic>? ??
              <dynamic>[])
          .map(
            (record) => PublicationRecord.fromJson(
              record as Map<String, dynamic>? ?? {},
            ),
          )
          .toList(growable: false),
      reviewComments: (json['reviewComments'] as List<dynamic>? ?? <dynamic>[])
          .map(
            (comment) => ReviewComment.fromJson(
              comment as Map<String, dynamic>?,
            ),
          )
          .toList(growable: false),
    );
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'title': title,
    'description': description,
    'updatedAt': updatedAt.toIso8601String(),
    'chapters': chapters.map((chapter) => chapter.toJson()).toList(),
    'dailyGoal': dailyGoal,
    'totalGoal': totalGoal,
    'passwordHash': passwordHash,
    'characters': characters.map((character) => character.toJson()).toList(),
    'glossary': glossary.map((entry) => entry.toJson()).toList(),
    'timeline': timeline.map((event) => event.toJson()).toList(),
    'worldElements': worldElements.map((element) => element.toJson()).toList(),
    'creativeIdeas': creativeIdeas.map((idea) => idea.toJson()).toList(),
    'templates': templates.map((template) => template.toJson()).toList(),
    'publicationChecklist': publicationChecklist.toJson(),
    'publicationHistory':
        publicationHistory.map((record) => record.toJson()).toList(),
    'reviewComments': reviewComments.map((comment) => comment.toJson()).toList(),
  };

  Project copyWith({
    String? id,
    String? title,
    String? description,
    DateTime? updatedAt,
    List<Chapter>? chapters,
    int? dailyGoal,
    int? totalGoal,
    String? passwordHash,
    List<CharacterSheet>? characters,
    List<GlossaryEntry>? glossary,
    List<TimelineEvent>? timeline,
    List<WorldElement>? worldElements,
    List<CreativeIdea>? creativeIdeas,
    List<NarrativeTemplate>? templates,
    PublicationChecklist? publicationChecklist,
    List<PublicationRecord>? publicationHistory,
    List<ReviewComment>? reviewComments,
  }) {
    return Project(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      updatedAt: updatedAt ?? this.updatedAt,
      chapters: chapters ?? this.chapters,
      dailyGoal: dailyGoal ?? this.dailyGoal,
      totalGoal: totalGoal ?? this.totalGoal,
      passwordHash: passwordHash ?? this.passwordHash,
      characters: characters ?? this.characters,
      glossary: glossary ?? this.glossary,
      timeline: timeline ?? this.timeline,
      worldElements: worldElements ?? this.worldElements,
      creativeIdeas: creativeIdeas ?? this.creativeIdeas,
      templates: templates ?? this.templates,
      publicationChecklist: publicationChecklist ?? this.publicationChecklist,
      publicationHistory: publicationHistory ?? this.publicationHistory,
      reviewComments: reviewComments ?? this.reviewComments,
    );
  }
}
