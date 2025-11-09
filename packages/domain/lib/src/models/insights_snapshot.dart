import 'package:meta/meta.dart';

import 'insight.dart';
import 'extension_manifest.dart';
import 'prompt_suggestion.dart';
import 'style_metrics.dart';

@immutable
class InsightsSnapshot {
  const InsightsSnapshot({
    required this.projectId,
    required this.generatedAt,
    required this.style,
    required this.insights,
    required this.prompts,
    required this.chapters,
    required this.extensions,
    required this.extensionFindings,
  });

  final String projectId;
  final DateTime generatedAt;
  final StyleMetrics style;
  final List<Insight> insights;
  final List<PromptSuggestion> prompts;
  final List<ChapterInsight> chapters;
  final List<ExtensionManifest> extensions;
  final List<ExtensionFinding> extensionFindings;

  InsightsSnapshot copyWith({
    String? projectId,
    DateTime? generatedAt,
    StyleMetrics? style,
    List<Insight>? insights,
    List<PromptSuggestion>? prompts,
    List<ChapterInsight>? chapters,
    List<ExtensionManifest>? extensions,
    List<ExtensionFinding>? extensionFindings,
  }) {
    return InsightsSnapshot(
      projectId: projectId ?? this.projectId,
      generatedAt: generatedAt ?? this.generatedAt,
      style: style ?? this.style,
      insights: insights ?? this.insights,
      prompts: prompts ?? this.prompts,
      chapters: chapters ?? this.chapters,
      extensions: extensions ?? this.extensions,
      extensionFindings: extensionFindings ?? this.extensionFindings,
    );
  }
}

@immutable
class ChapterInsight {
  const ChapterInsight({
    required this.chapterId,
    required this.chapterTitle,
    required this.wordCount,
    required this.dialogueDensity,
    required this.repeatedTerms,
    required this.characterMentions,
    required this.goalDelta,
  });

  final String chapterId;
  final String chapterTitle;
  final int wordCount;
  final double dialogueDensity;
  final List<String> repeatedTerms;
  final List<String> characterMentions;
  final int goalDelta;

  ChapterInsight copyWith({
    String? chapterId,
    String? chapterTitle,
    int? wordCount,
    double? dialogueDensity,
    List<String>? repeatedTerms,
    List<String>? characterMentions,
    int? goalDelta,
  }) {
    return ChapterInsight(
      chapterId: chapterId ?? this.chapterId,
      chapterTitle: chapterTitle ?? this.chapterTitle,
      wordCount: wordCount ?? this.wordCount,
      dialogueDensity: dialogueDensity ?? this.dialogueDensity,
      repeatedTerms: repeatedTerms ?? this.repeatedTerms,
      characterMentions: characterMentions ?? this.characterMentions,
      goalDelta: goalDelta ?? this.goalDelta,
    );
  }
}
